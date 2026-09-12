package com.saathi.intake

/**
 * Fast, device-local preparation for the apps we can identify confidently. This provides a
 * useful starting checklist without sending the user's task to a third party. It does not
 * claim to inspect a person's account or browse a live service; the visible screen remains
 * the source of truth for each actual instruction.
 */
object FlowResearchCoordinator {
    data class Briefing(
        val source: String,
        val summary: String,
        val likelySteps: List<String>
    )

    fun prepare(brief: TaskBrief): Briefing {
        val app = brief.appOrWebsite.orEmpty().lowercase()
        val knownFlow = when {
            app.contains("provider") || app.contains("website") -> listOf("Find the provider's official bill-payment page", "Review account and amount details", "Complete final payment approval yourself")
            app.contains("irctc") -> listOf("Choose journey details", "Review train and passenger details", "Confirm the booking yourself")
            app.contains("demo bill") -> listOf("Open Recharge & Pay Bills", "Choose Electricity, Water, or DTH", "Enter details and confirm yourself")
            else -> emptyList()
        }

        return if (knownFlow.isNotEmpty()) {
            Briefing("On-device app profile", "I prepared a short checklist for ${brief.appOrWebsite}. I will still follow the live screen, not a fixed script.", knownFlow)
        } else {
            Briefing("Live screen preparation", "I will use the app's visible screen and safe on-screen context to adapt each step as you go.", emptyList())
        }
    }
}
