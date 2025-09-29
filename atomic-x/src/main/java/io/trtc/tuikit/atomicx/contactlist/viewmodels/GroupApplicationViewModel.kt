package io.trtc.tuikit.atomicx.contactlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.ContactListStore
import io.trtc.tuikit.atomicxcore.api.GroupApplicationInfo
import kotlinx.coroutines.launch

class GroupApplicationViewModel(
    private val contactListStore: ContactListStore,
) : ViewModel() {
    private val contactListState = contactListStore.contactListState

    val groupApplications = contactListState.groupApplicationList

    fun fetchGroupApplicationList() {
        contactListStore.fetchGroupApplicationList(object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, message: String) {
            }
        })
    }

    fun acceptGroupApplication(application: GroupApplicationInfo) {
        viewModelScope.launch {
            contactListStore.acceptGroupApplication(
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

    fun refuseGroupApplication(application: GroupApplicationInfo) {
        viewModelScope.launch {
            contactListStore.refuseGroupApplication(
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

    fun clearGroupApplicationUnreadCount() {
        viewModelScope.launch {
            contactListStore.clearGroupApplicationUnreadCount(
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

class GroupApplicationViewModelFactory(
    private val contactListStore: ContactListStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupApplicationViewModel::class.java)) {
            return GroupApplicationViewModel(contactListStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 