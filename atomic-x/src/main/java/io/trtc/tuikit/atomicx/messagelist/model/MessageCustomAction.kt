package io.trtc.tuikit.atomicx.messagelist.model

import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class MessageCustomAction(
    val title: String,
    val iconResID: Int,
    val action: (MessageInfo) -> Unit
)
