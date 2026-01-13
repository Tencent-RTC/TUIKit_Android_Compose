package io.trtc.tuikit.atomicx.conversationlist.model

import io.trtc.tuikit.atomicxcore.api.conversation.ConversationInfo

data class ConversationMenuAction(
    var titleResID: Int = 0,
    var dangerous: Boolean = false,
    var action: (ConversationInfo) -> Unit = {},
)

data class ConversationCustomAction(val title: String, val action: (ConversationInfo) -> Unit)

