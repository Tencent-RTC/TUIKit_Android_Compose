package io.trtc.tuikit.chat.uikit.components.messageinput.ui

import io.trtc.tuikit.chat.uikit.components.messageinput.data.MessageInputMenuAction

internal class MorePanelActionsSnapshot {
    private var hasLoaded: Boolean = false
    private var wasExpanded: Boolean = false
    private var actions: List<MessageInputMenuAction> = emptyList()

    fun resolve(
        expanded: Boolean,
        load: () -> List<MessageInputMenuAction>,
    ): List<MessageInputMenuAction> {
        if (!hasLoaded || (expanded && !wasExpanded)) {
            actions = load()
            hasLoaded = true
        }
        wasExpanded = expanded
        return actions
    }
}
