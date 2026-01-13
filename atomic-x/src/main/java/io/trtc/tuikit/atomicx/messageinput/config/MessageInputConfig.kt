package io.trtc.tuikit.atomicx.messageinput.config

import io.trtc.tuikit.atomicx.basecomponent.config.AppBuilderConfig

interface MessageInputConfigProtocol {
    val isShowAudioRecorder: Boolean
    val isShowPhotoTaker: Boolean
    val isShowMore: Boolean
    val enableReadReceipt: Boolean
    val enableMention: Boolean
}

class ChatMessageInputConfig : MessageInputConfigProtocol {

    private var _isShowAudioRecorder: Boolean? = null
    private var _isShowPhotoTaker: Boolean? = null
    private var _isShowMore: Boolean? = null
    private var _enableReadReceipt: Boolean? = null
    private var _enableMention: Boolean? = null

    constructor(
        isShowAudioRecorder: Boolean? = null,
        isShowPhotoTaker: Boolean? = null,
        isShowMore: Boolean? = null,
        enableReadReceipt: Boolean? = null,
        enableMention: Boolean? = null
    ) {
        this._isShowAudioRecorder = isShowAudioRecorder
        this._isShowPhotoTaker = isShowPhotoTaker
        this._isShowMore = isShowMore
        this._enableReadReceipt = enableReadReceipt
        this._enableMention = enableMention
    }

    override var isShowAudioRecorder: Boolean
        get() = _isShowAudioRecorder ?: true
        set(value) {
            _isShowAudioRecorder = value
        }

    override var isShowPhotoTaker: Boolean
        get() = _isShowPhotoTaker ?: true
        set(value) {
            _isShowPhotoTaker = value
        }

    override var isShowMore: Boolean
        get() = _isShowMore ?: true
        set(value) {
            _isShowMore = value
        }

    override var enableReadReceipt: Boolean
        get() = _enableReadReceipt ?: AppBuilderConfig.enableReadReceipt
        set(value) {
            _enableReadReceipt = value
        }

    override var enableMention: Boolean
        get() = _enableMention ?: true
        set(value) {
            _enableMention = value
        }
}