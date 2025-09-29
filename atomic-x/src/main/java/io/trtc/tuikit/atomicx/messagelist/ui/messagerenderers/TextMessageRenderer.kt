package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.rememberEmojiText
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicxcore.api.MessageInfo

class TextMessageRenderer : MessageRenderer<MessageInfo> {
    @Composable
    override fun Render(message: MessageInfo) {
        val colors = LocalTheme.current.colors
        val contentColor = if (!message.isSelf) colors.textColorPrimary else colors.textColorAntiPrimary

        val (annotatedString, inlineContent) = rememberEmojiText(message.messageBody?.text ?: "")

        Text(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            fontSize = 14.sp,
            text = annotatedString,
            inlineContent = inlineContent,
            color = contentColor,
        )
    }
}