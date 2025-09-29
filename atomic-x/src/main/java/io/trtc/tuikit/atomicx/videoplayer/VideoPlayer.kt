package io.trtc.tuikit.atomicx.videoplayer

import android.content.Intent
import android.net.Uri
import io.trtc.tuikit.atomicx.basecomponent.utils.ContextProvider
import io.trtc.tuikit.atomicx.videoplayer.ui.VideoPlayerActivity

data class VideoData(
    val uri: Uri,
    val localPath: String? = null,
    val width: Int,
    val height: Int,
    val duration: Long? = null,
    val snapshotUrl: String? = null,
    val snapshotLocalPath: String? = null,
)

object VideoPlayer {
    fun play(videoData: VideoData) {
        val context = ContextProvider.appContext
        val intent = Intent(context, VideoPlayerActivity::class.java)
        intent.putExtra("data", videoData.uri)
        intent.putExtra("width", videoData.width)
        intent.putExtra("height", videoData.height)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}