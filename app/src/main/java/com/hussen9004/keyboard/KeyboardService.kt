package com.hussen9004.keyboard

import android.content.BroadcastReceiver
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

class KeyboardService : InputMethodService() {
    private var isArabic = true
    private var isShifted = false
    private var showNumbers = false
    private var imageTargetConnection: InputConnection? = null
    private var imageTargetEditorInfo: EditorInfo? = null

    private val imagePickedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val uri = intent.getStringExtra(EXTRA_IMAGE_URI)?.let(Uri::parse) ?: return
            insertGalleryImage(uri)
        }
    }

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

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(ACTION_IMAGE_PICKED)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(imagePickedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(imagePickedReceiver, filter)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(imagePickedReceiver)
        super.onDestroy()
    }

    override fun onCreateInputView(): View = buildKeyboard()

    private fun buildKeyboard(): View {
        val keyboard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(6))
            setBackgroundColor(Color.rgb(232, 230, 237))
        }
        addKeyRow(keyboard, listOf("🖼 ملصق", "📋 الحافظة", "▶ YouTube", "🌐 ويب")) { key ->
            when (key) {
                "🖼 ملصق" -> openGallery()
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
            val toolbar = label in setOf("🖼 ملصق", "📋 الحافظة", "▶ YouTube", "🌐 ويب")
            val special = label in setOf("⇧", "⌫", "?123", "ABC", "🌐", "↵") || toolbar
            val button = Button(this).apply {
                text = label
                textSize = when (label) {
                    "مسافة" -> 12f
                    "🌐" -> 18f
                    "🖼 ملصق", "📋 الحافظة", "▶ YouTube", "🌐 ويب" -> 10f
                    else -> 17f
                }
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
                "🖼 ملصق", "📋 الحافظة", "▶ YouTube", "🌐 ويب" -> 1.4f
                "⇧", "⌫", "?123", "ABC", "🌐", "↵" -> 1.35f
                else -> 1f
            }
            val height = if (toolbar) 40 else 46
            row.addView(button, LinearLayout.LayoutParams(0, dp(height), weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            })
        }
        parent.addView(row, LinearLayout.LayoutParams(-1, -2))
    }

    private fun openGallery() {
        if (currentInputConnection == null) {
            Toast.makeText(this, "افتح حقل كتابة أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        imageTargetConnection = currentInputConnection
        imageTargetEditorInfo = currentInputEditorInfo
        try {
            startActivity(Intent(this, ImagePickerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            imageTargetConnection = null
            imageTargetEditorInfo = null
            Toast.makeText(this, "تعذر فتح الاستديو", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertGalleryImage(uri: Uri) {
        val connection = imageTargetConnection ?: currentInputConnection
        val editorInfo = imageTargetEditorInfo ?: currentInputEditorInfo
        imageTargetConnection = null
        imageTargetEditorInfo = null
        if (connection == null || editorInfo == null) {
            Toast.makeText(this, "ارجع إلى حقل الكتابة ثم اختر الصورة مرة أخرى", Toast.LENGTH_SHORT).show()
            return
        }
        if (isPasswordField(editorInfo)) {
            Toast.makeText(this, "لا يمكن إدراج صورة في حقل كلمة المرور", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= 25) {
            val mimeType = contentResolver.getType(uri) ?: "image/*"
            val supportedTypes = editorInfo.contentMimeTypes.orEmpty()
            val supported = supportedTypes.any { ClipDescription.compareMimeTypes(mimeType, it) }
            if (supported) {
                val description = ClipDescription("ملصق", arrayOf(mimeType))
                val content = InputContentInfo(uri, description, null)
                val committed = try {
                    connection.commitContent(content, InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION, Bundle())
                } catch (_: Exception) {
                    false
                }
                if (committed) return
            }
        }
        shareGalleryImage(uri)
    }

    private fun shareGalleryImage(uri: Uri) {
        Toast.makeText(this, "التطبيق الحالي لا يدعم إدراج الصور من لوحة المفاتيح؛ يمكنك مشاركتها يدويًا", Toast.LENGTH_LONG).show()
        try {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = contentResolver.getType(uri) ?: "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newUri(contentResolver, "ملصق", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(send, "مشاركة الصورة").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            Toast.makeText(this, "تعذرت مشاركة الصورة", Toast.LENGTH_SHORT).show()
        }
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
        val text = clip.getItemAt(0).coerceToText(this)
        if (text.isNullOrEmpty()) {
            Toast.makeText(this, "لا يوجد نص في عنصر الحافظة الحالي", Toast.LENGTH_SHORT).show()
            return
        }
        currentInputConnection?.commitText(text, 1)
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
        "recieve" to "receive", "الذيي" to "الذي", "هاذا" to "هذا", "لاكن" to "لكن"
    )

    companion object {
        const val ACTION_IMAGE_PICKED = "com.hussen9004.keyboard.ACTION_IMAGE_PICKED"
        const val EXTRA_IMAGE_URI = "com.hussen9004.keyboard.EXTRA_IMAGE_URI"
    }
}
