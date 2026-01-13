package io.trtc.tuikit.atomicx.messagelist.ui

import androidx.compose.runtime.Composable
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.CreateGroupMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.DefaultMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.FaceMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.FileMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.ImageMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.MergeMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.SoundMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.SystemMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.TextMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.VideoMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.utils.jsonData2Dictionary
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageType

data class MessageRenderConfig(
    val showMessageMeta: Boolean = true,
    val useDefaultBubble: Boolean = true
)

fun interface MessageRenderer {
    @Composable
    fun Render(message: MessageInfo)

    val renderConfig: MessageRenderConfig
        get() = MessageRenderConfig()
}

object MessageRendererRegistry {
    private val renderers = mutableMapOf<MessageType, MessageRenderer>()
    private val customRenderers = mutableMapOf<String, MessageRenderer>()
    private val defaultRenderer = DefaultMessageRenderer()
    
    init {
        registerRenderer(MessageType.TEXT, TextMessageRenderer())
        registerRenderer(MessageType.FILE, FileMessageRenderer())
        registerRenderer(MessageType.IMAGE, ImageMessageRenderer())
        registerRenderer(MessageType.VIDEO, VideoMessageRenderer())
        registerRenderer(MessageType.SOUND, SoundMessageRenderer())
        registerRenderer(MessageType.FACE, FaceMessageRenderer())
        registerRenderer(MessageType.SYSTEM, SystemMessageRenderer())
        registerRenderer(MessageType.MERGED, MergeMessageRenderer())
        registerCustomMessageRenderer(
            "group_create",
            CreateGroupMessageRenderer()
        )
    }

    fun registerRenderer(type: MessageType, renderer: MessageRenderer) {
        renderers[type] = renderer
    }

    fun registerCustomMessageRenderer(businessID: String, renderer: MessageRenderer) {
        customRenderers[businessID] = renderer
    }

    fun getRenderer(message: MessageInfo): MessageRenderer {
        if (message.messageType != MessageType.CUSTOM) {
            return renderers[message.messageType] ?: defaultRenderer
        } else {
            val customInfo = message.messageBody?.customMessage?.data
            val dict = jsonData2Dictionary(customInfo)
            val businessID = dict?.get("businessID")
            return customRenderers[businessID] ?: defaultRenderer
        }
    }


}
