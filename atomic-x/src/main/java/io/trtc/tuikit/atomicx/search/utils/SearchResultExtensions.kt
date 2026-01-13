package io.trtc.tuikit.atomicx.search.utils

import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.search.FriendSearchInfo
import io.trtc.tuikit.atomicxcore.api.search.GroupSearchInfo
import io.trtc.tuikit.atomicxcore.api.search.MessageSearchResultItem

val FriendSearchInfo.displayName: String
    get() = when {
        !friendRemark.isNullOrEmpty() -> friendRemark!!
        !userInfo.nickname.isNullOrEmpty() -> userInfo.nickname!!
        else -> userID
    }

val FriendSearchInfo.userAvatarURL: String
    get() = userInfo.avatarURL ?: ""

val GroupSearchInfo.displayName: String
    get() = when {
        groupName.isNotEmpty() -> groupName
        else -> groupID
    }

val MessageSearchResultItem.displayName: String
    get() = conversationShowName.ifEmpty { conversationID }

val MessageInfo.messageSender: String
    get() = listOfNotNull(
        sender.nameCard,
        sender.nickname,
        sender.friendRemark,
        sender.userID
    ).firstOrNull { it.isNotEmpty() } ?: ""


val MessageInfo.messageSenderAvatarUrl: String
    get() = rawMessage?.faceUrl ?: ""
