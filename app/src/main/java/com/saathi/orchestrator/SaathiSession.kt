package com.saathi.orchestrator

import android.content.Context
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
import com.saathi.guardrails.GuardrailAuditLog
import com.saathi.guardrails.GuardrailDecision
import com.saathi.guardrails.GuardrailEngine
import com.saathi.guardrails.GuardrailResult
import com.saathi.language.GuidanceCopy
import com.saathi.language.GuidanceLanguage
import com.saathi.overlay.HighlightOverlayService
import com.saathi.speech.TtsManager
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
    private var active = false
    private var latestNodes: List<UiNode> = emptyList()
    private var settledFingerprint = ""
    private var expectedFingerprint = ""
    private var lastStep: GuideStep? = null
    private var history = mutableListOf<StepHistory>()
    private var pendingRunnable: Runnable? = null
    private var wrongTapRunnable: Runnable? = null

    fun start(appContext: Context, goalText: String, languageMode: GuidanceLanguage): GuardrailResult {
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
        history.clear()
        settledFingerprint = ""
        expectedFingerprint = ""
        lastStep = null
        ensureTts()
        return scope
    }

    fun stop() {
        active = false
        handler.removeCallbacksAndMessages(null)
        context?.stopService(Intent(context, HighlightOverlayService::class.java))
    }

    fun isActive() = active

    fun onScreenChanged(nodes: List<UiNode>) {
        if (!active) return
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
            val canUseRemoteGuidance = !isDemoScreen && GeminiClient().available() && GeminiRateLimiter(app).tryAcquire()
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
            val fallback = DemoGuide.next(nodes, language.apiTag, previousFailed)
            val step = when {
                remoteStep?.action == GuideAction.REFUSE -> refusalStep()
                remoteStep != null -> remoteStep
                !offlineNoticeGiven && !isDemoScreen && GeminiClient().available() -> {
                    offlineNoticeGiven = true
                    fallback.copy(speechText = "${GuidanceCopy.offlineNotice(language)} ${fallback.speechText}")
                }
                else -> fallback
            }
            handler.post { present(step) }
        }
    }

    private fun present(step: GuideStep) {
        if (!active) return

        lastStep?.let { history += StepHistory(it.speechText, it.expectedOutcome) }
        lastStep = step

        val app = context ?: return
        tts?.speak(step.correctionNote ?: step.speechText, language)
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
