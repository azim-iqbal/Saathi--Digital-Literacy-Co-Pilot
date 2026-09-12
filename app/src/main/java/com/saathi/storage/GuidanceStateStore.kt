package com.saathi.storage

import android.content.Context
import com.saathi.intake.TaskBrief
import com.saathi.intake.TaskKind
import com.saathi.language.GuidanceLanguage

/** Stores only structural session state; no screenshots, credentials, PINs, or spoken audio. */
class GuidanceStateStore(context: Context) {
    private val preferences = context.getSharedPreferences("saathi_guidance_state", Context.MODE_PRIVATE)

    fun save(brief: TaskBrief, language: GuidanceLanguage, voiceEnabled: Boolean) {
        preferences.edit()
            .putBoolean("active", true)
            .putString("goal", brief.goal)
            .putString("kind", brief.kind.name)
            .putString("destination", brief.appOrWebsite)
            .putString("language", language.storageValue)
            .putBoolean("voice", voiceEnabled)
            .apply()
    }

    fun restore(): TaskBrief? = runCatching {
        if (!preferences.getBoolean("active", false)) return null
        TaskBrief(
            kind = TaskKind.valueOf(preferences.getString("kind", null) ?: return null),
            goal = preferences.getString("goal", null) ?: return null,
            appOrWebsite = preferences.getString("destination", null)
        )
    }.getOrNull()

    fun clear() = preferences.edit().clear().apply()
}
