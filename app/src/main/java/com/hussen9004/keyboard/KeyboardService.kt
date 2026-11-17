package com.hussen9004.keyboard

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

class KeyboardService : InputMethodService() {
    private var isArabic = true
    private var isShifted = false
    private var showNumbers = false

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
    private val numberRows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
        listOf("*", "\"", "'", ":", ";", "!", "?", "=")
    )

    override fun onCreateInputView(): View = buildKeyboard()

    private fun buildKeyboard(): View {
        val keyboard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(6))
            setBackgroundColor(Color.rgb(232, 230, 237))
        }
        addKeyRow(keyboard, listOf("📋 الحافظة", "▶ YouTube", "🌐 ويب")) { key ->
            when (key) {
                "📋 الحافظة" -> pasteFromClipboard()
                "▶ YouTube" -> openUrl("https://www.youtube.com")
                "🌐 ويب" -> openUrl("https://www.google.com")
            }
        }
        val rows = if (showNumbers) numberRows else if (isArabic) arabicRows else englishRows
        rows.forEachIndexed { index, keys ->
            val rowKeys = if (index == 2) listOf(if (showNumbers) "ABC" else "⇧") + keys + listOf("⌫") else keys
            addKeyRow(keyboard, rowKeys) { key ->
                when (key) {
                    "⇧" -> { isShifted = !isShifted; refreshKeyboard() }
                    "ABC" -> { showNumbers = false; refreshKeyboard() }
                    "⌫" -> deleteCharacter()
                    else -> commitKey(key)
                }
            }
        }
        addKeyRow(keyboard, listOf(if (showNumbers) "ABC" else "?123", ",", "🌐", "مسافة", ".", "↵")) { key ->
            when (key) {
                "🌐" -> { isArabic = !isArabic; isShifted = false; showNumbers = false; refreshKeyboard() }
                "مسافة" -> commitSpace()
                "↵" -> sendEnter()
                "?123" -> { showNumbers = true; refreshKeyboard() }
                "ABC" -> { showNumbers = false; refreshKeyboard() }
                else -> currentInputConnection?.commitText(key, 1)
            }
        }
        return keyboard
    }

    private fun addKeyRow(parent: LinearLayout, labels: List<String>, onPress: (String) -> Unit) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        labels.forEach { label ->
            val special = label in setOf("⇧", "⌫", "?123", "ABC", "🌐", "↵", "📋 الحافظة", "▶ YouTube", "🌐 ويب")
            val button = Button(this).apply {
                text = label
                textSize = when (label) { "مسافة" -> 12f; "🌐" -> 18f; "📋 الحافظة", "▶ YouTube", "🌐 ويب" -> 11f; else -> 17f }
                isAllCaps = false
                minWidth = 0
                minimumWidth = 0
                minHeight = 0
                minimumHeight = 0
                setPadding(dp(1), 0, dp(1), 0)
                setTextColor(if (special) Color.WHITE else Color.rgb(35, 32, 40))
                background = roundedBackground(if (special) 0xFF716783.toInt() else Color.WHITE)
                isHapticFeedbackEnabled = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
                    .getBoolean(MainActivity.PREF_HAPTICS, false)
                setOnClickListener { onPress(label) }
            }
            val weight = when (label) {
                "مسافة" -> 2.8f
                "📋 الحافظة", "▶ YouTube", "🌐 ويب" -> 1.6f
                "⇧", "⌫", "?123", "ABC", "🌐", "↵" -> 1.35f
                else -> 1f
            }
            val height = if (label in setOf("📋 الحافظة", "▶ YouTube", "🌐 ويب")) 40 else 46
            row.addView(button, LinearLayout.LayoutParams(0, dp(height), weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            })
        }
        parent.addView(row, LinearLayout.LayoutParams(-1, -2))
    }

    private fun commitKey(key: String) {
        val output = if (isShifted && !isArabic) key.uppercase() else key
        currentInputConnection?.commitText(output, 1)
        if (isShifted) { isShifted = false; refreshKeyboard() }
    }

    private fun commitSpace() {
        val connection = currentInputConnection ?: return
        val correctionEnabled = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
            .getBoolean(MainActivity.PREF_AUTOCORRECT, true)
        val editor = currentInputEditorInfo
        if (correctionEnabled && editor != null && !isPasswordField(editor)) {
            val before = connection.getTextBeforeCursor(80, 0)?.toString().orEmpty()
            val word = before.takeLastWhile { !it.isWhitespace() }
            val replacement = corrections[word.lowercase()]
            if (word.isNotEmpty() && replacement != null) {
                connection.deleteSurroundingText(word.length, 0)
                connection.commitText(replacement, 1)
            }
        }
        connection.commitText(" ", 1)
    }

    private fun isPasswordField(editor: EditorInfo): Boolean {
        val inputType = editor.inputType
        val variation = inputType and android.text.InputType.TYPE_MASK_VARIATION
        return variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (!clipboard.hasPrimaryClip()) {
            Toast.makeText(this, "الحافظة فارغة", Toast.LENGTH_SHORT).show()
            return
        }
        val clip = clipboard.primaryClip ?: return
        val item = clip.getItemAt(0)
        val text = item.coerceToText(this)
        if (text.isNullOrEmpty()) {
            Toast.makeText(this, "لا يوجد نص في عنصر الحافظة الحالي", Toast.LENGTH_SHORT).show()
            return
        }
        currentInputConnection?.commitText(text, 1)
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
        }
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

    private fun refreshKeyboard() { setInputView(buildKeyboard()) }

    private fun roundedBackground(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(6).toFloat()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private val corrections = mapOf(
        "teh" to "the", "adn" to "and", "dont" to "don't", "cant" to "can't",
        "wont" to "won't", "im" to "I'm", "i" to "I", "thier" to "their",
        " recieve" to " receive", "الذيي" to "الذي", "هاذا" to "هذا", "لاكن" to "لكن"
    )
}
