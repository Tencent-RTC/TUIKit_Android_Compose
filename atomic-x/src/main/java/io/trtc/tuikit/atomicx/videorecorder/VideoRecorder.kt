package io.trtc.tuikit.atomicx.videorecorder

import io.trtc.tuikit.atomicx.videorecorder.impl.VideoRecorderImpl

object VideoRecorder {
    private val instance = VideoRecorderImpl()

    /**
     * Launches the camera capture interface
     * @param config Capture configuration settings
     * @param listener Callback interface for receiving capture results, including:
     *                 - Photo capture success
     *                 - Video recording success
     *                 - Error events
     */
    fun takeVideo(config: TakeVideoConfig = TakeVideoConfig(), listener: RecordListener) {
        instance.takeVideo(config, listener)
    }

    fun takePhoto(config: TakePhotoConfig = TakePhotoConfig(), listener: RecordListener) {
        val takeVideoConfig = TakeVideoConfig(
            recordMode = RecordMode.PHOTO_ONLY,
            primaryColor = config.primaryColor,
            defaultFrontCamera = config.defaultFrontCamera
        )
        instance.takeVideo(takeVideoConfig, listener)
    }
}

data class TakeVideoConfig(
    val maxVideoDuration: Int? = null,
    val minVideoDuration: Int? = null,
    val videoQuality: VideoQuality? = null,
    val recordMode: RecordMode? = null,
    val primaryColor: String? = null,
    val defaultFrontCamera: Boolean? = null
)

data class TakePhotoConfig(
    val primaryColor: String? = null,
    val defaultFrontCamera: Boolean? = null
)

enum class RecordMode(val value: Int) {
    MIXED(0),
    PHOTO_ONLY(1),
    VIDEO_ONLY(2);

    companion object {
        fun fromInteger(value: Int): RecordMode {
            return RecordMode.entries.find { it.value == value } ?: MIXED
        }
    }
}

interface RecordListener {
    fun onPhotoCaptured(filePath: String) {}

    fun onVideoCaptured(filePath: String, duration: Int) {}

    fun onError(errorCode: VideoRecordeErrorCode) {}
}

enum class VideoQuality(val value: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    companion object {
        fun fromInteger(value: Int): VideoQuality {
            return VideoQuality.entries.find { it.value == value } ?: LOW
        }
    }
}

enum class VideoRecordeErrorCode(val value: Int) {
    PERMISSION_DENIED(-1),
    RECORDE_CANCEL(-2),
    RECORDE_INNER_ERROR(-3);
}