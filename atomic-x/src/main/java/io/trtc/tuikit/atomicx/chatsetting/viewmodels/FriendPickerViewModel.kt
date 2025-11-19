package io.trtc.tuikit.atomicx.chatsetting.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class FriendPickerViewModel : ViewModel() {

    private val contactListStore = ContactListStore.create()
    private val contactListState = contactListStore.contactListState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val friends: StateFlow<List<ContactInfo>> = contactListState.friendList

    fun loadFriends() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                contactListStore.fetchFriendList(object : CompletionHandler {
                    override fun onSuccess() {
                        _isLoading.value = false
                    }

                    override fun onFailure(code: Int, desc: String) {
                        _isLoading.value = false
                    }
                })
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}

