package com.saathi.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Base64
import android.view.WindowManager
import com.saathi.core.UiNode
import java.io.ByteArrayOutputStream

/** Captures only after Android's explicit MediaProjection consent; blackouts occur before encoding. */
object ScreenshotCapture {
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var display: android.hardware.display.VirtualDisplay? = null

    fun consentIntent(context: Context): Intent =
        (context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent()

    fun initialize(context: Context, resultCode: Int, data: Intent) {
        stop()
        val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = manager.getMediaProjection(resultCode, data)
        val metrics = context.resources.displayMetrics
        reader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, android.graphics.PixelFormat.RGBA_8888, 2)
        display = projection?.createVirtualDisplay(
            "Saathi privacy-filtered capture", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader?.surface, null, null
        )
    }

    fun maskedJpegBase64(nodes: List<UiNode>): String? {
        val image = reader?.acquireLatestImage() ?: return null
        return try {
            val plane = image.planes.first()
            val width = image.width
            val height = image.height
            val rowPadding = plane.rowStride - plane.pixelStride * width
            val raw = Bitmap.createBitmap(width + rowPadding / plane.pixelStride, height, Bitmap.Config.ARGB_8888)
            raw.copyPixelsFromBuffer(plane.buffer)
            val copy = Bitmap.createBitmap(raw, 0, 0, width, height)
            raw.recycle()
            val canvas = Canvas(copy)
            nodes.filter { it.isSensitive }.forEach { canvas.drawRect(it.bounds, android.graphics.Paint().apply { color = Color.BLACK }) }
            val scaled = if (copy.width > 800) Bitmap.createScaledBitmap(copy, 800, copy.height * 800 / copy.width, true) else copy
            val output = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 78, output)
            if (scaled !== copy) scaled.recycle()
            copy.recycle()
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        } finally { image.close() }
    }

    fun stop() {
        display?.release(); display = null
        reader?.close(); reader = null
        projection?.stop(); projection = null
    }
}
