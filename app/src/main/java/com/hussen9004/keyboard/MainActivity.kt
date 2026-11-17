package com.hussen9004.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(28), dp(24), dp(28))
            setBackgroundColor(0xFFF7F5FA.toInt())
        }

        content.addView(TextView(this).apply {
            text = "لوحة المفاتيح العربية"
            textSize = 26f
            setTextColor(0xFF211B2B.toInt())
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })

        content.addView(TextView(this).apply {
            text = "لوحة مفاتيح بسيطة تدعم العربية والإنجليزية. فعّلها من الإعدادات ثم اخترها من قائمة لوحات المفاتيح."
            textSize = 16f
            setTextColor(0xFF625D68.toInt())
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(24) })

        content.addView(actionButton("1. تفعيل لوحة المفاتيح") {
            startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS"))
        })
        content.addView(actionButton("2. اختيار لوحة المفاتيح") {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(12) })

        setContentView(content)
    }

    private fun actionButton(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        textSize = 16f
        setOnClickListener { onClick() }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
