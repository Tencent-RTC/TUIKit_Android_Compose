package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.rememberEmojiText
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageReadReceiptIndicator
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class TextMessageRenderer : MessageRenderer {
    @Composable
    override fun Render(message: MessageInfo) {
        val colors = LocalTheme.current.colors
        val contentColor = if (!message.isSelf) colors.textColorPrimary else colors.textColorAntiPrimary

        val (annotatedString, inlineContent) = rememberEmojiText(message.messageBody?.text ?: "")

        Box(modifier = Modifier.padding(bottom = if (message.needReadReceipt) 8.dp else 0.dp)) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                fontSize = 14.sp,
                text = annotatedString,
                inlineContent = inlineContent,
                color = contentColor,
            )

            MessageReadReceiptIndicator(
                message = message,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = 6.dp)
                    .padding(end = 8.dp)
            )
        }
    }
}