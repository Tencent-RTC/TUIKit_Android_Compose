package io.trtc.tuikit.atomicx.messagelist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.trtc.tuikit.atomicxcore.api.message.MessageActionStore
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageListStore

class MessageListViewModelFactory(
    val messageListStore: MessageListStore,
    val messageActionStore: MessageActionStore,
    val locateMessage: MessageInfo?
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessageListViewModel(messageListStore, messageActionStore, locateMessage) as T
    }
}