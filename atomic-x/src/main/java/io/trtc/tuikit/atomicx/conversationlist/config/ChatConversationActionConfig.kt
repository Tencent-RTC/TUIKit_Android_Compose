package io.trtc.tuikit.atomicx.conversationlist.config

import io.trtc.tuikit.atomicx.basecomponent.config.AppBuilderConfig
import io.trtc.tuikit.atomicx.basecomponent.config.ConversationAction

interface ConversationActionConfigProtocol {
    val isSupportDelete: Boolean
    val isSupportMute: Boolean
    val isSupportPin: Boolean
    val isSupportMarkUnread: Boolean
    val isSupportClearHistory: Boolean
}

class ChatConversationActionConfig : ConversationActionConfigProtocol {

    private var _isSupportDelete: Boolean? = null
    private var _isSupportMute: Boolean? = null
    private var _isSupportPin: Boolean? = null
    private var _isSupportMarkUnread: Boolean? = null
    private var _isSupportClearHistory: Boolean? = null

    constructor(
        isSupportDelete: Boolean? = null,
        isSupportMute: Boolean? = null,
        isSupportPin: Boolean? = null,
        isSupportMarkUnread: Boolean? = null,
        isSupportClearHistory: Boolean? = null
    ) {
        _isSupportDelete = isSupportDelete
        _isSupportMute = isSupportMute
        _isSupportPin = isSupportPin
        _isSupportMarkUnread = isSupportMarkUnread
        _isSupportClearHistory = isSupportClearHistory
    }

    override var isSupportDelete: Boolean
        get() = _isSupportDelete
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.DELETE)
        set(value) {
            _isSupportDelete = value
        }

    override var isSupportMute: Boolean
        get() = _isSupportMute
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.MUTE)
        set(value) {
            _isSupportMute = value
        }

    override var isSupportPin: Boolean
        get() = _isSupportPin
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.PIN)
        set(value) {
            _isSupportPin = value
        }

    override var isSupportMarkUnread: Boolean
        get() = _isSupportMarkUnread
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.MARK_UNREAD)
        set(value) {
            _isSupportMarkUnread = value
        }

    override var isSupportClearHistory: Boolean
        get() = _isSupportClearHistory
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.CLEAR_HISTORY)
        set(value) {
            _isSupportClearHistory = value
        }
}

