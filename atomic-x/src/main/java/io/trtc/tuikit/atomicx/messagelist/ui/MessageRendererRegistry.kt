package io.trtc.tuikit.atomicx.messagelist.ui

import androidx.compose.runtime.Composable
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.DefaultMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.utils.jsonData2Dictionary
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageType

fun interface MessageRenderer<T : MessageInfo> {
    @Composable
    fun Render(message: T)
    fun showMessageMeta(): Boolean {
        return true
    }
}

object MessageRendererRegistry {
    private val renderers = mutableMapOf<MessageType, MessageRenderer<MessageInfo>>()
    private val customRenderers = mutableMapOf<String, MessageRenderer<MessageInfo>>()

    fun registerRenderer(type: MessageType, renderer: MessageRenderer<MessageInfo>) {
        renderers[type] = renderer
    }

    fun getRenderer(message: MessageInfo): MessageRenderer<MessageInfo> {
        if (message.messageType != MessageType.CUSTOM) {
            return renderers[message.messageType] ?: defaultRenderer
        } else {
            val customInfo = message.messageBody?.customMessage?.data
            val dict = jsonData2Dictionary(customInfo)
            val businessID = dict?.get("businessID")
            return customRenderers[businessID] ?: defaultRenderer
        }
    }

    fun registerCustomMessageRenderer(businessID: String, renderer: MessageRenderer<MessageInfo>) {
        customRenderers[businessID] = renderer
    }

    private val defaultRenderer = DefaultMessageRenderer()

}
