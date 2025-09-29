package io.trtc.tuikit.atomicx.imageviewer

import android.app.Activity
import android.content.Intent
import io.trtc.tuikit.atomicx.basecomponent.utils.ContextProvider
import io.trtc.tuikit.atomicx.imageviewer.ui.ImageViewerActivity
import kotlinx.coroutines.flow.MutableStateFlow

interface EventHandler {
    fun onEvent(eventData: Map<String, Any>, callback: (Any?) -> Unit)
}

data class ImageElement(
    val data: Any?,
    val type: Int = 0,
    val width: Int = 0,
    val height: Int = 0
)

object ImageViewer {
    var eventHandler: EventHandler? = null
    var mediaList = MutableStateFlow(listOf<ImageElement>())
    var initDataInternal: ImageElement? = null

    fun view(
        imageElements: List<ImageElement>,
        initialIndex: Int = 0,
        onEventTriggered: EventHandler? = null,
    ) {
        val context = ContextProvider.appContext
        eventHandler = onEventTriggered
        mediaList.value = imageElements
        initDataInternal = if (imageElements.size > initialIndex && imageElements.isNotEmpty()) {
            imageElements[initialIndex]
        } else {
            null
        }
        val intent = Intent(context, ImageViewerActivity::class.java)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}