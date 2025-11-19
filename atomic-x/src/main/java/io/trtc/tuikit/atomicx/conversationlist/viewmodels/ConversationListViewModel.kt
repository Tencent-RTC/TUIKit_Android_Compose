package io.trtc.tuikit.atomicx.conversationlist.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.ui.util.fastDistinctBy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.conversationlist.config.ConversationActionConfigProtocol
import io.trtc.tuikit.atomicx.conversationlist.model.ConversationActionType
import io.trtc.tuikit.atomicx.conversationlist.model.ConversationMenuAction
import io.trtc.tuikit.atomicx.messagelist.utils.collectAsState
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationFetchOption
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationInfo
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationListStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ConversationListViewModel(val conversationListStore: ConversationListStore) : ViewModel() {
    val conversationListState = conversationListStore.conversationListState
    val hasMoreConversation by collectAsState(conversationListState.hasMoreConversation)
    val conversationList by collectAsState(
        conversationListState.conversationList.map { list ->
            list.filter { item ->
                item.conversationID.isNotEmpty()
            }.fastDistinctBy { item -> item.conversationID }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
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
        config: ConversationActionConfigProtocol
    ): List<ConversationMenuAction> {

        val pinAction = ConversationMenuAction().apply {
            if (conversationInfo.isPinned) {
                titleResID = R.string.conversation_list_unpin
                type = ConversationActionType.UNPIN
            } else {
                titleResID = R.string.conversation_list_pin
                type = ConversationActionType.PIN
            }

        }
        val clearAction = ConversationMenuAction(
            titleResID = R.string.conversation_list_clear,
            type = ConversationActionType.CLEAR_MESSAGE
        )
        val deleteAction = ConversationMenuAction(
            titleResID = R.string.conversation_list_delete,
            dangerous = true,
            type = ConversationActionType.DELETE
        )
        return mutableListOf<ConversationMenuAction>().apply {
            if (config.isSupportPin) {
                add(pinAction)
            }
            if (config.isSupportClearHistory) {
                add(clearAction)
            }
            if (config.isSupportDelete) {
                add(deleteAction)
            }
        }
    }

    fun setConversationAction(
        conversationInfo: ConversationInfo,
        actionType: ConversationActionType
    ) {
        when (actionType) {
            ConversationActionType.DELETE -> {
                conversationListStore.deleteConversation(
                    conversationInfo.conversationID,
                    object : CompletionHandler {
                        override fun onSuccess() {
                        }

                        override fun onFailure(code: Int, desc: String) {
                        }
                    })
            }

            ConversationActionType.PIN -> {
                conversationListStore.pinConversation(
                    conversationInfo.conversationID,
                    true,
                    object : CompletionHandler {
                        override fun onSuccess() {
                        }

                        override fun onFailure(code: Int, desc: String) {
                        }
                    })
            }

            ConversationActionType.UNPIN -> {
                conversationListStore.pinConversation(
                    conversationInfo.conversationID,
                    false,
                    object : CompletionHandler {
                        override fun onSuccess() {
                        }

                        override fun onFailure(code: Int, desc: String) {
                        }
                    })
            }

            ConversationActionType.CLEAR_MESSAGE -> {
                conversationListStore.clearConversationMessages(
                    conversationInfo.conversationID,
                    object : CompletionHandler {
                        override fun onSuccess() {
                        }

                        override fun onFailure(code: Int, desc: String) {
                        }
                    })
            }

            ConversationActionType.CLEAR_UNREAD_COUNT -> {
                conversationListStore.clearConversationUnreadCount(
                    conversationInfo.conversationID,
                    object : CompletionHandler {
                        override fun onSuccess() {
                        }

                        override fun onFailure(code: Int, desc: String) {
                        }
                    })
            }

            else -> {}
        }

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
