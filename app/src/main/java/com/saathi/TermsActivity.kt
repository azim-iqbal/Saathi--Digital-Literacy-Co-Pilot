package com.saathi

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class TermsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(44, 48, 44, 48) }
        content.addView(TextView(this).apply { text = "Where Saathi can guide"; textSize = 27f; setTextColor(Color.rgb(11, 107, 90)); setTypeface(null, 1) })
        content.addView(TextView(this).apply {
            text = "Saathi gives visual guidance only where Android exposes safe screen information. It cannot bypass an app’s secure screen, capture block, login protection, or payment controls.\n\nSupported best: official websites in Chrome, Samsung Internet, and other browsers; standard forms; rail and airline websites.\n\nNot available inside restricted payment apps such as Google Pay, PhonePe, Paytm, and similar apps that block screen guidance. Use their official provider website for bill details, then complete the final payment in your chosen payment app yourself.\n\nYou remain responsible for every booking, payment, upload, and confirmation. Saathi never enters PINs, OTPs, passwords, CVVs, or payment approvals.\n\nBackground guidance requires Accessibility to remain enabled. Voice guidance needs the microphone while the active session notification is visible."
            textSize = 16f; setTextColor(Color.DKGRAY); setLineSpacing(8f, 1f); setPadding(0, 28, 0, 0)
        })
        setContentView(ScrollView(this).apply { addView(content) })
    }
}
