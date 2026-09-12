package com.saathi

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*

/** A safe, native demo surface: no money moves and its PIN input is a real password node. */
class DemoBillPayActivity : Activity() {
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun showHome() {
        base("Welcome back")
        text("A familiar place for everyday payments", 16, Color.DKGRAY)
        tile("Recharge & Pay Bills", "Mobile, electricity, water and more", R.id.recharge_bills, Color.rgb(19, 110, 91)) { showBillers() }
        tile("Send Money", "Transfer to a contact", R.id.send_money, Color.rgb(34, 91, 157)) { toast("No change made - Saathi will gently redirect you.") }
        tile("Scan QR", "Pay at a shop", R.id.scan_qr, Color.rgb(121, 78, 165)) { toast("No change made - Saathi will gently redirect you.") }
        tile("History", "Your recent activity", R.id.history, Color.rgb(91, 100, 110)) { toast("No change made - Saathi will gently redirect you.") }
    }
    private fun showBillers() {
        base("Pay bills")
        text("Choose a category", 16, Color.DKGRAY)
        tile("Electricity", "Pay your power bill", R.id.electricity_biller, Color.rgb(248, 177, 42)) { showForm() }
        tile("Water", "Pay your water bill", R.id.water_biller, Color.rgb(74, 159, 208)) { toast("For this demo, choose Electricity.") }
        tile("DTH", "Recharge your TV", R.id.dth_biller, Color.rgb(151, 99, 192)) { toast("For this demo, choose Electricity.") }
    }
    private fun showForm() {
        base("Electricity bill")
        text("Maharashtra State Electricity Board", 16, Color.DKGRAY)
        val account = input("Electricity account number", R.id.account_input, InputType.TYPE_CLASS_NUMBER)
        val amount = input("Amount (₹)", R.id.amount_input, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val pin = input("PIN", R.id.pin_input, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        val privacy = TextView(this).apply {
            text = "🔒 Your PIN is hidden from Saathi and never sent to AI."
            setTextColor(Color.rgb(169, 33, 48))
            textSize = 14f
            setPadding(0, dp(4), 0, dp(8))
        }
        content.addView(privacy)

        val pay = Button(this).apply {
            id = R.id.pay_button
            text = "Pay securely"
            isAllCaps = false
            isEnabled = false
            setOnClickListener { showSuccess() }
        }
        content.addView(pay, wide().apply { topMargin = dp(12) })
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { pay.isEnabled = account.text.isNotBlank() && amount.text.isNotBlank() && pin.text.isNotBlank() }
            override fun afterTextChanged(s: Editable?) = Unit
        }
        account.addTextChangedListener(watcher)
        amount.addTextChangedListener(watcher)
        pin.addTextChangedListener(watcher)
    }
    private fun showSuccess() {
        base("Payment successful")
        text("✓", 72, Color.rgb(20, 108, 90)).apply { gravity = Gravity.CENTER }
        val title = text("Your electricity bill has been paid", 24, Color.rgb(20, 108, 90))
        title.id = R.id.success_title
        text("This is a safe demo. No real payment was made.", 16, Color.DKGRAY)
        content.addView(Button(this).apply {
            text = "Back to home"
            isAllCaps = false
            setOnClickListener { showHome() }
        }, wide().apply { topMargin = dp(20) })
    }
    private fun base(title: String) {
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(40), dp(22), dp(26))
            setBackgroundColor(Color.rgb(249, 250, 252))
        }
        setContentView(ScrollView(this).apply { addView(content) })
        text(title, 30, Color.rgb(22, 39, 54))
    }
    private fun tile(title: String, subtitle: String, id: Int, color: Int, click: () -> Unit) {
        val box = LinearLayout(this).apply {
            this.id = id
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            setBackgroundColor(color)
            contentDescription = title
            isClickable = true
            isFocusable = true
            setOnClickListener { click() }
        }
        box.addView(TextView(this).apply { text = title; textSize = 20f; setTextColor(Color.WHITE); setTypeface(null, 1) })
        box.addView(TextView(this).apply { text = subtitle; textSize = 14f; setTextColor(Color.WHITE); setPadding(0, dp(4), 0, 0) })
        content.addView(box, wide().apply { topMargin = dp(14) })
    }
    private fun input(hint: String, id: Int, inputType: Int): EditText {
        val view = EditText(this).apply {
            this.id = id
            this.hint = hint
            this.inputType = inputType
            contentDescription = hint
        }
        content.addView(view, wide().apply { topMargin = dp(12) })
        return view
    }

    private fun text(value: String, size: Int, color: Int): TextView = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
        setPadding(0, dp(4), 0, dp(8))
        content.addView(this)
    }
    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
    private fun wide() = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
