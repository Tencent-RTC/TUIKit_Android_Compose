package io.trtc.tuikit.atomicx.conversationlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationListStore

class ConversationListViewModelFactory(val conversationListStore: ConversationListStore) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationListViewModel(conversationListStore) as T
    }
}