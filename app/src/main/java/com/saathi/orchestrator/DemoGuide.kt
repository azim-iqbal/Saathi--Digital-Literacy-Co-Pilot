package com.saathi.orchestrator

import android.graphics.Rect
import com.saathi.core.GuideStep
import com.saathi.core.GuideTarget
import com.saathi.core.UiNode

/** Reliable local guide used for the scripted mock flow and as an API failure fallback. */
object DemoGuide {
    fun next(nodes: List<UiNode>, language: String, previousFailed: Boolean): GuideStep {
        val pick = when {
            nodes.anyId("success_title") -> complete(language)
            nodes.anyId("account_input") && nodes.textOf("account_input").isNullOrBlank() -> instruction(nodes, "account_input", language, "Enter your electricity account number.", "अपना बिजली खाता नंबर दर्ज करें।", "Apna electricity account number daaliye.", "Account number is entered.")
            nodes.anyId("amount_input") && nodes.textOf("amount_input").isNullOrBlank() -> instruction(nodes, "amount_input", language, "Enter the bill amount.", "बिल की राशि दर्ज करें।", "Bill ki amount daaliye.", "Amount is entered.")
            nodes.anyId("pin_input") && !nodes.enabled("pay_button") -> instruction(nodes, "pin_input", language, "Enter your PIN carefully. Saathi cannot see it.", "अपना PIN सावधानी से दर्ज करें। Saathi इसे नहीं देख सकता।", "Apna PIN dhyan se daaliye. Saathi ise nahi dekh sakta.", "Pay becomes available.")
            nodes.anyId("pay_button") -> instruction(nodes, "pay_button", language, "Tap Pay to complete this bill payment.", "अब भुगतान पूरा करने के लिए Pay दबाएं।", "Payment poora karne ke liye Pay tap kijiye.", "Show payment success confirmation.")
            nodes.anyId("electricity_biller") -> instruction(nodes, "electricity_biller", language, "Tap Electricity to choose your biller.", "बिजली बिलर चुनने के लिए Electricity दबाएं।", "Apna biller chunne ke liye Electricity tap kijiye.", "Electricity bill form opens.")
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

    private fun List<UiNode>.anyId(id: String) = any { it.resourceId?.endsWith("/$id") == true }
    private fun List<UiNode>.textOf(id: String) = firstOrNull { it.resourceId?.endsWith("/$id") == true }?.text
    private fun List<UiNode>.enabled(id: String) = firstOrNull { it.resourceId?.endsWith("/$id") == true }?.isEnabled == true
    private fun instruction(nodes: List<UiNode>, id: String, language: String, english: String, hindi: String, hinglish: String = english, expected: String): GuideStep {
        val node = nodes.first { it.resourceId?.endsWith("/$id") == true }
        return GuideStep(pick(language, english, hindi, hinglish), language, GuideTarget(node.bounds, node.resourceId, id), expected, false)
    }
    private fun complete(language: String) = GuideStep(
        pick(language, "Your payment is complete. Well done!", "भुगतान पूरा हो गया। बहुत बढ़िया!", "Payment poora ho gaya. Bahut badhiya!"),
        language, null, "Payment success screen is visible.", true
    )
    private fun pick(language: String, english: String, hindi: String, hinglish: String) = when (language) {
        "hi-IN" -> hindi
        "hinglish" -> hinglish
        else -> english
    }
}
