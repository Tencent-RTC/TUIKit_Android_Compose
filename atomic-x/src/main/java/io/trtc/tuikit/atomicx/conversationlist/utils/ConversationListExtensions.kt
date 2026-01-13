package io.trtc.tuikit.atomicx.conversationlist.utils

import io.trtc.tuikit.atomicxcore.api.contact.GroupType
import io.trtc.tuikit.atomicxcore.api.contact.ReceiveMessageOpt
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationInfo
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationMarkType

val ConversationInfo.isUnread: Boolean
    get() = unreadCount > 0 || markList.contains(ConversationMarkType.UNREAD)

val ConversationInfo.needShowBadge: Boolean
    get() = receiveOption == ReceiveMessageOpt.RECEIVE

val ConversationInfo.needShowNotReceiveIcon: Boolean
    get() = receiveOption != ReceiveMessageOpt.RECEIVE && groupType != GroupType.MEETING
