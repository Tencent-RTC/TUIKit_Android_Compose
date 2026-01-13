package io.trtc.tuikit.atomicx.videopicker

import android.os.Parcelable
import io.trtc.tuikit.atomicx.albumpicker.AlbumPicker
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerModel
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerConfig
import io.trtc.tuikit.atomicx.albumpicker.PickMode
import io.trtc.tuikit.atomicx.albumpicker.PickMediaType
import io.trtc.tuikit.atomicx.albumpicker.interfaces.AlbumPickerListener
import kotlinx.parcelize.Parcelize

data class VideoPickerModel(
    var id: ULong,
    val mediaPath: String? = null,
    val mediaType: PickMediaType = PickMediaType.IMAGE,
    val videoThumbnailPath: String? = null,
    val isOrigin: Boolean = false
)

interface VideoPickerListener {
    fun onFinishedSelect(count: Int)
    fun onProgress(model: VideoPickerModel, index: Int, progress: Double)
}

@Parcelize
data class VideoPickerConfig(
    val maxCount: Int = 1,
    var gridCount: Int = 4,
    val primaryColor: Int = -1
) : Parcelable

object VideoPicker {
    fun pickVideos(videoPickerConfig: VideoPickerConfig = VideoPickerConfig(), listener: VideoPickerListener) {
        AlbumPicker.pickMedia(
            config = AlbumPickerConfig(
                pickMode = PickMode.VIDEO,
                maxCount = videoPickerConfig.maxCount,
                gridCount = videoPickerConfig.gridCount,
                primaryColor = videoPickerConfig.primaryColor
            ),
            listener = object : AlbumPickerListener {
                override fun onFinishedSelect(count: Int) {
                    listener.onFinishedSelect(count)
                }

                override fun onProgress(model: AlbumPickerModel, index: Int, progress: Double) {
                    val videoModel = VideoPickerModel(
                        id = model.id,
                        mediaPath = model.mediaPath,
                        mediaType = model.mediaType,
                        videoThumbnailPath = model.videoThumbnailPath,
                        isOrigin = model.isOrigin
                    )
                    listener.onProgress(videoModel, index, progress)
                }
            })
    }
}
