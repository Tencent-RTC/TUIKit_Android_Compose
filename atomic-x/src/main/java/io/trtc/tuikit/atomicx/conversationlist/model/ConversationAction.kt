package io.trtc.tuikit.atomicx.conversationlist.model


enum class ConversationActionType {
    DELETE,
    MARK_READ,
    MARK_UNREAD,
    MARK_HIDDEN,
    CLEAR_UNREAD_COUNT,
    CLEAR_MESSAGE,
    SET_DRAFT,
    PIN,
    UNPIN,
}

data class ConversationMenuAction(
    var titleResID: Int = 0,
    var dangerous: Boolean = false,
    var type: ConversationActionType = ConversationActionType.PIN,
)