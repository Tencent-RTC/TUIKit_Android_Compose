package io.trtc.tuikit.atomicx.filepicker.impl

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


class FilePickerBridgeActivity : ComponentActivity() {

    companion object {
        private const val TAG = "FilePickerActivity"
    }


    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setTheme(android.R.style.Theme_Translucent_NoTitleBar)


        if (SystemFilePicker.filePickerConfig == null || SystemFilePicker.filePickerListener == null) {
            finish()
            return
        }


        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val selectedFiles = mutableListOf<Uri>()


                val clipData: ClipData? = result.data?.clipData
                if (clipData != null) {

                    val maxSelection = SystemFilePicker.filePickerConfig?.maxCount ?: Int.MAX_VALUE
                    val itemCount = clipData.itemCount.coerceAtMost(maxSelection)

                    for (i in 0 until itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        if (isValidFile(uri)) {
                            selectedFiles.add(uri)
                        }
                    }
                } else {

                    val uri = result.data?.data
                    if (uri != null && isValidFile(uri)) {
                        selectedFiles.add(uri)
                    }
                }


                SystemFilePicker.filePickerListener?.onPicked(selectedFiles)
            } else {

                SystemFilePicker.filePickerListener?.onCanceled()
            }


            SystemFilePicker.filePickerListener = null
            SystemFilePicker.filePickerConfig = null


            finish()
        }


        val config = SystemFilePicker.filePickerConfig!!


        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

            if (config.allowedMimeType.isNotEmpty()) {
                type = config.allowedMimeType.first()
                if (config.allowedMimeType.size > 1) {

                    putExtra(Intent.EXTRA_MIME_TYPES, config.allowedMimeType.toTypedArray())
                }
            } else {
                type = "*/*"
            }

            addCategory(Intent.CATEGORY_OPENABLE)

            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, config.maxCount > 1)

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            filePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start file picker", e)
            SystemFilePicker.filePickerListener?.onCanceled()
            SystemFilePicker.filePickerListener = null
            SystemFilePicker.filePickerConfig = null
            finish()
        }
    }

    private fun isValidFile(uri: Uri): Boolean {
        try {

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating file URI", e)
        }
        return false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        SystemFilePicker.filePickerListener?.onCanceled()
        SystemFilePicker.filePickerListener = null
        SystemFilePicker.filePickerConfig = null
    }
} 