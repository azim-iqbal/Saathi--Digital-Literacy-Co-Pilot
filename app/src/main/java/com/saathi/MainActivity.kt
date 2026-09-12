package com.saathi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import com.saathi.capture.ScreenshotCapture
import com.saathi.orchestrator.SaathiSession
import com.saathi.language.GuidanceCopy
import com.saathi.language.GuidanceLanguage
import com.saathi.speech.SpeechInputManager

class MainActivity : Activity() {
    private companion object {
        const val SCREEN_CAPTURE_REQUEST = 42
        const val MICROPHONE_PERMISSION_REQUEST = 8
        const val PREFERENCES_NAME = "saathi_preferences"
        const val LANGUAGE_PREFERENCE = "guidance_language"
        const val LANGUAGE_PICKER_SHOWN = "language_picker_shown"
    }

    private lateinit var goalInput: EditText
    private lateinit var languagePicker: Spinner
    private lateinit var status: TextView
    private var speechInput: SpeechInputManager? = null
    private val preferences by lazy { getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = createPage()
        setContentView(ScrollView(this).apply { addView(page) })

        page.addView(title("Saathi"))
        page.addView(subtitle("A patient guide for every tap. Saathi highlights and explains; you stay in control."))
        page.addView(label("What would you like to do?"))
        goalInput = EditText(this).apply {
            hint = "e.g. Pay my electricity bill"
            setText("Pay my electricity bill")
            contentDescription = "Your goal"
        }
        page.addView(goalInput, wide())

        page.addView(label("Language"))
        val initialLanguage = savedOrSystemLanguage()
        languagePicker = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("English", "हिंदी", "Hinglish"))
            setSelection(initialLanguage.ordinal)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    saveLanguage(GuidanceLanguage.entries[position])
                }
            }
        }
        page.addView(languagePicker, wide())

        page.addView(button("Language settings") { showLanguagePicker(force = true) }, spaced())
        page.addView(button("Use voice to say my goal") { beginVoice() }, spaced())
        page.addView(label("Set up"))
        page.addView(button("1. Grant overlay permission") { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }, spaced())
        page.addView(button("2. Enable Saathi Accessibility") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, spaced())
        page.addView(button("3. Allow privacy-filtered screen capture (optional)") {
            startActivityForResult(ScreenshotCapture.consentIntent(this), SCREEN_CAPTURE_REQUEST)
        }, spaced())
        val startButton = button("Start guiding") { startGuiding() }.apply {
            setBackgroundColor(Color.rgb(20, 108, 90))
            setTextColor(Color.WHITE)
        }
        page.addView(startButton, spaced())
        page.addView(button("Open Demo Bill Pay") { startActivity(Intent(this, DemoBillPayActivity::class.java)) }, spaced())

        status = TextView(this).apply {
            setPadding(0, dp(18), 0, 0)
            textSize = 15f
            setTextColor(Color.DKGRAY)
        }
        page.addView(status)
        refreshStatus()

        if (!preferences.getBoolean(LANGUAGE_PICKER_SHOWN, false)) {
            languagePicker.post { showLanguagePicker(force = false) }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshStatus()
    }

    private fun startGuiding() {
        val goal = goalInput.text.toString().trim().ifEmpty { "Pay my electricity bill" }
        val language = languageMode()
        val result = SaathiSession.start(applicationContext, goal, language)
        status.text = if (result.decision == com.saathi.guardrails.GuardrailDecision.REFUSE) {
            GuidanceCopy.guardrailRedirect(language)
        } else {
            "Guidance is on. Open Demo Bill Pay, then follow the green circle. Saathi never taps for you."
        }
    }

    private fun beginVoice() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MICROPHONE_PERMISSION_REQUEST)
            return
        }

        val language = languageMode()
        speechInput?.destroy()
        speechInput = SpeechInputManager(
            this,
            { text -> goalInput.setText(text) },
            { Toast.makeText(this, GuidanceCopy.lowConfidence(language), Toast.LENGTH_LONG).show() },
            { Toast.makeText(this, GuidanceCopy.lowConfidence(language), Toast.LENGTH_LONG).show() }
        )
        speechInput?.listen(language.sttTag)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == MICROPHONE_PERMISSION_REQUEST && results.firstOrNull() == PackageManager.PERMISSION_GRANTED) beginVoice()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != SCREEN_CAPTURE_REQUEST || resultCode != RESULT_OK || data == null) return

        ScreenshotCapture.initialize(this, resultCode, data)
        Toast.makeText(this, "Screen capture is ready; sensitive regions will be blacked out first.", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        speechInput?.destroy()
        super.onDestroy()
    }

    private fun savedOrSystemLanguage(): GuidanceLanguage =
        if (preferences.contains(LANGUAGE_PREFERENCE)) {
            GuidanceLanguage.fromStorage(preferences.getString(LANGUAGE_PREFERENCE, null))
        } else {
            GuidanceLanguage.initialForSystemLocale()
        }

    private fun languageMode(): GuidanceLanguage = GuidanceLanguage.entries.getOrElse(languagePicker.selectedItemPosition) {
        savedOrSystemLanguage()
    }

    private fun saveLanguage(language: GuidanceLanguage) {
        preferences.edit().putString(LANGUAGE_PREFERENCE, language.storageValue).apply()
    }

    private fun showLanguagePicker(force: Boolean) {
        val current = languageMode().ordinal
        android.app.AlertDialog.Builder(this)
            .setTitle("Choose your guidance language")
            .setSingleChoiceItems(arrayOf("English", "हिंदी", "Hinglish"), current) { dialog, which ->
                languagePicker.setSelection(which)
                saveLanguage(GuidanceLanguage.entries[which])
                preferences.edit().putBoolean(LANGUAGE_PICKER_SHOWN, true).apply()
                dialog.dismiss()
            }
            .setNegativeButton(if (force) "Cancel" else "Use device default") { dialog, _ ->
                if (!force) {
                    val initial = GuidanceLanguage.initialForSystemLocale()
                    languagePicker.setSelection(initial.ordinal)
                    saveLanguage(initial)
                    preferences.edit().putBoolean(LANGUAGE_PICKER_SHOWN, true).apply()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun refreshStatus() {
        val overlayStatus = if (Settings.canDrawOverlays(this)) "ready" else "needs permission"
        status.text = "Overlay: $overlayStatus  •  Accessibility: enable it in Settings to begin live guidance."
    }

    private fun createPage() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(24), dp(36), dp(24), dp(28))
        setBackgroundColor(Color.rgb(247, 248, 244))
    }

    private fun title(value: String) = TextView(this).apply { text = value; textSize = 36f; setTextColor(Color.rgb(20, 108, 90)); setTypeface(null, 1) }
    private fun subtitle(value: String) = TextView(this).apply { text = value; textSize = 18f; setPadding(0, dp(8), 0, dp(24)) }
    private fun button(text: String, action: () -> Unit) = Button(this).apply { this.text = text; isAllCaps = false; setOnClickListener { action() } }
    private fun label(text: String) = TextView(this).apply { this.text = text; textSize = 16f; setTypeface(null, 1); setPadding(0, dp(18), 0, dp(5)) }
    private fun wide() = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    private fun spaced() = wide().apply { topMargin = dp(8) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
