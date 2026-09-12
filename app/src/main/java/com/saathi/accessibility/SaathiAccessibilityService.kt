package com.saathi.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.saathi.orchestrator.SaathiSession
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

class SaathiAccessibilityService : AccessibilityService() {
    private val nodeExecutor = Executors.newSingleThreadExecutor()
    private val lastQueuedAt = AtomicLong(0)

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!SaathiSession.isActive()) return
        if (event.eventType !in setOf(
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_VIEW_CLICKED,
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            )) return
        SaathiSession.onPackageChanged(event.packageName?.toString())
        val root = rootInActiveWindow ?: return
        val now = System.currentTimeMillis()
        if (now - lastQueuedAt.getAndSet(now) < 150L) { runCatching { root.recycle() }; return }
        val packageName = event.packageName?.toString()
        // Accessibility callbacks arrive on the main thread. Copy then process the tree off it.
        val snapshot = AccessibilityNodeInfo.obtain(root)
        runCatching { root.recycle() }
        nodeExecutor.execute {
            try { SaathiSession.onScreenChanged(NodeMasker.flatten(snapshot), packageName) }
            finally { runCatching { snapshot.recycle() } }
        }
    }

    override fun onDestroy() { nodeExecutor.shutdownNow(); super.onDestroy() }
    override fun onInterrupt() = Unit
}
