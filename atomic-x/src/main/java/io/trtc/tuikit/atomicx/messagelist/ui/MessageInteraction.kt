package io.trtc.tuikit.atomicx.messagelist.ui

import androidx.compose.runtime.compositionLocalOf

val LocalMessageInteraction = compositionLocalOf<MessageInteraction> {
    error("no MessageInteraction")
}

class MessageInteraction(
    val onClick: () -> Unit,
    val onLongPress: () -> Unit
)