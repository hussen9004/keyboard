package com.hussen9004.keyboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout

class KeyboardService : InputMethodService() {
    private var isArabic = true
    private var isShifted = false

    private val arabicRows = listOf(
        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
        listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
        listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ")
    )
    private val englishRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )

    override fun onCreateInputView(): View = buildKeyboard()

    private fun buildKeyboard(): View {
        val keyboard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(6), dp(4), dp(6))
            setBackgroundColor(Color.rgb(232, 230, 237))
        }
        val rows = if (isArabic) arabicRows else englishRows
        rows.forEachIndexed { index, keys ->
            val rowKeys = if (index == 2) listOf("⇧") + keys + listOf("⌫") else keys
            addKeyRow(keyboard, rowKeys) { key ->
                when (key) {
                    "⇧" -> { isShifted = !isShifted; refreshKeyboard() }
                    "⌫" -> deleteCharacter()
                    else -> commitKey(key)
                }
            }
        }
        addKeyRow(keyboard, listOf("?123", ",", "🌐", "مسافة", ".", "↵")) { key ->
            when (key) {
                "🌐" -> { isArabic = !isArabic; isShifted = false; refreshKeyboard() }
                "مسافة" -> currentInputConnection?.commitText(" ", 1)
                "↵" -> sendEnter()
                "?123" -> currentInputConnection?.commitText("123", 1)
                else -> currentInputConnection?.commitText(key, 1)
            }
        }
        return keyboard
    }

    private fun addKeyRow(parent: LinearLayout, labels: List<String>, onPress: (String) -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        labels.forEach { label ->
            val special = label in setOf("⇧", "⌫", "?123", "🌐", "↵")
            val button = Button(this).apply {
                text = label
                textSize = when (label) { "مسافة" -> 12f; "🌐" -> 18f; else -> 17f }
                isAllCaps = false
                minWidth = 0
                minimumWidth = 0
                minHeight = 0
                minimumHeight = 0
                setPadding(dp(1), 0, dp(1), 0)
                setTextColor(if (special) Color.WHITE else Color.rgb(35, 32, 40))
                background = roundedBackground(if (special) 0xFF716783.toInt() else Color.WHITE)
                setOnClickListener { onPress(label) }
            }
            val weight = if (label == "مسافة") 2.8f else if (label in setOf("⇧", "⌫", "?123", "🌐", "↵")) 1.35f else 1f
            row.addView(button, LinearLayout.LayoutParams(0, dp(48), weight).apply {
                setMargins(dp(2), dp(3), dp(2), dp(3))
            })
        }
        parent.addView(row, LinearLayout.LayoutParams(-1, -2))
    }

    private fun commitKey(key: String) {
        val output = if (isShifted && !isArabic) key.uppercase() else key
        currentInputConnection?.commitText(output, 1)
        if (isShifted) { isShifted = false; refreshKeyboard() }
    }

    private fun deleteCharacter() {
        val connection: InputConnection = currentInputConnection ?: return
        connection.deleteSurroundingText(1, 0)
    }

    private fun sendEnter() {
        val connection = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
            ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            connection.performEditorAction(action)
        } else {
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    private fun refreshKeyboard() {
        setInputView(buildKeyboard())
    }

    private fun roundedBackground(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(6).toFloat()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
