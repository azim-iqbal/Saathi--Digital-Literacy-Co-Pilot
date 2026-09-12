package com.saathi.language

object GuidanceCopy {
    fun guardrailRedirect(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "I can only help you complete tasks on your screen, like paying a bill or filling a form. Is there a task like that I can help with?"
        GuidanceLanguage.HINDI -> "मैं केवल आपकी स्क्रीन पर काम पूरा करने में मदद कर सकता हूँ - जैसे बिल भरना या फॉर्म भरना। क्या मैं ऐसे किसी काम में मदद करूँ?"
        GuidanceLanguage.HINGLISH -> HinglishTemplates.guardrailRedirect
    }
    fun offlineNotice(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "I’m using offline guidance right now. I can still guide you through the demo flow."
        GuidanceLanguage.HINDI -> "अभी मैं ऑफलाइन मार्गदर्शन का उपयोग कर रहा हूँ। मैं आपको डेमो फ्लो में मार्गदर्शन देता रहूँगा।"
        GuidanceLanguage.HINGLISH -> HinglishTemplates.offlineNotice
    }
    fun lowConfidence(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "I didn’t catch that clearly. Please say it again or type your goal."
        GuidanceLanguage.HINDI -> "मुझे बात साफ़ समझ नहीं आई। कृपया दोबारा बोलें या अपना लक्ष्य लिखें।"
        GuidanceLanguage.HINGLISH -> HinglishTemplates.lowConfidence
    }
    fun ttsSetup(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "The Hindi voice is not installed. You can install it in Text-to-Speech settings; English voice will work for now."
        GuidanceLanguage.HINDI -> "हिंदी आवाज़ इंस्टॉल नहीं है। आप इसे Text-to-Speech सेटिंग्स में इंस्टॉल कर सकते हैं; अभी अंग्रेज़ी आवाज़ काम करेगी।"
        GuidanceLanguage.HINGLISH -> HinglishTemplates.ttsSetup
    }
}
