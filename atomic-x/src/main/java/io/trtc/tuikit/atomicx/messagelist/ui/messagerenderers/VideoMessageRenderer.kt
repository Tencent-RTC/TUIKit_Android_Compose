package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messagelist.ui.LocalInteractionHandler
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageReadReceiptIndicator
import io.trtc.tuikit.atomicx.messagelist.utils.DateTimeUtils
import io.trtc.tuikit.atomicx.messagelist.utils.ImageUtils
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class VideoMessageRenderer : MessageRenderer {
    @Composable
    override fun Render(message: MessageInfo) {
        val originalWidth = message.messageBody?.videoSnapshotWidth?.takeIf { it > 0 } ?: 100
        val originalHeight = message.messageBody?.videoSnapshotHeight?.takeIf { it > 0 } ?: 100
        val imageSize = ImageUtils.calculateOptimalSize(originalWidth, originalHeight)
        val context = LocalContext.current
        val messageInteraction = LocalInteractionHandler.current
        val colors = LocalTheme.current.colors
        LaunchedEffect(Unit) {
            messageInteraction.onRendered()
        }

        Box(modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                messageInteraction.onTap()
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

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
            ) {

                Text(
                    modifier = Modifier
                        .background(color = colors.bgColorElementMask, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 2.dp),
                    text = DateTimeUtils.formatSmartTime(message.messageBody?.videoDuration),
                    fontSize = 10.sp,
                    color = colors.textColorButton
                )

                Spacer(modifier = Modifier.width(6.dp))

                MessageReadReceiptIndicator(
                    message = message,
                    modifier = Modifier
                        .offset(y = -4.dp)
                        .padding(end = 8.dp)
                )

            }
        }
    }

}
