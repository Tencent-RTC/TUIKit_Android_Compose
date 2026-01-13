package io.trtc.tuikit.atomicx.conversationlist.viewmodels

import androidx.compose.ui.util.fastDistinctBy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.conversationlist.config.ConversationActionConfigProtocol
import io.trtc.tuikit.atomicx.conversationlist.model.ConversationMenuAction
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationFetchOption
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationInfo
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationListStore
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationMarkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ConversationListViewModel(
    val conversationListStore: ConversationListStore,
    val conversationActionConfig: ConversationActionConfigProtocol
) : ViewModel() {
    val conversationListState = conversationListStore.conversationListState
    val hasMoreConversation = conversationListState.hasMoreConversation
    val conversationList =
        conversationListState.conversationList.map { list ->
            list.filter { item ->
                item.conversationID.isNotEmpty()
            }.fastDistinctBy { item -> item.conversationID }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private var _selectedConversations: MutableStateFlow<LinkedHashSet<ConversationInfo>> =
        MutableStateFlow(linkedSetOf())
    val selectedConversations = _selectedConversations.asStateFlow()

    init {
        conversationListStore.fetchConversationList(
            ConversationFetchOption(),
            object : CompletionHandler {
                override fun onSuccess() {
                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
    }

    fun addSelection(conversation: ConversationInfo) {
        _selectedConversations.value =
            (selectedConversations.value + conversation) as LinkedHashSet<ConversationInfo>
    }


    fun removeSelection(conversation: ConversationInfo) {
        _selectedConversations.value =
            (selectedConversations.value - conversation) as LinkedHashSet<ConversationInfo>
    }

    fun clearSelection() {
        _selectedConversations.value = linkedSetOf()
    }

    override fun onCleared() {
    }

    fun getActions(
        conversationInfo: ConversationInfo,
    ): List<ConversationMenuAction> {

        val pinAction = ConversationMenuAction().apply {
            if (conversationInfo.isPinned) {
                titleResID = R.string.conversation_list_unpin
                action = {
                    unpinConversation(conversationInfo)
                }
            } else {
                titleResID = R.string.conversation_list_pin
                action = {
                    pinConversation(conversationInfo)
                }
            }

        }
        val clearAction = ConversationMenuAction(
            titleResID = R.string.conversation_list_clear,
            action = {
                clearMessage(conversationInfo)
            }
        )
        val deleteAction = ConversationMenuAction(
            titleResID = R.string.conversation_list_delete,
            dangerous = true,
            action = {
                deleteConversation(conversationInfo)
            }
        )
        return mutableListOf<ConversationMenuAction>().apply {
            if (conversationActionConfig.isSupportPin) {
                add(pinAction)
            }
            if (conversationActionConfig.isSupportClearHistory) {
                add(clearAction)
            }
            if (conversationActionConfig.isSupportDelete) {
                add(deleteAction)
            }
        }
    }

    fun deleteConversation(conversationInfo: ConversationInfo) {
        conversationListStore.deleteConversation(conversationInfo.conversationID)
    }

    fun pinConversation(conversationInfo: ConversationInfo) {
        conversationListStore.pinConversation(conversationInfo.conversationID, true)
    }

    fun unpinConversation(conversationInfo: ConversationInfo) {
        conversationListStore.pinConversation(conversationInfo.conversationID, false)
    }

    fun clearMessage(conversationInfo: ConversationInfo) {
        conversationListStore.clearConversationMessages(conversationInfo.conversationID)
    }

    fun clearUnreadCount(conversationInfo: ConversationInfo) {
        conversationListStore.clearConversationUnreadCount(conversationInfo.conversationID)
        conversationListStore.markConversation(
            listOf(conversationInfo.conversationID),
            ConversationMarkType.UNREAD,
            false
        )
    }

    fun markAsUnRead(conversationInfo: ConversationInfo) {
        conversationListStore.markConversation(
            listOf(conversationInfo.conversationID),
            ConversationMarkType.UNREAD,
            true
        )
    }

    fun loadMoreConversation() {
        conversationListStore.fetchMoreConversationList(object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
    }

}
