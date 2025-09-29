package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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

class ImageMessageRenderer : MessageRenderer<MessageInfo> {

    @Composable
    override fun Render(message: MessageInfo) {
        val messageInteraction = LocalMessageInteraction.current
        val messageViewModel = LocalMessageListViewModel.current
        val context = LocalContext.current

        val originalWidth = message.messageBody?.originalImageWidth?.takeIf { it > 0 } ?: 100
        val originalHeight = message.messageBody?.originalImageHeight?.takeIf { it > 0 } ?: 100
        val imageSize = ImageUtils.calculateOptimalSize(originalWidth, originalHeight)
        LaunchedEffect(Unit) {
            messageViewModel.downloadImage(message)
        }
        AsyncImage(
            modifier = Modifier
                .size(imageSize)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            messageViewModel.showImage(context, message)
                        }, onLongPress = {
                            messageInteraction.onLongPress()
                        }
                    )
                },
            model = message.messageBody?.thumbImagePath, contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.message_list_image_error_image),
            placeholder = painterResource(R.drawable.message_list_image_error_image),
            imageLoader = ImageUtils.getImageLoader()
        )
    }
}
