package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.messagelist.ui.LocalInteractionHandler
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageReadReceiptIndicator
import io.trtc.tuikit.atomicx.messagelist.utils.ImageUtils
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class ImageMessageRenderer : MessageRenderer {

    @Composable
    override fun Render(message: MessageInfo) {
        val messageInteraction = LocalInteractionHandler.current
        val context = LocalContext.current

        val originalWidth = message.messageBody?.originalImageWidth?.takeIf { it > 0 } ?: 100
        val originalHeight = message.messageBody?.originalImageHeight?.takeIf { it > 0 } ?: 100
        val imageSize = ImageUtils.calculateOptimalSize(originalWidth, originalHeight)
        LaunchedEffect(Unit) {
            messageInteraction.onRendered()
        }

        Box {
            AsyncImage(
                modifier = Modifier
                    .size(imageSize)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                messageInteraction.onTap()
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

            MessageReadReceiptIndicator(
                message = message,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp)
            )
        }
    }
}
