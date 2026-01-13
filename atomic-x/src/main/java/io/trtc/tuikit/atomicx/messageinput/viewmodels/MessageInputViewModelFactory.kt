package io.trtc.tuikit.atomicx.messageinput.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.trtc.tuikit.atomicx.messageinput.config.ChatMessageInputConfig
import io.trtc.tuikit.atomicx.messageinput.config.MessageInputConfigProtocol
import io.trtc.tuikit.atomicxcore.api.message.MessageInputStore

class MessageInputViewModelFactory(
    val messageInputStore: MessageInputStore,
    private val messageInputConfig: MessageInputConfigProtocol = ChatMessageInputConfig()
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessageInputViewModel(messageInputStore, messageInputConfig) as T
    }
}