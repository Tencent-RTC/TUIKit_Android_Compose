package io.trtc.tuikit.atomicx.imagepicker

import android.os.Parcelable
import io.trtc.tuikit.atomicx.albumpicker.AlbumPicker
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerModel
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerConfig
import io.trtc.tuikit.atomicx.albumpicker.PickMode
import io.trtc.tuikit.atomicx.albumpicker.interfaces.AlbumPickerListener
import kotlinx.parcelize.Parcelize

interface ImagePickerListener {
    fun onFinishedSelect(count: Int)
    fun onProgress(model: AlbumPickerModel, index: Int, progress: Double)
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
                override fun onFinishedSelect(count: Int) {
                    listener.onFinishedSelect(count)
                }

                override fun onProgress(model: AlbumPickerModel, index: Int, progress: Double) {
                    listener.onProgress(model, index, progress)
                }
            })
    }
}
