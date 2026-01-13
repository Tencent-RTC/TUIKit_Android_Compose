package io.trtc.tuikit.atomicx.search.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.search.MessageSearchFilter
import io.trtc.tuikit.atomicxcore.api.search.SearchOption
import io.trtc.tuikit.atomicxcore.api.search.SearchState
import io.trtc.tuikit.atomicxcore.api.search.SearchStore
import io.trtc.tuikit.atomicxcore.api.search.SearchType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SearchMessageInConversationViewModel : ViewModel() {

    private val searchStore: SearchStore = SearchStore.create()
    private val searchState: SearchState = searchStore.searchState


    val isSearching: StateFlow<Boolean> = searchState.messageResults.map { false }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    val isLoadingMore: StateFlow<Boolean> = searchState.hasMoreMessageResults.map { false }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )


    val messagesInConversation: StateFlow<List<MessageInfo>> =
        searchState.messageResults.map { messageResults ->
            messageResults.firstOrNull()?.messageList ?: emptyList()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(conversationID: String, query: String) {
        if (query.isBlank()) {
            return
        }
        searchMessagesInConversation(conversationID, query)
    }

    fun searchMessagesInConversation(conversationID: String, keyword: String) {
        searchStore.search(
            listOf(keyword),
            SearchOption(
                searchType = SearchType.Message,
                messageFilter = MessageSearchFilter(conversationID = conversationID)
            )
        )
    }

    fun searchMore() {
        searchStore.searchMore(SearchType.Message)
    }

}
