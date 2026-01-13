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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Badge
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.conversationlist.utils.isUnread
import io.trtc.tuikit.atomicx.conversationlist.utils.needShowBadge
import io.trtc.tuikit.atomicx.conversationlist.utils.needShowNotReceiveIcon
import io.trtc.tuikit.atomicx.emojipicker.rememberEmojiKeyToName
import io.trtc.tuikit.atomicx.messagelist.utils.MessageUtils
import io.trtc.tuikit.atomicxcore.api.contact.ReceiveMessageOpt
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationInfo
import io.trtc.tuikit.atomicxcore.api.conversation.GroupAtType
import io.trtc.tuikit.atomicxcore.api.message.MessageStatus

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ConversationContent() {
    val conversationInfo = LocalConversation.current
    val colors = LocalTheme.current.colors

    val abstract = MessageUtils.getMessageAbstract(conversationInfo.lastMessage)
    val subtitle = rememberEmojiKeyToName(abstract)

    Row(verticalAlignment = Alignment.CenterVertically) {
        BoxWithConstraints {
            Column(modifier = Modifier.widthIn(max = maxWidth * 0.8f)) {
                Text(
                    text = conversationInfo.title ?: conversationInfo.conversationID, color = colors.textColorPrimary,
                    fontSize = 14.sp, fontWeight = FontWeight.W600, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildSubTitleAnnotatedString(
                        conversationInfo = conversationInfo,
                        defaultSubTitle = subtitle,
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                if (conversationInfo.needShowNotReceiveIcon) {
                    Icon(
                        painter = painterResource(R.drawable.conversation_list_not_receive_icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colors.textColorSecondary
                    )
                } else {
                    Spacer(Modifier.height(2.dp))
                    if (conversationInfo.isUnread && conversationInfo.needShowBadge) {
                        val unreadCount =
                            if (conversationInfo.unreadCount > 99) {
                                "99+"
                            } else if (conversationInfo.unreadCount > 0) {
                                conversationInfo.unreadCount.toString()
                            } else {
                                "1"
                            }
                        Badge(text = unreadCount)
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
                if (conversationInfo.lastMessage?.status == MessageStatus.SEND_FAIL || conversationInfo.lastMessage?.status == MessageStatus.VIOLATION) {
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
private fun buildSubTitleAnnotatedString(
    conversationInfo: ConversationInfo,
    defaultSubTitle: String,
): AnnotatedString {
    val colors = LocalTheme.current.colors
    val atTagText = buildAtTagText(conversationInfo)
    val draftPrefix = stringResource(R.string.conversation_list_draft_prefix)

    return buildAnnotatedString {
        if (!conversationInfo.draft.isNullOrEmpty()) {
            if (atTagText.isNotEmpty()) {
                withStyle(style = SpanStyle(color = colors.textColorError)) {
                    append(atTagText)
                }
            }
            withStyle(style = SpanStyle(color = colors.textColorError)) {
                append(draftPrefix)
            }
            withStyle(style = SpanStyle(color = colors.textColorSecondary)) {
                append(" ")
                append(rememberEmojiKeyToName(conversationInfo.draft ?: ""))
            }
            return@buildAnnotatedString
        }

        if (atTagText.isNotEmpty()) {
            withStyle(style = SpanStyle(color = colors.textColorError)) {
                append(atTagText)
            }
            withStyle(style = SpanStyle(color = colors.textColorSecondary)) {
                append(" ")
                append(defaultSubTitle)
            }
            return@buildAnnotatedString
        }

        if (conversationInfo.receiveOption != ReceiveMessageOpt.RECEIVE
            && conversationInfo.unreadCount > 0
        ) {
            withStyle(style = SpanStyle(color = colors.textColorSecondary)) {
                append("[")
                append(conversationInfo.unreadCount.toString())
                append(stringResource(R.string.conversation_list_message_count_unit))
                append("] ")
                append(defaultSubTitle)
            }
            return@buildAnnotatedString
        }

        withStyle(style = SpanStyle(color = colors.textColorSecondary)) {
            append(defaultSubTitle)
        }
    }
}

@Composable
private fun buildAtTagText(conversationInfo: ConversationInfo): String {
    if (conversationInfo.unreadCount <= 0) return ""
    if (!conversationInfo.conversationID.startsWith("group_")) return ""

    val groupAtInfoList = conversationInfo.groupAtInfoList ?: return ""
    if (groupAtInfoList.isEmpty()) return ""

    val atAllPrefix = stringResource(R.string.conversation_list_at_all_prefix)
    val atMePrefix = stringResource(R.string.conversation_list_at_me_prefix)

    var hasAtAll = false
    var hasAtMe = false

    for (atInfo in groupAtInfoList) {
        when (atInfo.atType) {
            GroupAtType.AT_ME -> hasAtMe = true
            GroupAtType.AT_ALL -> hasAtAll = true
            GroupAtType.AT_ALL_AT_ME -> {
                hasAtAll = true
                hasAtMe = true
            }
        }
    }

    return buildString {
        if (hasAtAll) append(atAllPrefix)
        if (hasAtMe) append(atMePrefix)
    }
}