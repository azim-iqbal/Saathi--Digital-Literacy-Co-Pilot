package com.saathi.orchestrator

import com.saathi.core.GuideStep
import com.saathi.core.GuideTarget
import com.saathi.core.UiNode

/**
 * Local, adaptive website fallback for travel and service portals. It uses only labels exposed
 * by the active accessibility tree, so it remains useful across IRCTC, airline, and browser
 * flows without claiming to know private account data or a site's hidden state.
 */
object WebsiteGuide {
    fun next(goal: String, nodes: List<UiNode>, language: String, previousFailed: Boolean): GuideStep? {
        val travel = goal.containsAny("train", "rail", "irctc", "flight", "airline", "ticket", "journey", "booking")
        if (!travel) return null
        val visible = nodes.filter { it.isEnabled && (it.isClickable || it.className?.contains("EditText") == true) && !it.isSensitive }
        val candidate = firstMatching(visible,
            "from", "origin", "departure", "to", "destination", "arrival", "date", "journey", "search", "find trains", "find flights",
            "passenger", "traveller", "class", "quota", "continue", "review", "book"
        ) ?: return waiting(language, previousFailed)
        val label = candidate.text ?: candidate.description ?: candidate.hint ?: "highlighted field"
        val instruction = when {
            label.containsAny("from", "origin", "departure") -> triple("Tap From and choose your departure city or station.", "From दबाकर अपना प्रस्थान शहर या स्टेशन चुनें।", "From tap karke apna departure city ya station chuniye.")
            label.containsAny("to", "destination", "arrival") -> triple("Tap To and choose your destination city or station.", "To दबाकर अपना गंतव्य शहर या स्टेशन चुनें।", "To tap karke apna destination city ya station chuniye.")
            label.containsAny("date", "journey") -> triple("Tap the date field and choose your travel date.", "तारीख़ वाले फ़ील्ड को दबाकर यात्रा की तारीख़ चुनें।", "Date field tap karke travel date chuniye.")
            label.containsAny("search", "find") -> triple("Tap Search after checking your route and date.", "रूट और तारीख़ जाँचने के बाद Search दबाएं।", "Route aur date check karke Search tap kijiye.")
            label.containsAny("passenger", "traveller") -> triple("Tap Passenger details and enter each traveller’s information yourself.", "Passenger details दबाकर हर यात्री की जानकारी स्वयं भरें।", "Passenger details tap karke har traveller ki information khud bhariye.")
            label.containsAny("review", "continue", "book") -> triple("Review the journey, fare, and passenger details. You must make the final booking decision yourself.", "यात्रा, किराया और यात्री जानकारी जाँचें। अंतिम बुकिंग का फैसला आप स्वयं करें।", "Journey, fare aur passenger details check kijiye. Final booking decision aap khud lijiye.")
            else -> triple("Tap the highlighted $label option to continue.", "आगे बढ़ने के लिए हाइलाइट किए गए $label विकल्प को दबाएं।", "Aage badhne ke liye highlighted $label option tap kijiye.")
        }
        return GuideStep(pick(language, instruction), language, GuideTarget(candidate.bounds, candidate.resourceId, label), "The next travel step is displayed.", false,
            correctionNote = if (previousFailed) pick(language, triple("That is okay. Use the highlighted option when you are ready.", "कोई बात नहीं। तैयार होने पर हाइलाइट किए गए विकल्प का उपयोग करें।", "Koi baat nahi. Ready hone par highlighted option use kijiye.")) else null)
    }

    private fun waiting(language: String, correction: Boolean) = GuideStep(
        speechText = pick(language, triple(
            if (correction) "I am still waiting for a supported travel field. Open the official booking website and show the route search page." else "Open the official booking website and show the route search page. I will guide the next visible field.",
            if (correction) "मैं अभी भी समर्थित यात्रा फ़ील्ड की प्रतीक्षा कर रहा हूँ। आधिकारिक बुकिंग वेबसाइट में रूट खोज पेज खोलें।" else "आधिकारिक बुकिंग वेबसाइट में रूट खोज पेज खोलें। मैं अगले दिखने वाले फ़ील्ड का मार्गदर्शन करूँगा।",
            if (correction) "Main supported travel field ka wait kar raha hoon. Official booking website mein route search page kholiye." else "Official booking website mein route search page kholiye. Main next visible field guide karunga."
        )), language = language, target = null, expectedOutcome = "A supported travel search field is visible.", goalComplete = false
    )

    private fun firstMatching(nodes: List<UiNode>, vararg words: String) = nodes.firstOrNull { node ->
        val label = listOf(node.text, node.description, node.hint, node.resourceId).filterNotNull().joinToString(" ").lowercase()
        words.any(label::contains)
    }
    private fun String.containsAny(vararg values: String) = values.any { contains(it, ignoreCase = true) }
    private fun triple(en: String, hi: String, hinglish: String) = arrayOf(en, hi, hinglish)
    private fun pick(language: String, values: Array<String>) = when (language) { "hi-IN" -> values[1]; "hinglish" -> values[2]; else -> values[0] }
}
