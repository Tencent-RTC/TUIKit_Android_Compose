package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageReadReceiptIndicator
import io.trtc.tuikit.atomicx.messagelist.utils.ImageUtils
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class FaceMessageRenderer : MessageRenderer {

    @Composable
    override fun Render(message: MessageInfo) {
        Box(contentAlignment = Alignment.Center) {

            AsyncImage(
                modifier = Modifier.size(width = 100.dp, height = 100.dp),
                model = R.drawable.message_list_face_holder_icon, contentDescription = "FaceMessage",
                contentScale = ContentScale.Crop,
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