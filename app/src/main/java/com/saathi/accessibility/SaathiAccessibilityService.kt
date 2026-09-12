package com.saathi.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.saathi.orchestrator.SaathiSession

class SaathiAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!SaathiSession.isActive()) return
        val root = rootInActiveWindow ?: return
        SaathiSession.onScreenChanged(NodeMasker.flatten(root))
    }
    override fun onInterrupt() = Unit
}
