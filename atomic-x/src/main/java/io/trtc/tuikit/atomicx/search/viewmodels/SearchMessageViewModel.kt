package io.trtc.tuikit.atomicx.search.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.search.MessageSearchResultItem
import io.trtc.tuikit.atomicxcore.api.search.SearchOption
import io.trtc.tuikit.atomicxcore.api.search.SearchState
import io.trtc.tuikit.atomicxcore.api.search.SearchStore
import io.trtc.tuikit.atomicxcore.api.search.SearchType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SearchMessageViewModel : ViewModel() {

    private val searchStore: SearchStore = SearchStore.create()
    private val searchState: SearchState = searchStore.searchState

    val isSearching: StateFlow<Boolean> =
        searchState.messageResults.map { false }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val messageSearchResults: StateFlow<List<MessageSearchResultItem>> =
        searchState.messageResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        if (query.isBlank()) {
            return
        }
        searchMessages(query)
    }

    private fun searchMessages(query: String) {
        searchStore.search(
            listOf(query),
            SearchOption(searchType = SearchType.Message)
        )
    }

    fun searchMore() {
        searchStore.searchMore(SearchType.Message)
    }

}
