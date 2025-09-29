package io.trtc.tuikit.atomicx.chatsetting.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.C2CSettingState
import io.trtc.tuikit.atomicxcore.api.C2CSettingStore
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.ConversationListStore
import io.trtc.tuikit.atomicxcore.api.GroupJoinOption
import io.trtc.tuikit.atomicxcore.api.GroupMember
import io.trtc.tuikit.atomicxcore.api.GroupMemberRole
import io.trtc.tuikit.atomicxcore.api.GroupSettingState
import io.trtc.tuikit.atomicxcore.api.GroupSettingStore
import io.trtc.tuikit.atomicxcore.api.GroupType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class C2CChatSettingViewModel(val userID: String) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _c2cSettingStore: C2CSettingStore = C2CSettingStore.create(userID)
    private val _c2cSettingState: C2CSettingState = _c2cSettingStore.c2cSettingState

    private val _conversationListStore: ConversationListStore = ConversationListStore.create()

    val nickname: StateFlow<String>
        get() = _c2cSettingState.nickname

    val avatar: StateFlow<String>
        get() = _c2cSettingState.avatarURL

    val signature: StateFlow<String>
        get() = _c2cSettingState.signature

    val remark: StateFlow<String>
        get() = _c2cSettingState.remark

    val isNotDisturb: StateFlow<Boolean>
        get() = _c2cSettingState.isNotDisturb

    val isPinned: StateFlow<Boolean>
        get() = _c2cSettingState.isPinned

    val isContact: StateFlow<Boolean>
        get() = _c2cSettingState.isContact

    val isInBlacklist: StateFlow<Boolean>
        get() = _c2cSettingState.isInBlacklist

    val c2CSettingStore: C2CSettingStore
        get() = _c2cSettingStore


    init {
        _c2cSettingStore.checkBlacklistStatus(object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
        _c2cSettingStore.fetchUserInfo(object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
        _conversationListStore.fetchConversationInfo(
            conversationID = "c2c_$userID",
            object : CompletionHandler {
                override fun onSuccess() {
                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
    }


    fun setChatPinned(pin: Boolean, completion: CompletionHandler) {
        _conversationListStore.pinConversation(conversationID = "c2c_$userID", pin, completion)
    }

    fun setChatMuted(mute: Boolean, completion: CompletionHandler) {
        _conversationListStore.muteConversation(conversationID = "c2c_$userID", mute, completion)
    }

    fun setUserRemark(remark: String, completion: CompletionHandler) {
        _c2cSettingStore.setUserRemark(remark, completion)
    }

    fun clearC2CChatHistory(completion: CompletionHandler) {
        _conversationListStore.clearConversationMessages(conversationID = "c2c_$userID", completion)
    }

    fun addToBlacklist(completion: CompletionHandler) {
        _c2cSettingStore.addToBlacklist(completion)
    }

    fun removeFromBlacklist(completion: CompletionHandler) {
        _c2cSettingStore.removeFromBlacklist(completion)
    }

    fun deleteFriend(completion: CompletionHandler) {
        _c2cSettingStore.deleteFriend(completion)
        _conversationListStore.deleteConversation(conversationID = "c2c_$userID")
    }


    fun toggleC2CDoNotDisturb() {
        val currentValue = isNotDisturb.value
        setChatMuted(!currentValue, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun toggleC2CTopChat() {
        val currentValue = isPinned.value
        setChatPinned(!currentValue, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun toggleC2CBlacklist() {
        if (isInBlacklist.value) {
            removeFromBlacklist(object : CompletionHandler {
                override fun onSuccess() {}
                override fun onFailure(code: Int, desc: String) {}
            })
        } else {
            addToBlacklist(object : CompletionHandler {
                override fun onSuccess() {}
                override fun onFailure(code: Int, desc: String) {}
            })
        }
    }

    fun deleteC2CContact() {
        deleteFriend(object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun clearC2CChatHistory() {
        clearC2CChatHistory(object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun setFriendRemark(remark: String) {
        setUserRemark(remark, object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
    }
}


class GroupChatSettingViewModel(val groupID: String) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _groupSettingStore: GroupSettingStore = GroupSettingStore.create(groupID)
    private val _groupSettingState: GroupSettingState = _groupSettingStore.groupSettingState
    private val _conversationListStore: ConversationListStore = ConversationListStore.create()

    init {
        _groupSettingStore.fetchGroupInfo(object : CompletionHandler {
            override fun onSuccess() {

            }

            override fun onFailure(code: Int, desc: String) {
            }
        })

        _conversationListStore.fetchConversationInfo(
            conversationID = "group_$groupID",
            object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })


        _groupSettingStore.fetchGroupMemberList(
            role = GroupMemberRole.ALL,
            object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
        _groupSettingStore.fetchSelfMemberInfo(object : CompletionHandler {
            override fun onSuccess() {

            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
    }

    val groupType: StateFlow<GroupType>
        get() = _groupSettingState.groupType

    val groupName: StateFlow<String>
        get() = _groupSettingState.groupName

    val avatar: StateFlow<String>
        get() = _groupSettingState.avatarURL

    val notice: StateFlow<String>
        get() = _groupSettingState.notice

    val isNotDisturb: StateFlow<Boolean>
        get() = _groupSettingState.isNotDisturb

    val isAllMuted: StateFlow<Boolean>
        get() = _groupSettingState.isAllMuted

    val isPinned: StateFlow<Boolean>
        get() = _groupSettingState.isPinned

    val groupOwner: StateFlow<GroupMember?>
        get() = _groupSettingState.groupOwner

    val allMembers: StateFlow<List<GroupMember>>
        get() = _groupSettingState.allMembers

    val silencedMembers = allMembers
        .map { members -> members.filter { it.isMuted } }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = allMembers.value.filter { it.isMuted }
        )

    val currentUserRole: StateFlow<GroupMemberRole>
        get() = _groupSettingState.currentUserRole

    val selfNameCard: StateFlow<String?>
        get() = _groupSettingState.selfNameCard

    val memberCount: StateFlow<Int>
        get() = _groupSettingState.memberCount

    val joinGroupApprovalType: StateFlow<GroupJoinOption>
        get() = _groupSettingState.joinGroupApprovalType

    val inviteToGroupApprovalType: StateFlow<GroupJoinOption>
        get() = _groupSettingState.inviteToGroupApprovalType

    fun updateGroupProfile(
        name: String? = null,
        notice: String? = null,
        avatar: String? = null,
        completion: CompletionHandler
    ) {
        _groupSettingStore.updateGroupProfile(name, notice, avatar, completion)
    }

    fun setGroupJoinOption(option: GroupJoinOption, completion: CompletionHandler) {
        _groupSettingStore.setGroupJoinOption(option, completion)
    }

    fun setGroupInviteOption(option: GroupJoinOption, completion: CompletionHandler) {
        _groupSettingStore.setGroupInviteOption(option, completion)
    }

    fun addGroupMember(userIDList: List<String>, completion: CompletionHandler) {
        _groupSettingStore.addGroupMember(userIDList, completion)
    }

    fun addGroupMember(userIDList: List<String>) {
        addGroupMember(userIDList, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun deleteGroupMember(groupGroupMembers: List<GroupMember>) {
        _groupSettingStore.deleteGroupMember(groupGroupMembers, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun changeGroupOwner(newOwnerID: String, completion: CompletionHandler) {
        _groupSettingStore.changeGroupOwner(newOwnerID, completion)
    }

    fun setGroupMemberRole(userID: String, role: GroupMemberRole) {
        _groupSettingStore.setGroupMemberRole(userID, role, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun setGroupChatPinned(value: Boolean, completion: CompletionHandler) {
        _conversationListStore.pinConversation(conversationID = "group_$groupID", value, completion)
    }

    fun setGroupChatNotDisturb(mute: Boolean, completion: CompletionHandler) {
        _conversationListStore.muteConversation(conversationID = "group_$groupID", mute, completion)
    }

    fun setGroupMemberMuteTime(userID: String, time: Long, completion: CompletionHandler) {
        _groupSettingStore.setGroupMemberMuteTime(userID, time, completion)
    }

    fun setMuteAllMember(value: Boolean, completion: CompletionHandler) {
        _groupSettingStore.setMuteAllMembers(value, completion)
    }

    fun setSelfGroupNameCard(nameCard: String?, completion: CompletionHandler) {
        _groupSettingStore.setSelfGroupNameCard(nameCard, completion)
    }

    fun dismissGroup(completion: CompletionHandler) {
        _groupSettingStore.dismissGroup(completion)
        _conversationListStore.deleteConversation(conversationID = "group_$groupID")

    }

    fun quitGroup(completion: CompletionHandler) {
        _groupSettingStore.quitGroup(completion)
        _conversationListStore.deleteConversation(conversationID = "group_$groupID")
    }

    fun clearGroupChatHistory(completion: CompletionHandler) {
        _conversationListStore.clearConversationMessages(
            conversationID = "group_$groupID",
            completion
        )
    }


    fun toggleGroupDoNotDisturb() {
        val currentValue = isNotDisturb.value
        setGroupChatNotDisturb(!currentValue, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun toggleGroupTopChat() {
        val currentValue = isPinned.value
        setGroupChatPinned(!currentValue, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun toggleGroupAllMute() {
        val currentValue = isAllMuted.value
        setMuteAllMember(!currentValue, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun muteGroupMember(userID: String, muteTimeSeconds: Long) {
        setGroupMemberMuteTime(userID, muteTimeSeconds, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun updateGroupNickname(nickname: String) {
        setSelfGroupNameCard(nickname, object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
    }

    fun quitGroup() {
        quitGroup(object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun dismissGroup() {
        dismissGroup(object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun clearGroupChatHistoryConvenience() {
        clearGroupChatHistory(object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun setGroupAvatar(avatarUrl: String) {
        _groupSettingStore.updateGroupProfile(null, null, avatarUrl, object : CompletionHandler {
            override fun onSuccess() {

            }

            override fun onFailure(code: Int, desc: String) {

            }
        })
    }

    fun transferGroupOwner(newOwnerID: String) {
        _groupSettingStore.changeGroupOwner(newOwnerID, object : CompletionHandler {
            override fun onSuccess() {

            }

            override fun onFailure(code: Int, desc: String) {

            }
        })
    }

    fun setGroupName(name: String) {
        updateGroupProfile(name = name, completion = object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
    }

    fun setGroupNotice(notice: String) {
        updateGroupProfile(notice = notice, completion = object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
            }
        })
    }


    fun setJoinGroupApproveType(type: GroupJoinOption) {
        setGroupJoinOption(type, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun setInviteGroupApproveType(type: GroupJoinOption) {
        setGroupInviteOption(type, object : CompletionHandler {
            override fun onSuccess() {}
            override fun onFailure(code: Int, desc: String) {}
        })
    }

    fun setGroupNickname(nickname: String) {
        updateGroupNickname(nickname)
    }

}

// Factory classes
class C2CChatSettingViewModelFactory(val userID: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(C2CChatSettingViewModel::class.java)) {
            return C2CChatSettingViewModel(userID) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class GroupChatSettingViewModelFactory(val groupID: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupChatSettingViewModel::class.java)) {
            return GroupChatSettingViewModel(groupID) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


const val GROUP_AVATAR_URL =
    "https://im.sdk.qcloud.com/download/tuikit-resource/group-avatar/group_avatar_%s.png"
const val GROUP_AVATAR_COUNT = 24
const val CHAT_BACKGROUND_IMAGE =
    "https://im.sdk.qcloud.com/download/tuikit-resource/conversation-backgroundImage/backgroundImage_%s_full.png";
const val CHAT_BACKGROUND_COUNT = 7

const val USER_AVATAR_URL =
    "https://im.sdk.qcloud.com/download/tuikit-resource/avatar/avatar_%s.png"
const val USER_AVATAR_URL_COUNT = 26

fun getUserAvatarUrls(): List<String> {
    return mutableListOf<String>().apply {
        for (i in 1..USER_AVATAR_URL_COUNT) {
            add(String.format(USER_AVATAR_URL, i))
        }
    }
}

fun getGroupAvatarUrls(): List<String> {
    return mutableListOf<String>().apply {
        for (i in 1..GROUP_AVATAR_COUNT) {
            add(String.format(GROUP_AVATAR_URL, i))
        }
    }
}
