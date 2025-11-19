package io.trtc.tuikit.atomicx.contactlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyGroupViewModel(
    private val contactListStore: ContactListStore,
) : ViewModel() {
    private val contactListState = contactListStore.contactListState

    val groups = contactListState.groupList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    override fun onCleared() {
        super.onCleared()
    }

    fun fetchGroups() {
        viewModelScope.launch {
            contactListStore.fetchJoinedGroupList(object : CompletionHandler {
                override fun onSuccess() {
                    // Success handled by StateFlow
                }

                override fun onFailure(code: Int, desc: String) {
                    // Handle error if needed
                }
            })
        }
    }

}

class MyGroupViewModelFactory(
    private val contactListStore: ContactListStore,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyGroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyGroupViewModel(contactListStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}