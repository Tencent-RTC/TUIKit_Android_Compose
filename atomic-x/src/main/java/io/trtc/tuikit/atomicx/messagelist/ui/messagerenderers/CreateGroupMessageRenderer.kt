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
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderConfig
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.utils.jsonData2Dictionary
import io.trtc.tuikit.atomicx.messagelist.utils.senderDisplayName
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class CreateGroupMessageRenderer : MessageRenderer {

    override val renderConfig = MessageRenderConfig(showMessageMeta = false)

    @Composable
    override fun Render(message: MessageInfo) {
        val colors = LocalTheme.current.colors

        val customInfo = message.messageBody?.customMessage?.data
        val dict = jsonData2Dictionary(customInfo)

        val isCommunity = dict?.get("cmd") as? Double == 1.0
        val senderName = message.senderDisplayName
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

}