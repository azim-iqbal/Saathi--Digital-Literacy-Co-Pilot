package com.saathi.orchestrator

import android.content.Context
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.saathi.capture.ScreenshotCapture
import com.saathi.core.GuideStep
import com.saathi.core.GuideAction
import com.saathi.core.StepHistory
import com.saathi.core.UiNode
import com.saathi.core.GuidancePolicy
import com.saathi.guardrails.GuardrailAuditLog
import com.saathi.guardrails.GuardrailDecision
import com.saathi.guardrails.GuardrailEngine
import com.saathi.guardrails.GuardrailResult
import com.saathi.language.GuidanceCopy
import com.saathi.language.GuidanceLanguage
import com.saathi.overlay.HighlightOverlayService
import com.saathi.speech.TtsManager
import com.saathi.speech.VoiceConversationService
import java.util.concurrent.Executors

object SaathiSession {
    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private var context: Context? = null
    private var tts: TtsManager? = null
    private var goal = ""
    private var language = GuidanceLanguage.ENGLISH
    private var flowCategory: String? = null
    private var offlineNoticeGiven = false
    private var spokenPromptsEnabled = false
    private var lastSpokenTargetId: String? = null
    private var active = false
    private var latestNodes: List<UiNode> = emptyList()
    private var settledFingerprint = ""
    private var expectedFingerprint = ""
    private var lastStep: GuideStep? = null
    private var history = mutableListOf<StepHistory>()
    private var pendingRunnable: Runnable? = null
    private var wrongTapRunnable: Runnable? = null
    private var voiceReplyReceiver: BroadcastReceiver? = null
    private var currentPackage: String? = null
    private var restrictionAnnouncedFor: String? = null
    private var lastPresentedAt = 0L
    private var watchdogRunnable: Runnable? = null

    fun start(
        appContext: Context,
        goalText: String,
        languageMode: GuidanceLanguage,
        speakPrompts: Boolean = false
    ): GuardrailResult {
        context = appContext.applicationContext
        goal = goalText
        language = languageMode

        val scope = GuardrailEngine.classify(goalText).also(GuardrailAuditLog::record)
        if (scope.decision == GuardrailDecision.REFUSE) {
            active = false
            ensureTts()
            tts?.speak(GuidanceCopy.guardrailRedirect(language), language)
            return scope
        }

        flowCategory = scope.category
        active = true
        offlineNoticeGiven = false
        spokenPromptsEnabled = speakPrompts
        lastSpokenTargetId = null
        history.clear()
        settledFingerprint = ""
        expectedFingerprint = ""
        lastStep = null
        currentPackage = null
        restrictionAnnouncedFor = null
        ensureTts()
        GuidanceForegroundService.start(context!!)
        startWatchdog()
        if (spokenPromptsEnabled) startVoiceConversation()
        return scope
    }

    fun stop() {
        active = false
        handler.removeCallbacksAndMessages(null)
        watchdogRunnable = null
        context?.stopService(Intent(context, HighlightOverlayService::class.java))
        context?.let(VoiceConversationService::stop)
        context?.let(GuidanceForegroundService::stop)
        unregisterVoiceConversation()
    }

    fun isActive() = active

    fun onPackageChanged(packageName: String?) {
        currentPackage = packageName
        val restricted = GuidancePolicy.restrictedAppName(packageName) ?: return
        if (restrictionAnnouncedFor == packageName) return
        restrictionAnnouncedFor = packageName
        val message = GuidancePolicy.unavailableMessage(language, restricted)
        handler.post {
            val app = context ?: return@post
            Toast.makeText(app, message, Toast.LENGTH_LONG).show()
            if (spokenPromptsEnabled) VoiceConversationService.speakOnly(app, language, message)
            app.startService(HighlightOverlayService.intent(app, null, emptyList(), false, message))
        }
    }

