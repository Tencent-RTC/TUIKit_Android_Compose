package io.trtc.tuikit.atomicx.imagepicker

import android.net.Uri
import android.os.Parcelable
import io.trtc.tuikit.atomicx.albumpicker.AlbumPicker
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerConfig
import io.trtc.tuikit.atomicx.albumpicker.PickMode
import io.trtc.tuikit.atomicx.albumpicker.interfaces.AlbumPickerListener
import kotlinx.parcelize.Parcelize

interface VideoPickerListener {
    fun onPicked(result: List<Pair<Uri, Boolean>>)

    fun onCanceled()
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
                override fun onPicked(result: List<Pair<Uri, Boolean>>) {
                    listener.onPicked(result)
                }

                override fun onCanceled() {
                    listener.onCanceled()
                }
            })
    }
}
