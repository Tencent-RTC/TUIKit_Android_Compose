package io.trtc.tuikit.atomicx.albumpicker

import android.os.Parcelable
import io.trtc.tuikit.atomicx.albumpicker.impl.MultimediaAlbumPicker
import io.trtc.tuikit.atomicx.albumpicker.interfaces.AlbumPickerListener
import kotlinx.parcelize.Parcelize

enum class PickMode {
    IMAGE,
    VIDEO,
    ALL
}

@Parcelize
data class AlbumPickerConfig(
    var pickMode: PickMode = PickMode.ALL,
    var maxCount: Int = 99,
    var gridCount: Int = 4,
    var primaryColor: Int = -1
) : Parcelable

interface AbstractAlbumPicker {
    fun pickMedia(config: AlbumPickerConfig, listener: AlbumPickerListener)
}

object AlbumPicker {
    private val instance = MultimediaAlbumPicker()

    fun pickMedia(config: AlbumPickerConfig = AlbumPickerConfig(), listener: AlbumPickerListener) {
        instance.pickMedia(config, listener)
    }
}
