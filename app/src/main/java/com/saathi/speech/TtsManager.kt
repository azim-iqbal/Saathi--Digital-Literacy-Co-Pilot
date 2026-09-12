package com.saathi.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.saathi.language.GuidanceLanguage
import java.util.Locale

class TtsManager(context: Context, private val onHindiVoiceMissing: (() -> Unit)? = null) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var queued: Pair<String, GuidanceLanguage>? = null
    private var promptedForHindi = false
    private var completion: ((Boolean) -> Unit)? = null
    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) = Unit
            override fun onDone(utteranceId: String) { completion?.let { callback -> completion = null; callback(true) } }
            @Deprecated("Deprecated in Java") override fun onError(utteranceId: String) { completion?.let { callback -> completion = null; callback(false) } }
            override fun onError(utteranceId: String, errorCode: Int) { completion?.let { callback -> completion = null; callback(false) } }
        })
    }
    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        queued?.let { (text, language) -> queued = null; speak(text, language) }
    }
    fun speak(text: String, language: String) {
        speak(text, GuidanceLanguage.fromApiTag(language))
    }
    fun speak(text: String, language: GuidanceLanguage) {
        speak(text, language, null)
    }
    fun speak(text: String, language: GuidanceLanguage, onFinished: ((Boolean) -> Unit)?) {
        if (!ready) { queued = text to language; return }
        val result = tts.setLanguage(language.ttsLocale)
        if ((language == GuidanceLanguage.HINDI || language == GuidanceLanguage.HINGLISH) &&
            (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)) {
            if (!promptedForHindi) { promptedForHindi = true; onHindiVoiceMissing?.invoke() }
            tts.setLanguage(Locale.Builder().setLanguage("en").setRegion("IN").build())
        }
        tts.setSpeechRate(0.9f)
        completion = onFinished
        val speakResult = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "saathi-step-${System.nanoTime()}")
        if (speakResult == TextToSpeech.ERROR) { completion = null; onFinished?.invoke(false) }
    }
    fun release() { tts.stop(); tts.shutdown() }
}
