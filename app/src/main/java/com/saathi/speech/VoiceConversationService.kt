package com.saathi.speech

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.saathi.language.GuidanceCopy
import com.saathi.language.GuidanceLanguage
import kotlin.math.max
import kotlin.math.min

/**
 * Explicit, foreground voice mode for a guided session. It is deliberately turn-based: Saathi
 * speaks a short prompt, listens for a short answer, then waits for the next on-screen action.
 * Recognition is never started for sensitive fields.
 */
class VoiceConversationService : Service(), RecognitionListener {
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var tts: TtsManager? = null
    private var language = GuidanceLanguage.ENGLISH
    private var active = false
    private var listening = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TtsManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                language = GuidanceLanguage.fromStorage(intent.getStringExtra(EXTRA_LANGUAGE))
                active = true
                showForegroundNotice()
                speakOnly(GuidanceCopy.voiceIntro(language))
            }
            ACTION_ASK -> {
                language = GuidanceLanguage.fromStorage(intent.getStringExtra(EXTRA_LANGUAGE))
                if (active) ask(intent.getStringExtra(EXTRA_PROMPT).orEmpty())
            }
            ACTION_SPEAK_ONLY -> if (active) speakOnly(intent.getStringExtra(EXTRA_PROMPT).orEmpty())
            ACTION_LISTEN -> if (active) startListening()
            ACTION_STOP -> stopConversation()
        }
        return START_NOT_STICKY
    }

    private fun ask(prompt: String) {
        if (prompt.isBlank()) return
        stopListening()
        // Start listening only after the speech engine completes; elapsed-time estimates cause
        // silence or talking-over on slower engines and after audio interruptions.
        tts?.speak(prompt, language) { completed ->
            if (completed && active) handler.post { startListening() }
            if (!completed && active) sendBroadcast(Intent(ACTION_VOICE_UNAVAILABLE).setPackage(packageName))
        }
    }

    private fun speakOnly(prompt: String) {
        stopListening()
        if (prompt.isNotBlank()) tts?.speak(prompt, language)
    }

    private fun startListening() {
        if (listening || !SpeechRecognizer.isRecognitionAvailable(this)) return
        recognizer = (recognizer ?: SpeechRecognizer.createSpeechRecognizer(this)).also { it.setRecognitionListener(this) }
        listening = true
        recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.sttTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Saathi")
        })
    }

    private fun stopListening() {
        listening = false
        handler.removeCallbacksAndMessages(null)
        runCatching { recognizer?.cancel() }
    }

    override fun onResults(results: Bundle) {
        listening = false
        val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim().orEmpty()
        if (text.isNotBlank()) {
            sendBroadcast(Intent(ACTION_REPLY).setPackage(packageName).putExtra(EXTRA_REPLY, text))
        }
    }

    override fun onError(error: Int) {
        listening = false
        // Recognition commonly stops after a timeout or audio-focus interruption. Keep the
        // conversation alive by offering the next short listening turn instead of going silent.
        if (active && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            handler.postDelayed({ if (active) startListening() }, 700)
        } else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            sendBroadcast(Intent(ACTION_VOICE_UNAVAILABLE).setPackage(packageName))
        }
    }
    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private fun stopConversation() {
        active = false
        stopListening()
        recognizer?.destroy()
        recognizer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopListening()
        recognizer?.destroy()
        tts?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showForegroundNotice() {
        val notification = android.app.Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Saathi voice guidance is active")
            .setContentText("Listening only for short guidance replies. Say cancel to stop.")
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Saathi voice guidance", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NotificationManager::class.java)).createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_REPLY = "com.saathi.action.VOICE_REPLY"
        const val ACTION_VOICE_UNAVAILABLE = "com.saathi.action.VOICE_UNAVAILABLE"
        private const val ACTION_START = "com.saathi.action.START_VOICE"
        private const val ACTION_ASK = "com.saathi.action.ASK_VOICE"
        private const val ACTION_SPEAK_ONLY = "com.saathi.action.SPEAK_VOICE"
        private const val ACTION_LISTEN = "com.saathi.action.LISTEN_VOICE"
        private const val ACTION_STOP = "com.saathi.action.STOP_VOICE"
        private const val EXTRA_PROMPT = "prompt"
        private const val EXTRA_LANGUAGE = "language"
        const val EXTRA_REPLY = "reply"
        private const val CHANNEL_ID = "saathi_voice_guidance"
        private const val NOTIFICATION_ID = 41

        fun start(context: Context, language: GuidanceLanguage) = launch(context, ACTION_START, language)
        fun ask(context: Context, language: GuidanceLanguage, prompt: String) = launch(context, ACTION_ASK, language, prompt)
        fun speakOnly(context: Context, language: GuidanceLanguage, prompt: String) = launch(context, ACTION_SPEAK_ONLY, language, prompt)
        fun listen(context: Context, language: GuidanceLanguage) = launch(context, ACTION_LISTEN, language)
        fun stop(context: Context) = context.stopService(Intent(context, VoiceConversationService::class.java))

        private fun launch(context: Context, action: String, language: GuidanceLanguage, prompt: String? = null) {
            val intent = Intent(context, VoiceConversationService::class.java)
                .setAction(action)
                .putExtra(EXTRA_LANGUAGE, language.storageValue)
            prompt?.let { intent.putExtra(EXTRA_PROMPT, it) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        }
    }
}
