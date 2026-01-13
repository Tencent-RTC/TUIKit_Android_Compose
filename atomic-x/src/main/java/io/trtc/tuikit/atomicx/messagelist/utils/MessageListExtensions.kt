package io.trtc.tuikit.atomicx.messagelist.utils

import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageStatus
import io.trtc.tuikit.atomicxcore.api.message.MessageType

val MessageInfo.isShowReadReceipt: Boolean
    get() = isSelf && needReadReceipt && status == MessageStatus.SEND_SUCCESS && messageType != MessageType.SYSTEM

val MessageInfo.senderDisplayName: String
    get() = listOfNotNull(
        sender.nameCard,
        sender.nickname,
        sender.friendRemark,
        sender.userID
    ).firstOrNull { it.isNotEmpty() } ?: ""

val MessageInfo.isAllRead: Boolean
    get() = when {
        receipt == null -> false
        groupID.isNullOrEmpty() -> receipt?.isPeerRead == true
        else -> receipt?.unreadCount == 0
    }

val MessageInfo.isUnread: Boolean
    get() = when {
        receipt == null -> true
        groupID.isNullOrEmpty() -> receipt?.isPeerRead == false
        else -> receipt?.readCount == 0
    }
