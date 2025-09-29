package io.trtc.tuikit.atomicx.conversationlist.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Badge
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.rememberEmojiKeyToName
import io.trtc.tuikit.atomicx.messagelist.utils.MessageUtils
import io.trtc.tuikit.atomicxcore.api.ConversationInfo
import io.trtc.tuikit.atomicxcore.api.ConversationReceiveOption
import io.trtc.tuikit.atomicxcore.api.GroupType
import io.trtc.tuikit.atomicxcore.api.MessageStatus

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ConversationContent() {
    val conversationInfo = LocalConversation.current
    val colors = LocalTheme.current.colors


    val abstract = MessageUtils.getMessageAbstract(conversationInfo.lastMessage)
    val displaySubTitle = rememberEmojiKeyToName(abstract)

    Row(verticalAlignment = Alignment.CenterVertically) {
        BoxWithConstraints {
            Column(modifier = Modifier.widthIn(max = maxWidth * 0.8f)) {
                Text(
                    text = conversationInfo.title ?: conversationInfo.conversationID, color = colors.textColorPrimary,
                    fontSize = 14.sp, fontWeight = FontWeight.W600, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = getDisplaySubTitle(displaySubTitle, conversationInfo),
                    color = colors.textColorSecondary,
                    fontSize = 12.sp, fontWeight = FontWeight.W400, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(
            modifier = Modifier
                .weight(1f)
                .widthIn(min = 16.dp)
        )

        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
            Row {
                if (conversationInfo.receiveOption != ConversationReceiveOption.RECEIVE && conversationInfo.groupType != GroupType.MEETING) {
                    Icon(
                        painter = painterResource(R.drawable.conversation_list_not_receive_icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colors.textColorSecondary
                    )
                } else {
                    Spacer(Modifier.height(2.dp))
                    if (conversationInfo.unreadCount > 0) {
                        Badge(text = conversationInfo.unreadCount.toString())
                    } else {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (conversationInfo.lastMessage?.status == MessageStatus.SEND_FAIL) {
                    Text(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color = colors.textColorError, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .wrapContentSize(),
                        text = "!",
                        color = colors.textColorButton,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.W600
                    )

                    Spacer(Modifier.width(4.dp))
                } else if (conversationInfo.lastMessage?.status == MessageStatus.SENDING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        color = colors.textColorAntiSecondary,
                        strokeWidth = 1.5.dp
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    modifier = Modifier,
                    text = MessageUtils.convertDateToYMDStr(conversationInfo.timestamp?.times(1000L)),
                    color = colors.textColorSecondary,
                    maxLines = 1,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
    }

}

@Composable
fun getDisplaySubTitle(displaySubTitle: String, conversationInfo: ConversationInfo): String {
    if (conversationInfo.receiveOption != ConversationReceiveOption.RECEIVE && conversationInfo.unreadCount > 0) {
        val messageCount =
            "[${conversationInfo.unreadCount}${stringResource(R.string.conversation_list_message_count_unit)}] "
        return messageCount + displaySubTitle
    } else {
        return displaySubTitle
    }
}