package com.saathi.orchestrator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Keeps at most three generic flow labels and timestamps. It deliberately does not cache
 * screen text, spoken goals, screenshots, voice data, PINs, or model responses.
 */
class GuidanceFlowCache(context: Context) {
    private val prefs = context.getSharedPreferences("completed_guidance_flows", Context.MODE_PRIVATE)
    fun markCompleted(category: String?) {
        val key = category ?: return
        val values = load().filterNot { it == key }.takeLast(2) + key
        prefs.edit().putString("categories", JSONArray(values).toString()).apply()
    }
    fun hasCompleted(category: String?) = category != null && load().contains(category)
    private fun load(): List<String> = runCatching {
        val data = JSONArray(prefs.getString("categories", "[]")); List(data.length()) { data.getString(it) }
    }.getOrDefault(emptyList())
}
