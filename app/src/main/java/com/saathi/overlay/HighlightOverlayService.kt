package com.saathi.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.IBinder
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator

class HighlightOverlayService : Service() {
    private var overlay: GuidanceOverlay? = null
    private var windowManager: WindowManager? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) return START_NOT_STICKY
        val target = intent?.rectExtra(EXTRA_TARGET)
        val sensitive = intent?.rectListExtra(EXTRA_SENSITIVE).orEmpty()
        val complete = intent?.getBooleanExtra(EXTRA_COMPLETE, false) ?: false
        if (overlay == null) {
            overlay = GuidanceOverlay(this)
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP or Gravity.START }
            windowManager?.addView(overlay, params)
        }
        overlay?.setState(target, sensitive, complete)
        return START_NOT_STICKY
    }
    override fun onDestroy() { overlay?.let { windowManager?.removeView(it) }; overlay = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val EXTRA_TARGET = "target"; private const val EXTRA_SENSITIVE = "sensitive"; private const val EXTRA_COMPLETE = "complete"
        fun intent(context: Context, target: Rect?, sensitive: List<Rect>, complete: Boolean) = Intent(context, HighlightOverlayService::class.java).apply {
            putExtra(EXTRA_TARGET, target); putParcelableArrayListExtra(EXTRA_SENSITIVE, ArrayList(sensitive)); putExtra(EXTRA_COMPLETE, complete)
        }
    }
}

private fun Intent.rectExtra(key: String): Rect? = if (Build.VERSION.SDK_INT >= 33) {
    getParcelableExtra(key, Rect::class.java)
} else {
    @Suppress("DEPRECATION") getParcelableExtra(key)
}

private fun Intent.rectListExtra(key: String): ArrayList<Rect>? = if (Build.VERSION.SDK_INT >= 33) {
    getParcelableArrayListExtra(key, Rect::class.java)
} else {
    @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

private class GuidanceOverlay(context: Context) : android.view.View(context) {
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(20, 108, 90); style = Paint.Style.STROKE; strokeWidth = 5f }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(28, 20, 108, 90); style = Paint.Style.FILL }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 24f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
    private val labelBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(169, 33, 48) }
    private var target: Rect? = null; private var sensitive = emptyList<Rect>(); private var complete = false; private var pulse = 1f
    private val animator = ValueAnimator.ofFloat(0.92f, 1.10f).apply { duration = 760; repeatCount = ValueAnimator.INFINITE; repeatMode = ValueAnimator.REVERSE; interpolator = LinearInterpolator(); addUpdateListener { pulse = it.animatedValue as Float; invalidate() }; start() }
    fun setState(newTarget: Rect?, newSensitive: List<Rect>, isComplete: Boolean) { target = newTarget; sensitive = newSensitive; complete = isComplete; invalidate() }
    override fun onDetachedFromWindow() { animator.cancel(); super.onDetachedFromWindow() }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        target?.let { rect ->
            drawTargetAnnotation(canvas, rect)
        }
        sensitive.forEach { rect ->
            val badge = Rect(rect.left, (rect.top - 38).coerceAtLeast(0), (rect.left + 310).coerceAtMost(width), rect.top)
            canvas.drawRect(badge, labelBg); canvas.drawText("LOCKED - EXCLUDED FROM AI", badge.left + 10f, badge.bottom - 10f, label)
        }
        if (complete) { canvas.drawText("DONE", width / 2f - 45f, 96f, ring.apply { style = Paint.Style.FILL; textSize = 36f }); ring.style = Paint.Style.STROKE }
    }

    private fun drawTargetAnnotation(canvas: Canvas, target: Rect) {
        val inset = 8f * pulse
        val outline = RectF(target.left - inset, target.top - inset, target.right + inset, target.bottom + inset)
        canvas.drawRoundRect(outline, 24f, 24f, fill)
        canvas.drawRoundRect(outline, 24f, 24f, ring)

        val tagWidth = 118f
        val tagHeight = 38f
        val tagTop = (outline.top - tagHeight - 10f).coerceAtLeast(8f)
        val tag = RectF(outline.left, tagTop, outline.left + tagWidth, tagTop + tagHeight)
        canvas.drawRoundRect(tag, tagHeight / 2, tagHeight / 2, ring.apply { style = Paint.Style.FILL })
        canvas.drawText("NEXT STEP", tag.left + 14f, tag.bottom - 12f, label)
        ring.style = Paint.Style.STROKE
    }
}