    fun onScreenChanged(nodes: List<UiNode>, packageName: String? = currentPackage) {
        if (!active) return
        if (GuidancePolicy.restrictedAppName(packageName) != null) return
        latestNodes = nodes
        pendingRunnable?.let(handler::removeCallbacks)
        pendingRunnable = Runnable { evaluateSettledScreen(nodes) }.also { handler.postDelayed(it, 600) }
    }

    private fun evaluateSettledScreen(nodes: List<UiNode>) {
        if (!active || nodes !== latestNodes) return
        val fingerprint = nodes.joinToString("#") { it.fingerprintPart() }.hashCode().toString()
        if (fingerprint == settledFingerprint) return
        val previousFailed = lastStep != null && fingerprint == expectedFingerprint
        settledFingerprint = fingerprint
        wrongTapRunnable?.let(handler::removeCallbacks)
        requestStep(nodes, previousFailed)
    }

    private fun requestStep(nodes: List<UiNode>, previousFailed: Boolean) {
        val app = context ?: return
        if (GuardrailEngine.screenIsUnsafe(nodes.asSequence().flatMap { sequenceOf(it.text, it.description, it.hint) })) {
            val scope = GuardrailResult(GuardrailDecision.REFUSE, null, "unsafe_screen_content")
            GuardrailAuditLog.record(scope)
            handler.post { present(refusalStep()) }
            return
        }
        executor.execute {
            val isDemoScreen = nodes.any { node ->
                node.resourceId?.startsWith("com.saathi:id/") == true && node.resourceId.contains("_")
            }
            val rateLimitAvailable = GeminiRateLimiter(app).tryAcquire()
            val canUseRemoteGuidance = !isDemoScreen && GeminiClient().available() && rateLimitAvailable
            val remoteStep = if (canUseRemoteGuidance) {
                GeminiClient().request(
                    goal = goal,
                    language = language.apiTag,
                    nodes = nodes,
                    history = history,
                    screenshotBase64 = ScreenshotCapture.maskedJpegBase64(nodes),
                    previousFailed = previousFailed
                )
            } else {
                null
            }
            val fallback = WebsiteGuide.next(goal, nodes, language.apiTag, previousFailed)
                ?: DemoGuide.next(goal, nodes, language.apiTag, previousFailed)
            val step = when {
                remoteStep?.action == GuideAction.REFUSE -> refusalStep()
                remoteStep != null -> remoteStep
                !offlineNoticeGiven && !isDemoScreen && GeminiClient().available() -> {
                    offlineNoticeGiven = true
                    val notice = if (rateLimitAvailable) GuidanceCopy.offlineNotice(language) else "I’m guiding a little more slowly right now to stay within a safe limit."
                    fallback.copy(speechText = "$notice ${fallback.speechText}")
                }
                else -> fallback
            }
            handler.post { present(step) }
        }
    }

    private fun present(step: GuideStep) {
        if (!active) return
        lastPresentedAt = System.currentTimeMillis()

        lastStep?.let { history += StepHistory(it.speechText, it.expectedOutcome) }
        lastStep = step

        val app = context ?: return
        speakWhenUseful(step)
        app.startService(
            HighlightOverlayService.intent(
                app,
                step.target?.bounds,
                latestNodes.filter { it.isSensitive }.map { it.bounds },
                step.goalComplete
            )
        )

        if (step.action == GuideAction.REFUSE) {
            active = false
            context?.let(VoiceConversationService::stop)
            unregisterVoiceConversation()
            return
        }
        if (step.goalComplete) GuidanceFlowCache(app).markCompleted(flowCategory)
        if (!step.goalComplete) {
            expectedFingerprint = settledFingerprint
            wrongTapRunnable = Runnable {
                if (active && settledFingerprint == expectedFingerprint) requestStep(latestNodes, true)
            }.also { handler.postDelayed(it, 5_000) }
        }
    }

    private fun refusalStep() = GuideStep(
        speechText = GuidanceCopy.guardrailRedirect(language), language = language.apiTag, target = null,
        expectedOutcome = "No guidance is provided outside Saathi's task scope.", goalComplete = true,
        action = GuideAction.REFUSE
    )

