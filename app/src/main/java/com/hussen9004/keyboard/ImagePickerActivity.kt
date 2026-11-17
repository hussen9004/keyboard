package com.hussen9004.keyboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast

/** Opens Android's system photo picker/document picker without broad storage permissions. */
class ImagePickerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val picker = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        try {
            startActivityForResult(picker, REQUEST_IMAGE)
        } catch (_: Exception) {
            Toast.makeText(this, "تعذر فتح الاستديو", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @Deprecated("Uses the platform document picker result callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                try {
                    val flags = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, flags)
                } catch (_: SecurityException) {
                    // The picker may provide a temporary URI grant instead.
                } catch (_: IllegalArgumentException) {
                    // Some document providers do not offer persistable grants.
                }
                sendBroadcast(Intent(KeyboardService.ACTION_IMAGE_PICKED).apply {
                    setPackage(packageName)
                    putExtra(KeyboardService.EXTRA_IMAGE_URI, uri.toString())
                })
            }
        }
        finish()
    }

    companion object {
        private const val REQUEST_IMAGE = 401
    }
}
