package io.trtc.tuikit.atomicx.contactlist.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.utils.appContext
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicx.userpicker.UserPickerData
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore
import io.trtc.tuikit.atomicxcore.api.contact.GroupType
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.atomicxcore.api.message.CustomMessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageBody
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageInputStore
import io.trtc.tuikit.atomicxcore.api.message.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ChatType {
    SINGLE,
    GROUP
}

enum class GroupFlowStep {
    CONTACT_SELECTION,
    GROUP_SETTINGS,
    GROUP_TYPE_SELECTION
}

data class GroupTypeOption(
    val displayNameResID: Int,
    val descriptionResID: Int,
    val type: GroupType
)

const val BUSINESS_ID_GROUP_CREATE = "group_create"

val workTypeOption = GroupTypeOption(
    displayNameResID = R.string.contact_list_group_work_type,
    descriptionResID = R.string.contact_list_group_work_des,
    type = GroupType.WORK
)
val publicTypeOption = GroupTypeOption(
    displayNameResID = R.string.contact_list_group_public_type,
    descriptionResID = R.string.contact_list_group_public_des,
    type = GroupType.PUBLIC
)
val meetingTypeOption = GroupTypeOption(
    displayNameResID = R.string.contact_list_group_meeting_type,
    descriptionResID = R.string.contact_list_group_meeting_des,
    type = GroupType.MEETING
)
val communityTypeOption = GroupTypeOption(
    displayNameResID = R.string.contact_list_group_community_type,
    descriptionResID = R.string.contact_list_group_community_des,
    type = GroupType.COMMUNITY
)

data class AddNewChatUiState(
    val chatType: ChatType = ChatType.SINGLE,
    val selectedContacts: List<ContactInfo> = emptyList(),
    val isCreating: Boolean = false,
    val error: String? = null,
    val createdConversationId: String? = null,
    val groupFlowStep: GroupFlowStep = GroupFlowStep.CONTACT_SELECTION,
    val groupName: String = "",
    val groupID: String? = null,
    val groupAvatarUrl: String? = null,
)

