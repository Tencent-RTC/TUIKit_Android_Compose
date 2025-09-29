package io.trtc.tuikit.atomicx.videorecorder.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import io.trtc.tuikit.atomicx.videorecorder.RecordListener
import io.trtc.tuikit.atomicx.videorecorder.VideoRecordeErrorCode
import io.trtc.tuikit.atomicx.videorecorder.config.VideoRecorderConstants

class VideoRecorderBridgeActivity : ComponentActivity() {
    companion object {
        private const val TAG = "VideoRecorderBridge"
        var callback: RecordListener? = null
    }

    class VideoRecordResult {
        var filePath: String? = null
        var type: Int = VideoRecorderConstants.RECORD_TYPE_VIDEO
        var duration: Int = 0
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) {
            Log.i(TAG, "The activity did not return RESULT_OK, indicating the operation was canceled or failed.")
            callback?.onError(VideoRecordeErrorCode.RECORDE_CANCEL)
        } else {
            val recordResult: VideoRecordResult? = getRecodeResult(result)
            val recodeFilePath = recordResult?.filePath
            Log.i(TAG, "recode file complete. path is $recodeFilePath")
            if (recodeFilePath.isNullOrEmpty()) {
                callback?.onError(VideoRecordeErrorCode.RECORDE_INNER_ERROR)
            } else {
                if (recordResult.type == VideoRecorderConstants.RECORD_TYPE_VIDEO) {
                    callback?.onVideoCaptured(recodeFilePath, recordResult.duration)
                } else {
                    callback?.onPhotoCaptured(recodeFilePath)
                }
            }
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_Translucent_NoTitleBar)
        val intent = Intent(this, VideoRecorderActivity::class.java)
        cameraLauncher.launch(intent)
    }

    private fun getRecodeResult(result: ActivityResult?): VideoRecordResult? {
        if (result == null) {
            return null
        }
        val resultBundle: Bundle = result.data?.extras ?: return null
        val recordeResult = VideoRecordResult()
        recordeResult.filePath = resultBundle.getString(VideoRecorderConstants.PARAM_NAME_EDITED_FILE_PATH)
        recordeResult.type = resultBundle.getInt(VideoRecorderConstants.RESULT_NAME_RECORD_TYPE)
        recordeResult.duration = resultBundle.getInt(VideoRecorderConstants.RESULT_NAME_RECORD_DURATION)
        return recordeResult
    }
} 