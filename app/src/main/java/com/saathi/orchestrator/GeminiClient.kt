package com.saathi.orchestrator

import android.graphics.Rect
import com.saathi.BuildConfig
import com.saathi.core.GuideStep
import com.saathi.core.GuideTarget
import com.saathi.core.StepHistory
import com.saathi.core.UiNode
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiClient {
    companion object {
        // NEVER enable billing on this Google Cloud project: it removes the free-tier-only safety boundary.
        // Keep this client on a Flash-class model; BuildConfig defaults to gemini-2.0-flash.
        private const val SYSTEM_PROMPT = """You are Saathi, a patient guide helping users complete a concrete task on their current screen: bill payment, UPI/payment navigation, a form, ticket booking, a government portal, or essential settings. Saathi ONLY observes, speaks, and highlights. It never taps, types, submits, or performs gestures. Never ask for, infer, or repeat password, PIN, OTP, CVV, or MPIN contents. If the goal, screen, or app is outside this scope, or appears to involve gambling, adult content, or unlicensed lending, respond with action REFUSE and no target. Otherwise use action GUIDE and decide the SINGLE next physical action. Respond only with JSON: {\"action\":\"GUIDE\"|\"REFUSE\",\"speech_text\":string,\"language\":string,\"target_element\":{\"bounds\":[x1,y1,x2,y2],\"resource_id\":string|null,\"description\":string}|null,\"expected_outcome\":string,\"goal_complete\":boolean,\"correction_note\":string|null}."""
    }

    fun available() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    fun request(
        goal: String,
        language: String,
        nodes: List<UiNode>,
        history: List<StepHistory>,
        screenshotBase64: String?,
        previousFailed: Boolean
    ): GuideStep? {
        if (!available() || !BuildConfig.GEMINI_MODEL.contains("flash", ignoreCase = true)) return null

        return runCatching {
            val prompt = buildPrompt(goal, language, nodes, history, previousFailed)
            val parts = JSONArray().put(JSONObject().put("text", prompt.toString()))
            screenshotBase64?.let { image ->
                parts.put(JSONObject().put("inline_data", JSONObject().put("mime_type", "image/jpeg").put("data", image)))
            }

            val payload = createPayload(parts)
            val connection = openConnection()
            connection.outputStream.use { it.write(payload.toString().toByteArray()) }
            check(connection.responseCode in 200..299) { "Gemini response ${connection.responseCode}" }
            val reply = connection.inputStream.bufferedReader().use { it.readText() }
            parse(extractResponseJson(reply))
        }.getOrNull()
    }

    private fun buildPrompt(
        goal: String,
        language: String,
        nodes: List<UiNode>,
        history: List<StepHistory>,
        previousFailed: Boolean
    ) = JSONObject().apply {
        put("goal", goal)
        put("language_preference", language)
        put("previous_step_failed", previousFailed)
        put("node_tree", JSONArray().apply { nodes.forEach { put(it.toJson()) } })
        put("step_history", JSONArray().apply {
            history.forEach { entry -> put(JSONObject().put("instruction", entry.instruction).put("expected_outcome", entry.expectedOutcome)) }
        })
    }

    private fun UiNode.toJson() = JSONObject().apply {
        put("bounds", JSONArray(listOf(bounds.left, bounds.top, bounds.right, bounds.bottom)))
        put("text", text)
        put("content_description", description)
        put("hint", hint)
        put("resource_id", resourceId)
        put("class_name", className)
        put("is_password", isPassword)
        put("is_enabled", isEnabled)
    }

    private fun createPayload(parts: JSONArray) = JSONObject()
        .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT))))
        .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", parts)))
        .put("generationConfig", JSONObject().put("responseMimeType", "application/json").put("maxOutputTokens", 500))

    private fun openConnection(): HttpURLConnection =
        (URL("https://generativelanguage.googleapis.com/v1beta/models/${BuildConfig.GEMINI_MODEL}:generateContent?key=${BuildConfig.GEMINI_API_KEY}").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10_000
            readTimeout = 15_000
        }

    private fun extractResponseJson(response: String): JSONObject {
        val body = JSONObject(response)
        return JSONObject(
            body.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        )
    }

    private fun parse(json: JSONObject): GuideStep {
        val rawTarget = json.optJSONObject("target_element")
        val target = rawTarget?.let {
            val b = it.getJSONArray("bounds")
            GuideTarget(Rect(b.getInt(0), b.getInt(1), b.getInt(2), b.getInt(3)), it.optString("resource_id").ifBlank { null }, it.optString("description"))
        }
        return GuideStep(
            json.getString("speech_text"), json.getString("language"), target,
            json.getString("expected_outcome"), json.getBoolean("goal_complete"),
            json.optString("correction_note").ifBlank { null },
            if (json.optString("action", "GUIDE") == "REFUSE") com.saathi.core.GuideAction.REFUSE else com.saathi.core.GuideAction.GUIDE
        )
    }
}
