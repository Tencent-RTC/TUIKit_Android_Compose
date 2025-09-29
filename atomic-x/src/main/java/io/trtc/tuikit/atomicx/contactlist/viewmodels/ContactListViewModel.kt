package io.trtc.tuikit.atomicx.contactlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.ContactListStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DefaultContactItem(
    val id: String,
    val titleResID: Int = 0,
    val badgeCount: StateFlow<Int> = MutableStateFlow(0),
    val onClick: () -> Unit = {}
)

class ContactListViewModel(
    val contactListStore: ContactListStore
) : ViewModel() {
    private val contactListState = contactListStore.contactListState

    init {
        contactListStore.fetchFriendList(object : CompletionHandler {
            override fun onSuccess() {

            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
        contactListStore.fetchFriendApplicationList(object : CompletionHandler {
            override fun onSuccess() {

            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
        contactListStore.fetchGroupApplicationList(object : CompletionHandler {
            override fun onSuccess() {

            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
    }

    val groupApplicationCount = contactListState.groupApplicationUnreadCount
    val friendApplicationCount = contactListState.friendApplicationUnreadCount

    val friendList = contactListState.friendList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        fetchFriendList()
    }

    override fun onCleared() {
        super.onCleared()
    }


    fun fetchFriendList() {
        viewModelScope.launch {
            contactListStore.fetchFriendList(object : CompletionHandler {
                override fun onSuccess() {
                    // Success handled by StateFlow
                }

                override fun onFailure(code: Int, desc: String) {
                    // Handle error if needed
                }
            })
        }
    }

    fun getDefaultItems(
        onNavigateToMyGroup: () -> Unit = {},
        onNavigateToBlacklist: () -> Unit = {},
        onNavigateToGroupApplications: () -> Unit = {},
        onNavigateToFriendApplications: () -> Unit = {}
    ): List<DefaultContactItem> {

        return listOf(
            DefaultContactItem(
                id = "new_contacts_applications",
                titleResID = R.string.contact_list_new_contacts,
                badgeCount = friendApplicationCount,
                onClick = onNavigateToFriendApplications
            ),
            DefaultContactItem(
                id = "new_group_applications",
                titleResID = R.string.contact_list_new_group_applications,
                badgeCount = groupApplicationCount,
                onClick = onNavigateToGroupApplications
            ),
            DefaultContactItem(
                id = "my_group",
                titleResID = R.string.contact_list_my_group,
                onClick = onNavigateToMyGroup
            ),
            DefaultContactItem(
                id = "blacklist",
                titleResID = R.string.contact_list_blacklist,
                onClick = onNavigateToBlacklist
            )

        )
    }
}

class ContactListViewModelFactory(
    private val contactListStore: ContactListStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactListViewModel::class.java)) {
            return ContactListViewModel(contactListStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}