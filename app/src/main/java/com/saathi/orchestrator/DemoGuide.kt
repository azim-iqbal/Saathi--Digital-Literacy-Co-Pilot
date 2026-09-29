package com.saathi.orchestrator

import android.graphics.Rect
import com.saathi.core.GuideStep
import com.saathi.core.GuideTarget
import com.saathi.core.UiNode

/** Local guide restricted by the session coordinator to the synthetic practice flow. */
object DemoGuide {
    fun next(goal: String, nodes: List<UiNode>, language: String, previousFailed: Boolean): GuideStep {
        val pick = when {
            nodes.anyId("success_title") -> complete(language)
            nodes.anyId("account_input") && !nodes.hasValue("account_input") -> instruction(
                nodes,
                "account_input",
                language,
                "Enter your ${serviceName(nodes)} account or subscriber number.",
                "अपना ${serviceNameHindi(nodes)} खाता या सब्सक्राइबर नंबर दर्ज करें।",
                "Apna ${serviceNameHinglish(nodes)} account ya subscriber number daaliye.",
                "Account number is entered."
            )
            nodes.anyId("amount_input") && !nodes.hasValue("amount_input") -> instruction(nodes, "amount_input", language, "Enter the amount to pay.", "भुगतान की राशि दर्ज करें।", "Payment ki amount daaliye.", "Amount is entered.")
            nodes.anyId("pin_input") && !nodes.enabled("pay_button") -> instruction(nodes, "pin_input", language, "Enter a made-up practice PIN. Never use a real PIN here.", "अभ्यास के लिए काल्पनिक PIN डालें। असली PIN न डालें।", "Practice ke liye bana hua PIN daaliye. Asli PIN mat daaliye.", "Pay becomes available.")
            nodes.anyId("pay_button") -> instruction(nodes, "pay_button", language, "Review the synthetic details, then tap Pay to finish this practice. No money moves.", "काल्पनिक जानकारी जाँचकर अभ्यास पूरा करने के लिए Pay दबाएँ। पैसे नहीं कटेंगे।", "Practice details check karke Pay tap kijiye. Paise nahi katenge.", "Show payment success confirmation.")
            nodes.anyId("electricity_biller") -> billerInstruction(goal, nodes, language)
            nodes.anyId("recharge_bills") -> instruction(nodes, "recharge_bills", language,
                if (previousFailed) "That’s alright. Tap Recharge & Pay Bills, the green tile at the top." else "Tap Recharge & Pay Bills, the green tile at the top.",
                if (previousFailed) "कोई बात नहीं। ऊपर हरे Recharge & Pay Bills टाइल को दबाएं।" else "ऊपर हरे Recharge & Pay Bills टाइल को दबाएं।",
                if (previousFailed) "Koi baat nahi. Upar green Recharge & Pay Bills tile tap kijiye." else "Upar green Recharge & Pay Bills tile tap kijiye.",
                "Biller choices appear.")
            else -> GuideStep(
                speechText = pick(language, "Open the Demo Bill Pay app and I’ll show the next step.", "डेमो बिल भुगतान ऐप खोलें, फिर मैं अगला कदम बताऊंगा।", "Demo Bill Pay app kholo, phir main agla step bataunga."),
                language = language, target = null, expectedOutcome = "Demo bill pay home screen is visible.", goalComplete = false
            )
        }
        return pick
    }

    private fun billerInstruction(goal: String, nodes: List<UiNode>, language: String): GuideStep = when {
        goal.contains("water", ignoreCase = true) || goal.contains("पानी") -> instruction(nodes, "water_biller", language, "Tap Water to pay your water bill.", "पानी का बिल भरने के लिए Water दबाएं।", "Water bill pay karne ke liye Water tap kijiye.", "Water bill form opens.")
        goal.contains("dth", ignoreCase = true) || goal.contains("tv", ignoreCase = true) || goal.contains("recharge", ignoreCase = true) -> instruction(nodes, "dth_biller", language, "Tap DTH to recharge your TV.", "TV रिचार्ज करने के लिए DTH दबाएं।", "TV recharge karne ke liye DTH tap kijiye.", "DTH recharge form opens.")
        else -> instruction(nodes, "electricity_biller", language, "Tap Electricity to choose your biller.", "बिजली बिलर चुनने के लिए Electricity दबाएं।", "Apna biller chunne ke liye Electricity tap kijiye.", "Electricity bill form opens.")
    }

    private fun List<UiNode>.anyId(id: String) = any { it.resourceId?.endsWith("/$id") == true }
    private fun List<UiNode>.hasValue(id: String) = firstOrNull { it.resourceId?.endsWith("/$id") == true }?.hasValue == true
    private fun List<UiNode>.enabled(id: String) = firstOrNull { it.resourceId?.endsWith("/$id") == true }?.isEnabled == true
    private fun serviceName(nodes: List<UiNode>) = when {
        nodes.containsText("Water bill") -> "water"
        nodes.containsText("DTH recharge") -> "DTH"
        else -> "electricity"
    }
    private fun serviceNameHindi(nodes: List<UiNode>) = when {
        nodes.containsText("Water bill") -> "पानी"
        nodes.containsText("DTH recharge") -> "DTH"
        else -> "बिजली"
    }
    private fun serviceNameHinglish(nodes: List<UiNode>) = when {
        nodes.containsText("Water bill") -> "water"
        nodes.containsText("DTH recharge") -> "DTH"
        else -> "electricity"
    }
    private fun List<UiNode>.containsText(value: String) = any { it.text?.contains(value, ignoreCase = true) == true || it.description?.contains(value, ignoreCase = true) == true }
    private fun instruction(nodes: List<UiNode>, id: String, language: String, english: String, hindi: String, hinglish: String = english, expected: String): GuideStep {
        val node = nodes.first { it.resourceId?.endsWith("/$id") == true }
        return GuideStep(pick(language, english, hindi, hinglish), language, GuideTarget(node.bounds, node.resourceId, id), expected, false)
    }
    private fun complete(language: String) = GuideStep(
        pick(language, "Practice complete. No real payment was made.", "अभ्यास पूरा हुआ। कोई असली भुगतान नहीं हुआ।", "Practice poori hui. Koi asli payment nahi hui."),
        language, null, "Payment success screen is visible.", true
    )
    private fun pick(language: String, english: String, hindi: String, hinglish: String) = when (language) {
        "hi-IN" -> hindi
        "hinglish" -> hinglish
        else -> english
    }
}
