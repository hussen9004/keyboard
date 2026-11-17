package com.hussen9004.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    private val preferences by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
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
            text = "إعدادات لوحة مفاتيح عربية وإنجليزية مع أدوات سريعة."
            textSize = 16f
            setTextColor(0xFF625D68.toInt())
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(20) })

        content.addView(actionButton("تفعيل لوحة المفاتيح") {
            startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS"))
        })
        content.addView(actionButton("اختيار لوحة المفاتيح") {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(10) })

        content.addView(sectionTitle("الكتابة"), topMargin())
        content.addView(settingSwitch("التصحيح التلقائي المحلي", PREF_AUTOCORRECT, true))
        content.addView(settingSwitch("اهتزاز المفاتيح", PREF_HAPTICS, false))

        content.addView(sectionTitle("الخصوصية"), topMargin())
        content.addView(TextView(this).apply {
            text = "التصحيح يعمل بقائمة كلمات محفوظة داخل التطبيق. زر الحافظة لا يقرأ محتواها إلا عند ضغطك عليه، ولا تُحفظ عناصر الحافظة. لا توجد حاليًا خدمة ذكاء اصطناعي أو ترجمة متصلة بالإنترنت."
            textSize = 14f
            setTextColor(0xFF625D68.toInt())
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(0xFFEDEAF2.toInt())
        })

        content.addView(sectionTitle("قريبًا"), topMargin())
        content.addView(TextView(this).apply {
            text = "الترجمة والذكاء الاصطناعي والملصقات والنسخ الاحتياطي تحتاج اختيار مزوّد/تصميم آمن قبل تفعيلها."
            textSize = 14f
            setTextColor(0xFF625D68.toInt())
        })

        val scroll = ScrollView(this).apply { addView(content) }
        setContentView(scroll)
    }

    private fun settingSwitch(label: String, key: String, defaultValue: Boolean) = Switch(this).apply {
        text = label
        textSize = 15f
        setTextColor(0xFF211B2B.toInt())
        isChecked = preferences.getBoolean(key, defaultValue)
        setOnCheckedChangeListener { _, checked -> preferences.edit().putBoolean(key, checked).apply() }
        setPadding(0, dp(8), 0, dp(8))
    }

    private fun sectionTitle(title: String) = TextView(this).apply {
        text = title
        textSize = 19f
        setTextColor(0xFF51436C.toInt())
    }

    private fun topMargin() = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(24); bottomMargin = dp(8) }

    private fun actionButton(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        textSize = 16f
        isAllCaps = false
        setOnClickListener { onClick() }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val PREFS_NAME = "keyboard_settings"
        const val PREF_AUTOCORRECT = "autocorrect_enabled"
        const val PREF_HAPTICS = "haptics_enabled"
    }
}
