package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.rememberEmojiKeyToName
import io.trtc.tuikit.atomicx.messagelist.ui.LocalInteractionHandler
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderConfig
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.highlightBackground
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageReadReceiptIndicator
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class MergeMessageRenderer : MessageRenderer {

    override val renderConfig: MessageRenderConfig = MessageRenderConfig(useDefaultBubble = false)

    @Composable
    override fun Render(message: MessageInfo) {
        val colors = LocalTheme.current.colors
        val messageInteraction = LocalInteractionHandler.current
        val context = LocalContext.current

        val title = message.messageBody?.mergedMessage?.title ?: ""
        val abstractList = message.messageBody?.mergedMessage?.abstractList ?: emptyList()

        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .highlightBackground(
                    highlightKey = message.msgID ?: "",
                    color = colors.bgColorBubbleReciprocal,
                    shape = RoundedCornerShape(4.dp)
                )
                .pointerInput(message.msgID) {
                    detectTapGestures(
                        onTap = {
                            messageInteraction.onTap()
                        },
                        onLongPress = {
                            messageInteraction.onLongPress()
                        }
                    )
                }
                .padding(vertical = 12.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.W400,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.textColorPrimary
            )

            if (abstractList.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    text = rememberEmojiKeyToName(abstractList.joinToString("\n")),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    lineHeight = 18.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.textColorSecondary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                thickness = 1.dp,
                color = colors.shadowColor
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.message_list_forward_chat_record),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorSecondary
                )

                MessageReadReceiptIndicator(message = message)
            }
        }
    }
}