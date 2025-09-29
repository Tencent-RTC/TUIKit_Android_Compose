package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.messagelist.ui.LocalMessageInteraction
import io.trtc.tuikit.atomicx.messagelist.ui.LocalMessageListViewModel
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.utils.ImageUtils
import io.trtc.tuikit.atomicxcore.api.MessageInfo

class VideoMessageRenderer : MessageRenderer<MessageInfo> {
    @Composable
    override fun Render(message: MessageInfo) {
        val originalWidth = message.messageBody?.videoSnapshotWidth?.takeIf { it > 0 } ?: 100
        val originalHeight = message.messageBody?.videoSnapshotHeight?.takeIf { it > 0 } ?: 100
        val imageSize = ImageUtils.calculateOptimalSize(originalWidth, originalHeight)
        val context = LocalContext.current
        val viewModel = LocalMessageListViewModel.current
        val messageViewModel = LocalMessageListViewModel.current
        val messageInteraction = LocalMessageInteraction.current

        LaunchedEffect(Unit) {
            messageViewModel.downloadVideoSnapShot(message)
        }

        Box(modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                messageViewModel.downloadOrShowVideo(context, message)
            }, onLongPress = {
                messageInteraction.onLongPress()
            })
        }, contentAlignment = Alignment.Center) {

            AsyncImage(
                modifier = Modifier.size(imageSize),
                model = message.messageBody?.videoSnapshotPath, contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.message_list_image_error_image),
                error = painterResource(R.drawable.message_list_image_error_image),
                imageLoader = ImageUtils.getImageLoader()
            )

            Icon(
                painter = painterResource(R.drawable.message_list_video_play_icon),
                contentDescription = "",
                tint = Color.Unspecified
            )
        }
    }

}
