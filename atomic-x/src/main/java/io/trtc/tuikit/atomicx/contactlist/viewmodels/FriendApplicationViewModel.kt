package io.trtc.tuikit.atomicx.contactlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.ContactListStore
import io.trtc.tuikit.atomicxcore.api.FriendApplicationInfo
import kotlinx.coroutines.launch

class FriendApplicationViewModel(
    private val contactListStore: ContactListStore,
) : ViewModel() {
    private val contactListState = contactListStore.contactListState

    val friendApplications = contactListState.friendApplicationList

    init {
        loadFriendApplications()
    }

    private fun loadFriendApplications() {
        contactListStore.fetchFriendApplicationList(object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, message: String) {
            }
        })
    }

    fun acceptFriendApplication(application: FriendApplicationInfo) {
        viewModelScope.launch {
            contactListStore.acceptFriendApplication(
                application,
                object : CompletionHandler {
                    override fun onSuccess() {
                    }

                    override fun onFailure(code: Int, message: String) {
                    }
                }
            )
        }
    }

    fun refuseFriendApplication(application: FriendApplicationInfo) {
        viewModelScope.launch {
            contactListStore.refuseFriendApplication(
                application,
                object : CompletionHandler {
                    override fun onSuccess() {
                    }

                    override fun onFailure(code: Int, message: String) {
                    }
                }
            )
        }
    }

    fun clearFriendApplicationUnreadCount() {
        viewModelScope.launch {
            contactListStore.clearFriendApplicationUnreadCount(
                object : CompletionHandler {
                    override fun onSuccess() {
                    }

                    override fun onFailure(code: Int, message: String) {
                    }
                }
            )
        }
    }

}

class FriendApplicationViewModelFactory(
    private val contactListStore: ContactListStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendApplicationViewModel::class.java)) {
            return FriendApplicationViewModel(contactListStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 