package io.trtc.tuikit.atomicx.conversationlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.trtc.tuikit.atomicx.conversationlist.config.ChatConversationActionConfig
import io.trtc.tuikit.atomicx.conversationlist.config.ConversationActionConfigProtocol
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationListStore

class ConversationListViewModelFactory(
    val conversationListStore: ConversationListStore,
    val conversationActionConfig: ConversationActionConfigProtocol = ChatConversationActionConfig()
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationListViewModel(conversationListStore, conversationActionConfig) as T
    }
}