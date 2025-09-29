package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.utils.jsonData2Dictionary
import io.trtc.tuikit.atomicx.messagelist.utils.messageSenderDisplayName
import io.trtc.tuikit.atomicxcore.api.MessageInfo

class CreateGroupMessageRenderer : MessageRenderer<MessageInfo> {
    @Composable
    override fun Render(message: MessageInfo) {
        val colors = LocalTheme.current.colors

        val customInfo = message.messageBody?.customMessage?.data
        val dict = jsonData2Dictionary(customInfo)

        val isCommunity = dict?.getValue("cmd") as? Double == 1.0
        val senderName = message.rawMessage?.messageSenderDisplayName
        val text = "$senderName " +
                if (isCommunity) stringResource(R.string.message_list_create_community)
                else stringResource(R.string.message_list_create_group)
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = text,
            fontSize = 14.sp,
            color = colors.textColorSecondary,
            fontWeight = FontWeight.W400
        )
    }

    override fun showMessageMeta(): Boolean {
        return false
    }
}