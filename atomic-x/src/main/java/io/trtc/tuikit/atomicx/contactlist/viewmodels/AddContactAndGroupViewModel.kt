package io.trtc.tuikit.atomicx.contactlist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tencent.imsdk.BaseConstants
import com.tencent.qcloud.tuicore.util.ErrorMessageConverter
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.utils.appContext
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AddType {
    CONTACT,
    GROUP
}

data class AddContactAndGroupUiState(
    val searchKeyword: String = "",
    val addFriendInfo: ContactInfo? = null,
    val joinGroupInfo: ContactInfo? = null,
    val isAddingContact: Boolean = false,
    val currentUserId: String = "",
    val requestResult: RequestResult? = null,
    val isSearching: Boolean = false
)

data class RequestResult(
    val isSuccess: Boolean,
    val message: String,
    val type: AddType
)

class AddContactAndGroupViewModel(
    private val contactListStore: ContactListStore,
) : ViewModel() {

    private val contactListState = contactListStore.contactListState
    private val _uiState = MutableStateFlow(AddContactAndGroupUiState())
    val uiState: StateFlow<AddContactAndGroupUiState> = _uiState.asStateFlow()

    init {

        viewModelScope.launch {
            contactListState.addFriendInfo.collect { results ->

                _uiState.value = _uiState.value.copy(addFriendInfo = results)
            }
        }


        viewModelScope.launch {
            contactListState.joinGroupInfo.collect { results ->

                _uiState.value = _uiState.value.copy(joinGroupInfo = results)
            }
        }


        getCurrentUserId()
    }

    fun updateSearchKeyword(keyword: String) {
        _uiState.value = _uiState.value.copy(searchKeyword = keyword)

        if (keyword.isEmpty()) {
            _uiState.value = _uiState.value.copy(joinGroupInfo = null, addFriendInfo = null, isSearching = false)
        }
    }

    fun searchGroup() {
        val keyword = _uiState.value.searchKeyword.trim()
        if (keyword.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSearching = true)
        contactListStore.fetchGroupInfo(keyword, object : CompletionHandler {
            override fun onSuccess() {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }

            override fun onFailure(errorCode: Int, errorMessage: String) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        })
    }

    fun searchContact() {
        val keyword = _uiState.value.searchKeyword.trim()
        if (keyword.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSearching = true)
        contactListStore.fetchUserInfo(keyword, object : CompletionHandler {
            override fun onSuccess() {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }

            override fun onFailure(errorCode: Int, errorMessage: String) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        })
    }

    fun addFriend(result: ContactInfo, addWording: String) {

        _uiState.value = _uiState.value.copy(isAddingContact = true, requestResult = null)

        contactListStore.addFriend(
            result.contactID,
            result.title,
            addWording,
            object : CompletionHandler {
                override fun onSuccess() {
                    _uiState.value = _uiState.value.copy(
                        isAddingContact = false,
                        requestResult = RequestResult(
                            isSuccess = true,
                            message = appContext.getString(R.string.contact_list_add_friend_success),
                            type = AddType.CONTACT
                        )
                    )
                }

                override fun onFailure(errorCode: Int, errorMessage: String) {
                    _uiState.value = _uiState.value.copy(
                        isAddingContact = false,
                        requestResult = RequestResult(
                            isSuccess = errorCode == BaseConstants.ERR_SVR_FRIENDSHIP_ALREADY_FRIENDS
                                    || errorCode == BaseConstants.ERR_SVR_FRIENDSHIP_ALLOW_TYPE_NEED_CONFIRM,
                            message = ErrorMessageConverter.convertIMError(errorCode, errorMessage),
                            type = AddType.CONTACT
                        )
                    )
                }
            })
    }

    fun joinGroup(result: ContactInfo, message: String) {
        _uiState.value = _uiState.value.copy(isAddingContact = true, requestResult = null)

        contactListStore.joinGroup(result.contactID, message, object : CompletionHandler {
            override fun onSuccess() {
                _uiState.value = _uiState.value.copy(
                    isAddingContact = false,
                    requestResult = RequestResult(
                        isSuccess = true,
                        message = appContext.getString(R.string.contact_list_join_group_request_sent),
                        type = AddType.GROUP
                    )
                )
            }

            override fun onFailure(errorCode: Int, errorMessage: String) {
                _uiState.value = _uiState.value.copy(
                    isAddingContact = false,
                    requestResult = RequestResult(
                        isSuccess = false,
                        message = ErrorMessageConverter.convertIMError(errorCode, errorMessage),
                        type = AddType.GROUP
                    )
                )
            }
        })
    }

    private fun getCurrentUserId() {

        val currentUserId = com.tencent.imsdk.v2.V2TIMManager.getInstance().loginUser ?: ""
        _uiState.value = _uiState.value.copy(currentUserId = currentUserId)
    }


    fun clearSearchResults() {
        _uiState.value =
            _uiState.value.copy(searchKeyword = "", joinGroupInfo = null, addFriendInfo = null, isSearching = false)
    }

    fun clearRequestResult() {
        _uiState.value = _uiState.value.copy(requestResult = null)
    }
}

class AddContactAndGroupViewModelFactory(
    private val contactListStore: ContactListStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddContactAndGroupViewModel::class.java)) {
            return AddContactAndGroupViewModel(contactListStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 