package com.saathi.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.saathi.orchestrator.SaathiSession
import java.util.concurrent.Executors

class SaathiAccessibilityService : AccessibilityService() {
    private val nodeExecutor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private var pending = false
    private var copying = false
    private var dirty = false

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!SaathiSession.isActive()) return
        if (getSystemService(KeyguardManager::class.java).isKeyguardLocked) { SaathiSession.stop(); return }
        if (event.eventType !in setOf(
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_WINDOWS_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED, AccessibilityEvent.TYPE_VIEW_SCROLLED,
                AccessibilityEvent.TYPE_VIEW_CLICKED, AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            )) return
        SaathiSession.invalidateScreen()
        dirty = true
        scheduleCopy()
    }

    private fun scheduleCopy() {
        if (pending || copying || !SaathiSession.isActive()) return
        pending = true
        // Fixed delay from the first event: continuous events cannot starve snapshots.
        handler.postDelayed({
            pending = false
            if (!SaathiSession.isActive()) return@postDelayed
            dirty = false
            val root = rootInActiveWindow ?: return@postDelayed
            val ticket = SaathiSession.beginObservation(root.packageName?.toString().orEmpty(), root.windowId)
            val snapshot = AccessibilityNodeInfo.obtain(root)
            @Suppress("DEPRECATION") root.recycle()
            if (ticket == null) { @Suppress("DEPRECATION") snapshot.recycle(); return@postDelayed }
            copying = true
            nodeExecutor.execute {
                try { SaathiSession.onScreenChanged(NodeMasker.flatten(snapshot), ticket) }
                finally {
                    @Suppress("DEPRECATION") snapshot.recycle()
                    handler.post { copying = false; if (dirty) scheduleCopy() }
                }
            }
        }, 150)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        SaathiSession.stop()
        nodeExecutor.shutdownNow()
        super.onDestroy()
    }
    override fun onInterrupt() { SaathiSession.stop() }
}
