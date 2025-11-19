package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class DefaultMessageRenderer : MessageRenderer<MessageInfo> {
    @Composable
    override fun Render(message: MessageInfo) {
        val colors = LocalTheme.current.colors
        Text(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            fontSize = 14.sp,
            text = stringResource(R.string.message_list_message_tips_unsupport_custom_message),
            color = colors.textColorPrimary,
        )
    }
}