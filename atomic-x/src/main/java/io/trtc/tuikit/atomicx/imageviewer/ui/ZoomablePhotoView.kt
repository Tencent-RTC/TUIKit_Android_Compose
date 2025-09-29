package io.trtc.tuikit.atomicx.imageviewer.ui

import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil3.load
import io.trtc.tuikit.atomicx.imageviewer.ui.photoview.PhotoView

@Composable
fun ZoomablePhotoView(modifier: Modifier = Modifier, data: Any, onTap: () -> Unit = {}) {
    val context = LocalContext.current
    val androidPhotoView = remember { PhotoView(context) }
    AndroidView(modifier = modifier, factory = {
        androidPhotoView.apply {
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnPhotoTapListener { _, _, _ -> onTap() }
        }
    }, update = {
        androidPhotoView.load(data = data)
    })
}