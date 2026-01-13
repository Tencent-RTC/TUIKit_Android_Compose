package io.trtc.tuikit.atomicx.messagelist.config

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.trtc.tuikit.atomicx.basecomponent.config.AppBuilderConfig
import io.trtc.tuikit.atomicx.basecomponent.config.MessageAction
import io.trtc.tuikit.atomicx.basecomponent.config.MessageAlignment

interface MessageListConfigProtocol {
    val alignment: MessageAlignment
    val isShowTimeMessage: Boolean
    val isShowLeftAvatar: Boolean
    val isShowLeftNickname: Boolean
    val isShowRightAvatar: Boolean
    val isShowRightNickname: Boolean
    val isShowTimeInBubble: Boolean
    val cellSpacing: Dp
    val isShowSystemMessage: Boolean
    val isShowUnsupportMessage: Boolean
    val horizontalPadding: Dp
    val avatarSpacing: Dp

    // actions
    val isSupportCopy: Boolean
    val isSupportDelete: Boolean
    val isSupportRecall: Boolean
    val isSupportMultiSelect: Boolean
    val isSupportForward: Boolean
    val isSupportReaction: Boolean
}

class ChatMessageListConfig : MessageListConfigProtocol {

    private var _alignment: MessageAlignment? = null
    private var _isShowTimeMessage: Boolean? = null
    private var _isShowLeftAvatar: Boolean? = null
    private var _isShowLeftNickname: Boolean? = null
    private var _isShowRightAvatar: Boolean? = null
    private var _isShowRightNickname: Boolean? = null
    private var _isShowTimeInBubble: Boolean? = null
    private var _cellSpacing: Dp? = null
    private var _isShowSystemMessage: Boolean? = null
    private var _isShowUnsupportMessage: Boolean? = null
    private var _horizontalPadding: Dp? = null
    private var _avatarSpacing: Dp? = null
    private var _isSupportCopy: Boolean? = null
    private var _isSupportDelete: Boolean? = null
    private var _isSupportRecall: Boolean? = null
    private var _isSupportMultiSelect: Boolean? = null
    private var _isSupportForward: Boolean? = null
    private var _isSupportReaction: Boolean? = null

    constructor(
        alignment: MessageAlignment? = null,
        isShowTimeMessage: Boolean? = null,
        isShowLeftAvatar: Boolean? = null,
        isShowLeftNickname: Boolean? = null,
        isShowRightAvatar: Boolean? = null,
        isShowRightNickname: Boolean? = null,
        isShowTimeInBubble: Boolean? = null,
        cellSpacing: Dp? = null,
        isShowSystemMessage: Boolean? = null,
        isShowUnsupportMessage: Boolean? = null,
        horizontalPadding: Dp? = null,
        avatarSpacing: Dp? = null,
        isSupportCopy: Boolean? = null,
        isSupportDelete: Boolean? = null,
        isSupportRecall: Boolean? = null,
        isSupportMultiSelect: Boolean? = null,
        isSupportForward: Boolean? = null,
        isSupportReaction: Boolean? = null,
    ) {
        _alignment = alignment
        _isShowTimeMessage = isShowTimeMessage
        _isShowLeftAvatar = isShowLeftAvatar
        _isShowLeftNickname = isShowLeftNickname
        _isShowRightAvatar = isShowRightAvatar
        _isShowRightNickname = isShowRightNickname
        _isShowTimeInBubble = isShowTimeInBubble
        _cellSpacing = cellSpacing
        _isShowSystemMessage = isShowSystemMessage
        _isShowUnsupportMessage = isShowUnsupportMessage
        _horizontalPadding = horizontalPadding
        _avatarSpacing = avatarSpacing
        _isSupportCopy = isSupportCopy
        _isSupportDelete = isSupportDelete
        _isSupportRecall = isSupportRecall
        _isSupportMultiSelect = isSupportMultiSelect
        _isSupportForward = isSupportForward
        _isSupportReaction = isSupportReaction
    }

    override var alignment: MessageAlignment
        get() = _alignment ?: AppBuilderConfig.messageAlignment
        set(value) {
            _alignment = value
        }

    override var isShowTimeMessage: Boolean
        get() = _isShowTimeMessage ?: true
        set(value) {
            _isShowTimeMessage = value
        }

    override var isShowLeftAvatar: Boolean
        get() = _isShowLeftAvatar ?: true
        set(value) {
            _isShowLeftAvatar = value
        }

    override var isShowLeftNickname: Boolean
        get() = _isShowLeftNickname ?: false
        set(value) {
            _isShowLeftNickname = value
        }

    override var isShowRightAvatar: Boolean
        get() = _isShowRightAvatar ?: false
        set(value) {
            _isShowRightAvatar = value
        }

    override var isShowRightNickname: Boolean
        get() = _isShowRightNickname ?: false
        set(value) {
            _isShowRightNickname = value
        }

    override var isShowTimeInBubble: Boolean
        get() = _isShowTimeInBubble ?: true
        set(value) {
            _isShowTimeInBubble = value
        }

    override var cellSpacing: Dp
        get() = _cellSpacing ?: 10.0.dp
        set(value) {
            _cellSpacing = value
        }

    override var isShowSystemMessage: Boolean
        get() = _isShowSystemMessage ?: true
        set(value) {
            _isShowSystemMessage = value
        }

    override var isShowUnsupportMessage: Boolean
        get() = _isShowUnsupportMessage ?: true
        set(value) {
            _isShowUnsupportMessage = value
        }

    override var isSupportCopy: Boolean
        get() = _isSupportCopy
            ?: AppBuilderConfig.messageActionList.contains(MessageAction.COPY)
        set(value) {
            _isSupportCopy = value
        }

    override var isSupportDelete: Boolean
        get() = _isSupportDelete
            ?: AppBuilderConfig.messageActionList.contains(MessageAction.DELETE)
        set(value) {
            _isSupportDelete = value
        }

    override var isSupportRecall: Boolean
        get() = _isSupportRecall
            ?: AppBuilderConfig.messageActionList.contains(MessageAction.RECALL)
        set(value) {
            _isSupportRecall = value
        }

    override var horizontalPadding: Dp
        get() = _horizontalPadding ?: 16.0.dp
        set(value) {
            _horizontalPadding = value
        }

    override var avatarSpacing: Dp
        get() = _avatarSpacing ?: 8.0.dp
        set(value) {
            _avatarSpacing = value
        }

    override var isSupportMultiSelect: Boolean
        get() = _isSupportMultiSelect
            ?: true
        set(value) {
            _isSupportMultiSelect = value
        }

    override var isSupportForward: Boolean
        get() = _isSupportForward
            ?: true
        set(value) {
            _isSupportForward = value
        }

    override var isSupportReaction: Boolean
        get() = _isSupportReaction ?: true
        set(value) {
            _isSupportReaction = value
        }

}
