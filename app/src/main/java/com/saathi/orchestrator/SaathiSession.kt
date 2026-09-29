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
import com.saathi.core.ObservationGate
import com.saathi.storage.GuidanceStateStore

object SaathiSession {
    private val mutableStatus = kotlinx.coroutines.flow.MutableStateFlow("Stopped")
    val status: kotlinx.coroutines.flow.StateFlow<String> = mutableStatus
    fun pause() { stop(); mutableStatus.value = "Paused" }
    private val handler = Handler(Looper.getMainLooper())
    private val observationGate = ObservationGate()
    private var context: Context? = null
    private var tts: TtsManager? = null
    private var goal = ""
    private var language = GuidanceLanguage.ENGLISH
    private var flowCategory: String? = null
    private var spokenPromptsEnabled = false
    private var lastSpokenTargetId: String? = null
    private var active = false
    private var latestNodes: List<UiNode> = emptyList()
    private var lastStep: GuideStep? = null
    private var history = mutableListOf<StepHistory>()
    private var pendingRunnable: Runnable? = null
    private var currentPackage: String? = null

    fun start(
        appContext: Context,
        goalText: String,
        languageMode: GuidanceLanguage,
        speakPrompts: Boolean = false
    ): GuardrailResult {
        stop()
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
        observationGate.start()
        mutableStatus.value = "Active"
        spokenPromptsEnabled = speakPrompts
        lastSpokenTargetId = null
        history.clear()
        lastStep = null
        currentPackage = null
        ensureTts()
        GuidanceForegroundService.start(context!!)

        // Session microphone is disabled until explicit push-to-talk lifecycle tests exist.
        return scope
    }

    fun stop() {
        active = false
        mutableStatus.value = "Stopped"
        observationGate.stop()
        latestNodes = emptyList()
        lastStep = null
        history.clear()
        ScreenshotCapture.stop()
        tts?.release()
        tts = null
        context?.let { GuidanceStateStore(it).clear() }
        handler.removeCallbacksAndMessages(null)
        context?.stopService(Intent(context, HighlightOverlayService::class.java))
        context?.let(VoiceConversationService::stop)
        context?.let(GuidanceForegroundService::stop)
    }

    fun isActive() = active
    fun sessionKey(): String? = observationGate.sessionKey()
    fun stopForSession(key: String?) { if (observationGate.matchesSession(key)) stop() }
    fun presentationKey(): String? = observationGate.currentKey()

    /** A failed old overlay must not terminate a newer observation or restarted session. */
    fun stopForPresentation(key: String?) {
        if (observationGate.matchesPresentation(key)) stop()
    }

    /** Called on the main thread at the event boundary, before copying a replacement tree. */
    fun invalidateScreen() {
        observationGate.invalidate()
        pendingRunnable?.let(handler::removeCallbacks)
        latestNodes = emptyList()
        lastStep = null
        lastSpokenTargetId = null
        context?.stopService(Intent(context, HighlightOverlayService::class.java))
        context?.let(VoiceConversationService::stop)
        tts?.stop()
    }

    fun beginObservation(packageName: String, windowId: Int): ObservationGate.Ticket? =
        observationGate.observe(packageName, windowId)

    fun onScreenChanged(nodes: List<UiNode>, ticket: ObservationGate.Ticket) {
        // Accessibility copying runs off-thread. All session state and presentation stay on main.
        handler.post {
            if (!observationGate.accepts(ticket)) return@post
            latestNodes = nodes
            currentPackage = ticket.packageName
            pendingRunnable?.let(handler::removeCallbacks)
            pendingRunnable = Runnable {
                if (!observationGate.accepts(ticket)) return@Runnable
                val demoIds = setOf("recharge_bills", "electricity_biller", "account_input", "amount_input", "pin_input", "pay_button", "success_title")
                val isDemo = ticket.packageName == "com.saathi" && nodes.any {
                    it.resourceId?.removePrefix("com.saathi:id/") in demoIds &&
                        it.resourceId?.startsWith("com.saathi:id/") == true
                }
                val step = if (isDemo) DemoGuide.next(goal, nodes, language.apiTag, false) else unsupportedStep()
                // Only a current, enabled local node can determine overlay coordinates.
                val target = step.target?.let { proposed ->
                    nodes.singleOrNull { it.resourceId == proposed.resourceId && it.isEnabled && !it.isSensitive }
                        ?.let { proposed.copy(bounds = android.graphics.Rect(it.bounds)) }
                }
                if (observationGate.accepts(ticket)) present(step.copy(target = target))
            }.also { handler.postDelayed(it, 150) }
        }
    }

    private fun unsupportedStep() = GuideStep(
        speechText = when (language) {
            GuidanceLanguage.ENGLISH -> "Guidance for this screen is unavailable. This build supports local practice only."
            GuidanceLanguage.HINDI -> "इस स्क्रीन पर मार्गदर्शन उपलब्ध नहीं है। इस संस्करण में केवल अभ्यास उपलब्ध है।"
            GuidanceLanguage.HINGLISH -> "Is screen par guidance available nahi hai. Is build mein sirf local practice hai."
        }, language = language.apiTag, target = null,
        expectedOutcome = "Wait for an explicitly chosen practice screen.", goalComplete = false
    )

    private fun present(step: GuideStep) {
        if (!active) return

        lastStep?.let { history += StepHistory(it.speechText, it.expectedOutcome) }
        lastStep = step

        val app = context ?: return
        speakWhenUseful(step)
        app.startService(
            HighlightOverlayService.intent(
                app,
                step.target?.bounds,
                latestNodes.filter { it.isSensitive }.map { it.bounds },
                step.goalComplete,
                if (step.target == null) step.speechText else null,
                presentationKey()
            )
        )

        if (step.action == GuideAction.REFUSE) {
            stop()
            return
        }
        if (step.goalComplete) GuidanceFlowCache(app).markCompleted(flowCategory)
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
                tts?.speak(GuidanceCopy.privateField(language), language)
            } else {
                tts?.speak(prompt, language)
            }
            lastSpokenTargetId = targetId
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

}
