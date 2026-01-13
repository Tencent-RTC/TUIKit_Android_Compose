package io.trtc.tuikit.atomicx.search.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.search.SearchOption
import io.trtc.tuikit.atomicxcore.api.search.SearchState
import io.trtc.tuikit.atomicxcore.api.search.SearchStore
import io.trtc.tuikit.atomicxcore.api.search.SearchType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SearchCategory(
    val type: SearchType,
    val results: List<Any>,
    val hasMore: Boolean = false,
    val totalCount: Int = 0
)

class SearchAllViewModel : ViewModel() {

    private val searchStore: SearchStore = SearchStore.create()
    private val searchState: SearchState = searchStore.searchState

    val isSearching: StateFlow<Boolean> = combine(
        searchState.friendList,
        searchState.groupList,
        searchState.messageResults
    ) { _, _, _ ->
        false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val searchCategories: StateFlow<List<SearchCategory>> =
        combine(
            searchState.friendList,
            searchState.groupList,
            searchState.messageResults
        ) { friendList, groupList, messageResults ->
            val categories = mutableListOf<SearchCategory>()

            if (friendList.isNotEmpty()) {
                categories.add(
                    SearchCategory(
                        type = SearchType.Friend,
                        results = friendList.take(3),
                        hasMore = friendList.size > 3,
                        totalCount = friendList.size
                    )
                )
            }

            if (groupList.isNotEmpty()) {
                categories.add(
                    SearchCategory(
                        type = SearchType.Group,
                        results = groupList.take(3),
                        hasMore = groupList.size > 3,
                        totalCount = groupList.size
                    )
                )
            }

            if (messageResults.isNotEmpty()) {
                categories.add(
                    SearchCategory(
                        type = SearchType.Message,
                        results = messageResults.take(3),
                        hasMore = messageResults.size > 3,
                        totalCount = messageResults.size
                    )
                )
            }

            categories
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        if (query.isBlank()) {
            return
        }
        searchGlobally(query)
    }

    private fun searchGlobally(query: String) {
        searchStore.search(
            listOf(query),
            SearchOption()
        )
    }

}
