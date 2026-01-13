package io.trtc.tuikit.atomicx.messagelist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.trtc.tuikit.atomicx.messagelist.config.ChatMessageListConfig
import io.trtc.tuikit.atomicx.messagelist.config.MessageListConfigProtocol
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageListStore

class MessageListViewModelFactory(
    val messageListStore: MessageListStore,
    val locateMessage: MessageInfo? = null,
    val messageListConfig: MessageListConfigProtocol = ChatMessageListConfig()
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessageListViewModel(messageListStore, locateMessage, messageListConfig) as T
    }
}