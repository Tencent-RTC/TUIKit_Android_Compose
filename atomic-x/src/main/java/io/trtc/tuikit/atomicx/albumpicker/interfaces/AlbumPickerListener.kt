package io.trtc.tuikit.atomicx.albumpicker.interfaces;

import android.net.Uri

interface AlbumPickerListener {
    fun onPicked(result: List<Pair<Uri, Boolean>>)
    fun onCanceled()
}
