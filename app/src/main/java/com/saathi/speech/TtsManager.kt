package com.saathi.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import com.saathi.language.GuidanceLanguage
import java.util.Locale

class TtsManager(context: Context, private val onHindiVoiceMissing: (() -> Unit)? = null) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var queued: Pair<String, GuidanceLanguage>? = null
    private var promptedForHindi = false
    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        queued?.let { (text, language) -> queued = null; speak(text, language) }
    }
    fun speak(text: String, language: String) {
        speak(text, GuidanceLanguage.fromApiTag(language))
    }
    fun speak(text: String, language: GuidanceLanguage) {
        if (!ready) { queued = text to language; return }
        val result = tts.setLanguage(language.ttsLocale)
        if ((language == GuidanceLanguage.HINDI || language == GuidanceLanguage.HINGLISH) &&
            (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)) {
            if (!promptedForHindi) { promptedForHindi = true; onHindiVoiceMissing?.invoke() }
            tts.setLanguage(Locale.Builder().setLanguage("en").setRegion("IN").build())
        }
        tts.setSpeechRate(0.9f)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "saathi-step")
    }
    fun release() { tts.stop(); tts.shutdown() }
}