    /**
     * Visual guidance is the default. Speech is opt-in and only fires for a genuinely new
     * destination or a gentle correction, avoiding repetitive narration while a user types.
     */
    private fun speakWhenUseful(step: GuideStep) {
        if (!spokenPromptsEnabled) return

        val targetId = step.target?.resourceId ?: step.target?.description
        val isCorrection = step.correctionNote != null
        if (isCorrection || targetId != lastSpokenTargetId) {
            val prompt = step.correctionNote ?: step.speechText
            val isSensitiveTarget = targetId.orEmpty().lowercase().let { it.contains("pin") || it.contains("password") || it.contains("otp") || it.contains("cvv") }
            val app = context ?: return
            if (isSensitiveTarget) {
                VoiceConversationService.speakOnly(app, language, GuidanceCopy.privateField(language))
            } else {
                VoiceConversationService.ask(app, language, "$prompt ${GuidanceCopy.voiceControlHint(language)}")
            }
            lastSpokenTargetId = targetId
        }
    }

    private fun startVoiceConversation() {
        val app = context ?: return
        unregisterVoiceConversation()
        voiceReplyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == VoiceConversationService.ACTION_VOICE_UNAVAILABLE) {
                    Toast.makeText(context, "Voice input is unavailable. You can continue with the on-screen highlight or type a reply.", Toast.LENGTH_LONG).show()
                } else handleVoiceReply(intent.getStringExtra(VoiceConversationService.EXTRA_REPLY).orEmpty())
            }
        }
        val filter = IntentFilter().apply {
            addAction(VoiceConversationService.ACTION_REPLY)
            addAction(VoiceConversationService.ACTION_VOICE_UNAVAILABLE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            app.registerReceiver(voiceReplyReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION") app.registerReceiver(voiceReplyReceiver, filter)
        }
        VoiceConversationService.start(app, language)
    }

    private fun unregisterVoiceConversation() {
        val app = context ?: return
        voiceReplyReceiver?.let { runCatching { app.unregisterReceiver(it) } }
        voiceReplyReceiver = null
    }

    /** Interpret only short control phrases; spoken personal data is neither recorded nor handled here. */
    private fun handleVoiceReply(reply: String) {
        if (!active || reply.isBlank()) return
        val normalized = reply.lowercase()
        val app = context ?: return
        when {
            listOf("cancel", "stop", "exit", "रद्द", "बंद", "रोक").any(normalized::contains) -> {
                VoiceConversationService.speakOnly(app, language, GuidanceCopy.guidancePaused(language))
                stop()
            }
            listOf("help", "repeat", "again", "मदद", "दोबारा", "phir").any(normalized::contains) -> {
                lastStep?.let { VoiceConversationService.ask(app, language, it.speechText) }
            }
            listOf("understood", "got it", "samajh", "समझ", "हाँ", "haan").any(normalized::contains) -> {
                VoiceConversationService.speakOnly(app, language, GuidanceCopy.acknowledged(language))
            }
            else -> VoiceConversationService.ask(app, language, GuidanceCopy.voiceFallback(language))
        }
    }

    private fun ensureTts() {
        if (tts != null) return

        val app = context ?: return
        tts = TtsManager(app) {
            handler.post {
                Toast.makeText(app, GuidanceCopy.ttsSetup(language), Toast.LENGTH_LONG).show()
                runCatching {
                    app.startActivity(
                        Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }
    }

    /** A missed accessibility callback or slow network must never turn into unexplained silence. */
    private fun startWatchdog() {
        watchdogRunnable?.let(handler::removeCallbacks)
        watchdogRunnable = object : Runnable {
            override fun run() {
                if (!active) return
                if (latestNodes.isNotEmpty() && System.currentTimeMillis() - lastPresentedAt > 20_000L) {
                    requestStep(latestNodes, true)
                }
                handler.postDelayed(this, 20_000L)
            }
        }.also { handler.postDelayed(it, 20_000L) }
    }
}