class AddNewChatViewModel(
    private val contactListStore: ContactListStore,
) : ViewModel() {
    private val contactListState = contactListStore.contactListState

    val contactDataSource = contactListState.friendList.map {
        it.map { UserPickerData(key = it.contactID, label = it.displayName, avatarUrl = it.avatarURL, extraData = it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(AddNewChatUiState())
    val uiState: StateFlow<AddNewChatUiState> = _uiState.asStateFlow()

    private val _currentSelectedGroupType = MutableStateFlow(workTypeOption)
    val currentSelectedGroupType: StateFlow<GroupTypeOption> =
        _currentSelectedGroupType.asStateFlow()

    init {
        fetchFriendList()
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun setChatType(chatType: ChatType) {
        _uiState.value = _uiState.value.copy(
            chatType = chatType,
            selectedContacts = emptyList(),
            error = null
        )
    }

    fun setSelectedContacts(contacts: List<UserPickerData<ContactInfo>>) {
        _uiState.value = _uiState.value.copy(
            selectedContacts = contacts.map { it.extraData },
            error = null
        )
    }

    fun removeSelectedContact(contact: ContactInfo) {
        val currentState = _uiState.value
        val selectedContacts = currentState.selectedContacts.toMutableList()
        selectedContacts.remove(contact)
        _uiState.value = currentState.copy(
            selectedContacts = selectedContacts,
        )
    }

    fun clearState() {
        _uiState.value = AddNewChatUiState()
    }

    fun getCreatedGroupID(): String? {
        return contactListState.createdGroupID
    }

    fun startChat() {
        val currentState = _uiState.value

        if (currentState.selectedContacts.isEmpty()) {
            _uiState.value = currentState.copy(error = "")
            return
        }

        if (currentState.chatType == ChatType.SINGLE) {
            startSingleChat(currentState.selectedContacts.first())
        } else {
            val ensuredGroupName = if (currentState.groupName.isBlank()) {
                generateGroupName(currentState.selectedContacts.toList())
            } else {
                currentState.groupName
            }
            _uiState.value = currentState.copy(
                groupFlowStep = GroupFlowStep.GROUP_SETTINGS,
                groupName = ensuredGroupName
            )
        }
    }

    private fun startSingleChat(contact: ContactInfo) {
        val conversationId = "c2c_${contact.contactID}"
        _uiState.value = _uiState.value.copy(
            createdConversationId = conversationId,
            isCreating = false
        )
    }

    private fun generateGroupName(contacts: List<ContactInfo>): String {
        val names = contacts.map { it.displayName }

        return when {
            names.size <= 3 -> names.joinToString(
                separator = appContext.getString(R.string.contact_list_group_name_separator) ?: ""
            )

            else -> buildString {
                append(
                    names.take(3).joinToString(
                        separator = appContext.getString(R.string.contact_list_group_name_separator)
                            ?: ""
                    )
                )
                append(appContext.getString(R.string.contact_list_group_name_suffix, names.size))
                    ?: ""
            }
        }
    }

    private fun fetchFriendList() {
        contactListStore.fetchFriendList(object : CompletionHandler {
            override fun onSuccess() {
                // Success handled by StateFlow
            }

            override fun onFailure(code: Int, desc: String) {
                _uiState.value = _uiState.value.copy(error = desc)
            }
        })
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearCreatedConversation() {
        _uiState.value = _uiState.value.copy(createdConversationId = null)
    }

    fun clearGroupSettingsScreen() {
        _uiState.value = _uiState.value.copy(groupFlowStep = GroupFlowStep.CONTACT_SELECTION)
    }

    fun showGroupTypeSelectionScreen() {
        _uiState.value = _uiState.value.copy(groupFlowStep = GroupFlowStep.GROUP_TYPE_SELECTION)
    }

    fun clearGroupTypeSelectionScreen() {
        _uiState.value = _uiState.value.copy(groupFlowStep = GroupFlowStep.GROUP_SETTINGS)
    }

    fun updateSelectedGroupType(groupTypeOption: GroupTypeOption) {
        _currentSelectedGroupType.value = groupTypeOption
        _uiState.value = _uiState.value.copy(
            groupFlowStep = GroupFlowStep.GROUP_SETTINGS
        )
    }

    fun updateGroupName(name: String) {
        _uiState.value = _uiState.value.copy(groupName = name)
    }

    fun updateGroupID(id: String?) {
        _uiState.value = _uiState.value.copy(groupID = id)
    }

    fun updateGroupAvatarUrl(url: String?) {
        _uiState.value = _uiState.value.copy(groupAvatarUrl = url)
    }

    fun createGroupChatWithSettings(
        groupName: String,
        groupID: String? = null,
        groupAvatarUrl: String? = null,
        onSuccess: () -> Unit,
        onFailure: (code: Int, desc: String) -> Unit
    ) {
        val currentState = _uiState.value
        if (currentState.selectedContacts.isEmpty()) {
            _uiState.value = currentState.copy(error = "")
            return
        }

        _uiState.value = currentState.copy(isCreating = true, error = null)

        contactListStore.createGroup(
            groupType = currentSelectedGroupType.value.type.value,
            groupName = groupName,
            groupID = groupID,
            memberList = currentState.selectedContacts,
            avatarURL = groupAvatarUrl, completion = object : CompletionHandler {
                override fun onSuccess() {
                    val finalGroupID = contactListState.createdGroupID
                    val conversationId = "group_${finalGroupID}"
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createdConversationId = conversationId,
                        groupFlowStep = GroupFlowStep.CONTACT_SELECTION
                    )
                    onSuccess()
                    sendGroupCreateMessage(conversationId, currentSelectedGroupType.value.type)
                }

                override fun onFailure(code: Int, desc: String) {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        groupFlowStep = GroupFlowStep.GROUP_SETTINGS
                    )
                    onFailure(code, desc)
                }
            }
        )

    }

    fun getDefaultGroupName(): String {
        val currentState = _uiState.value
        return generateGroupName(currentState.selectedContacts.toList())
    }

    private fun sendGroupCreateMessage(conversationID: String, groupType: GroupType) {
        viewModelScope.launch {
            try {
                val currentUser = LoginStore.shared.loginState.loginUserInfo.value
                val jsonMap = mapOf(
                    "version" to 1,
                    "businessID" to BUSINESS_ID_GROUP_CREATE,
                    "opUser" to (currentUser?.userID ?: ""),
                    "content" to appContext.getString(R.string.contact_list_create_group),
                    "cmd" to if (groupType == GroupType.COMMUNITY) 1 else 0
                )
                val messageInputStore = MessageInputStore.create(conversationID)
                val messageInfo = MessageInfo().apply {
                    messageBody = MessageBody().apply {
                        customMessage = CustomMessageInfo().apply {
                            data = dictionary2JsonData(jsonMap)?.toByteArray(Charsets.UTF_8)
                        }
                        messageType = MessageType.CUSTOM
                    }
                }
                messageInputStore.sendMessage(messageInfo, object : CompletionHandler {
                    override fun onSuccess() {

                    }

                    override fun onFailure(code: Int, desc: String) {
                    }
                })

            } catch (e: Exception) {

            }
        }
    }
}

fun dictionary2JsonData(dictionary: Map<String, Any>): String? {
    return try {
        Gson().toJson(dictionary)
    } catch (e: Exception) {
        Log.e("Util", "Gson conversion failed", e)
        null
    }
}

class AddNewChatViewModelFactory(
    private val contactListStore: ContactListStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddNewChatViewModel::class.java)) {
            return AddNewChatViewModel(contactListStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

const val GROUP_AVATAR_URL =
    "https://im.sdk.qcloud.com/download/tuikit-resource/group-avatar/group_avatar_%s.png"
const val GROUP_AVATAR_COUNT = 24

fun getGroupAvatarUrls(): List<String> {
    return mutableListOf<String>().apply {
        for (i in 1..GROUP_AVATAR_COUNT) {
            add(String.format(GROUP_AVATAR_URL, i))
        }
    }
}

fun getGroupTypeOptionList(): List<GroupTypeOption> {
    return listOf(workTypeOption, publicTypeOption, meetingTypeOption, communityTypeOption)
}