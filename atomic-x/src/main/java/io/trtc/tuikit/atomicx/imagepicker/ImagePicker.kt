package io.trtc.tuikit.atomicx.imagepicker

import android.net.Uri
import android.os.Parcelable
import io.trtc.tuikit.atomicx.albumpicker.AlbumPicker
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerConfig
import io.trtc.tuikit.atomicx.albumpicker.PickMode
import io.trtc.tuikit.atomicx.albumpicker.interfaces.AlbumPickerListener
import kotlinx.parcelize.Parcelize

interface ImagePickerListener {
    fun onPicked(result: List<Pair<Uri, Boolean>>)

    fun onCanceled()
}

@Parcelize
data class ImagePickerConfig(
    val maxCount: Int = 1,
    var gridCount: Int = 4,
    val primaryColor: Int = -1
) : Parcelable

object ImagePicker {
    fun pickImages(imagePickerConfig: ImagePickerConfig = ImagePickerConfig(), listener: ImagePickerListener) {
        AlbumPicker.pickMedia(
            config = AlbumPickerConfig(
                pickMode = PickMode.IMAGE,
                maxCount = imagePickerConfig.maxCount,
                gridCount = imagePickerConfig.gridCount,
                primaryColor = imagePickerConfig.primaryColor
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
