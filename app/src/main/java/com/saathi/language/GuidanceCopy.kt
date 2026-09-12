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

    fun voiceIntro(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "Voice guidance is on. I will describe one safe step at a time. You can say repeat, help, or cancel."
        GuidanceLanguage.HINDI -> "आवाज़ से मार्गदर्शन चालू है। मैं एक बार में एक सुरक्षित कदम बताऊँगा। आप दोबारा, मदद, या रद्द बोल सकते हैं।"
        GuidanceLanguage.HINGLISH -> "Voice guidance on hai. Main ek time par ek safe step bataunga. Aap repeat, help, ya cancel bol sakte hain."
    }

    fun voiceControlHint(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "When you understand, say understood. Say help to hear it again, or cancel to stop guidance."
        GuidanceLanguage.HINDI -> "समझ आने पर समझ गया बोलें। दोबारा सुनने के लिए मदद, या मार्गदर्शन रोकने के लिए रद्द बोलें।"
        GuidanceLanguage.HINGLISH -> "Samajh aaye to samajh gaya boliye. Phir se sunne ke liye help, ya guidance rokne ke liye cancel boliye."
    }

    fun privateField(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "This is a private field. Enter it yourself; Saathi will not listen to or see the value."
        GuidanceLanguage.HINDI -> "यह निजी जानकारी का फ़ील्ड है। इसे आप खुद भरें; Saathi इसकी कीमत न सुनेगा, न देखेगा।"
        GuidanceLanguage.HINGLISH -> "Yeh private field hai. Isse aap khud bhariye; Saathi value ko na sunega, na dekhega."
    }

    fun guidancePaused(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "Guidance is paused. Nothing was submitted."
        GuidanceLanguage.HINDI -> "मार्गदर्शन रोक दिया गया है। कुछ भी सबमिट नहीं हुआ।"
        GuidanceLanguage.HINGLISH -> "Guidance pause ho gayi hai. Kuch bhi submit nahi hua."
    }

    fun acknowledged(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "Great. Take your time and use the highlighted control. I will speak again after the screen changes."
        GuidanceLanguage.HINDI -> "बहुत बढ़िया। आराम से हाइलाइट किए गए विकल्प का उपयोग करें। स्क्रीन बदलने पर मैं फिर बोलूँगा।"
        GuidanceLanguage.HINGLISH -> "Bahut badhiya. Aaram se highlighted control use kijiye. Screen badalne par main phir bolunga."
    }

    fun voiceFallback(language: GuidanceLanguage) = when (language) {
        GuidanceLanguage.ENGLISH -> "I heard you. Please say understood, help, or cancel."
        GuidanceLanguage.HINDI -> "मैंने सुना। कृपया समझ गया, मदद, या रद्द बोलें।"
        GuidanceLanguage.HINGLISH -> "Maine suna. Please samajh gaya, help, ya cancel boliye."
    }
}
