package com.saathi.core

import android.graphics.Rect

data class UiNode(
    val bounds: Rect,
    val text: String?,
    val description: String?,
    val hint: String?,
    val resourceId: String?,
    val className: String?,
    val isPassword: Boolean,
    val isEnabled: Boolean,
    val isClickable: Boolean,
    val isSensitive: Boolean = false
) {
    fun fingerprintPart() = listOf(resourceId, text, description, className, isEnabled, bounds.toShortString()).joinToString("|")
}

data class GuideTarget(val bounds: Rect, val resourceId: String? = null, val description: String)

data class GuideStep(
    val speechText: String,
    val language: String,
    val target: GuideTarget?,
    val expectedOutcome: String,
    val goalComplete: Boolean,
    val correctionNote: String? = null,
    val action: GuideAction = GuideAction.GUIDE
)

enum class GuideAction { GUIDE, REFUSE }

data class StepHistory(val instruction: String, val expectedOutcome: String)
