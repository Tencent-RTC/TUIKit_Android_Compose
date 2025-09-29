package io.trtc.tuikit.atomicx.messageinput.data

data class MessageInputMenuAction(
    var title: String = "",
    var iconResID: Int = 0,
    var dangerous: Boolean = false,
    var onClick: () -> Unit = {}
)