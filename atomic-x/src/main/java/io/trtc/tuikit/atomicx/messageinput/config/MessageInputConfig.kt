package io.trtc.tuikit.atomicx.messageinput.config

import androidx.compose.runtime.staticCompositionLocalOf

val LocalMessageInputConfig = staticCompositionLocalOf { MessageInputConfig.Default }

class MessageInputConfig {

    companion object {
        private val _default = MessageInputConfig()
        val Default: MessageInputConfig get() = _default

        fun configure(block: MessageInputConfig.() -> Unit) {
            _default.apply(block)
        }
    }

    var autoFocus = false
    var disableMention = false
    var enableTypingStatus = true
    var placeHolder = "Send a message"
}