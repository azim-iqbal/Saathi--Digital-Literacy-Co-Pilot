package com.saathi.storage

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class StoredMessage(val text: String, val fromUser: Boolean)

/** A bounded on-device transcript. It is intentionally not synced or uploaded. */
class ConversationStore(context: Context) {
    private val preferences = context.getSharedPreferences("saathi_conversation", Context.MODE_PRIVATE)
    private val key = "messages"

    fun load(): List<StoredMessage> = runCatching {
        val items = JSONArray(preferences.getString(key, "[]"))
        List(items.length()) { index ->
            val item = items.getJSONObject(index)
            StoredMessage(item.getString("text"), item.getBoolean("fromUser"))
        }
    }.getOrDefault(emptyList())

    fun append(message: StoredMessage) {
        val values = JSONArray(preferences.getString(key, "[]"))
        while (values.length() >= MAX_MESSAGES) values.remove(0)
        values.put(JSONObject().put("text", message.text).put("fromUser", message.fromUser))
        preferences.edit().putString(key, values.toString()).apply()
    }

    fun clear() = preferences.edit().remove(key).apply()

    private companion object { const val MAX_MESSAGES = 80 }
}
