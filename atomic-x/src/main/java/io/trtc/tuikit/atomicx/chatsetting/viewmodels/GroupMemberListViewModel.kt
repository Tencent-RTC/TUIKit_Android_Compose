package io.trtc.tuikit.atomicx.chatsetting.viewmodels

import androidx.lifecycle.ViewModel
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.contact.GroupMemberRole
import io.trtc.tuikit.atomicxcore.api.contact.GroupSettingStore

class GroupMemberListViewModel(private val groupID: String) : ViewModel() {

    private val groupMemberListStore = GroupSettingStore.create(groupID)
    private val groupMemberListState = groupMemberListStore.groupSettingState

    val members = groupMemberListState.allMembers
    private var isLoadingMore = false

    init {
        groupMemberListStore.fetchGroupMemberList(
            role = GroupMemberRole.ALL,
            object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
    }

    fun loadMoreMembers() {
        if (isLoadingMore) return
        isLoadingMore = true
        groupMemberListStore.fetchMoreGroupMemberList(object : CompletionHandler {
            override fun onSuccess() {
                isLoadingMore = false
            }

            override fun onFailure(code: Int, desc: String) {
                isLoadingMore = false
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
    }
} 