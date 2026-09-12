package com.saathi.orchestrator

import android.content.Context

/** Stores timestamps only - never prompts, screen content, audio, or API responses. */
class GeminiRateLimiter(context: Context) {
    private val prefs = context.getSharedPreferences("gemini_rate_limits", Context.MODE_PRIVATE)

    fun tryAcquire(nowMs: Long = System.currentTimeMillis()): Boolean {
        val minuteAgo = nowMs - 60_000
        val dayAgo = nowMs - 86_400_000
        val retained = prefs.getString("timestamps", "").orEmpty().split(',').mapNotNull(String::toLongOrNull).filter { it >= dayAgo }
        if (retained.count { it >= minuteAgo } >= MAX_CALLS_PER_MINUTE || retained.size >= MAX_CALLS_PER_DAY) return false
        prefs.edit().putString("timestamps", (retained + nowMs).joinToString(",")).apply()
        return true
    }

    companion object {
        const val MAX_CALLS_PER_MINUTE = 8
        const val MAX_CALLS_PER_DAY = 120
    }
}
