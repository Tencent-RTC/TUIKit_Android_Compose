package io.trtc.tuikit.atomicx.messageinput.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.trtc.tuikit.atomicxcore.api.message.MessageInputStore

class MessageInputViewModelFactory(val messageInputStore: MessageInputStore) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessageInputViewModel(messageInputStore) as T
    }
}