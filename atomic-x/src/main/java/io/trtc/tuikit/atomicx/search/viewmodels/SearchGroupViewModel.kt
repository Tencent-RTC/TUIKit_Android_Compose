package io.trtc.tuikit.atomicx.search.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.search.GroupSearchInfo
import io.trtc.tuikit.atomicxcore.api.search.SearchOption
import io.trtc.tuikit.atomicxcore.api.search.SearchState
import io.trtc.tuikit.atomicxcore.api.search.SearchStore
import io.trtc.tuikit.atomicxcore.api.search.SearchType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SearchGroupViewModel : ViewModel() {

    private val searchStore: SearchStore = SearchStore.create()
    private val searchState: SearchState = searchStore.searchState

    val isSearching: StateFlow<Boolean> = searchState.groupList.map { false }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val groupSearchResults: StateFlow<List<GroupSearchInfo>> =
        searchState.groupList.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        if (query.isBlank()) {
            return
        }
        searchGroups(query)
    }

    private fun searchGroups(query: String) {
        searchStore.search(
            listOf(query),
            SearchOption(searchType = SearchType.Group)
        )
    }

    fun searchMore() {
        searchStore.searchMore(SearchType.Group)
    }

}
