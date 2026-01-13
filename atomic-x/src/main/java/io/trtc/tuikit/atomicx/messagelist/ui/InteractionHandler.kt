package io.trtc.tuikit.atomicx.messagelist.ui

import androidx.compose.runtime.compositionLocalOf

val LocalInteractionHandler = compositionLocalOf<InteractionHandler> {
    error("no MessageInteraction")
}

class InteractionHandler(
    val onRendered: () -> Unit = {},

    val onTap: () -> Unit = {},
    val onLongPress: () -> Unit = {}
)