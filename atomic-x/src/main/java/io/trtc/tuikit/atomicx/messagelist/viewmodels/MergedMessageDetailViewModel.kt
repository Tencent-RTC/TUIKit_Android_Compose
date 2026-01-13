package io.trtc.tuikit.atomicx.messagelist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicxcore.api.message.MessageFetchOption
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageListStore
import io.trtc.tuikit.atomicxcore.api.message.MessageMediaFileType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MergedMessageDetailViewModel(
    val messageListStore: MessageListStore,
    val messageInfo: MessageInfo
) : ViewModel() {

    val messageListState = messageListStore.messageListState
    val messageList = messageListState.messageList.map {
        it.filter { item -> !item.msgID.isNullOrEmpty() }
            .distinctBy { item -> item.msgID }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        messageListStore.fetchMessageList(MessageFetchOption(message = messageInfo))
    }


    fun downloadThumbImage(messageInfo: MessageInfo) {
        messageListStore.downloadMessageResource(
            messageInfo,
            MessageMediaFileType.THUMB_IMAGE
        )
    }

    fun downloadSound(messageInfo: MessageInfo) {
        messageListStore.downloadMessageResource(
            messageInfo,
            MessageMediaFileType.SOUND
        )
    }

    fun downloadVideoSnapShot(messageInfo: MessageInfo) {
        messageListStore.downloadMessageResource(
            messageInfo,
            MessageMediaFileType.VIDEO_SNAPSHOT
        )
    }
}


class MergedMessageDetailViewModelFactory(
    private val messageListStore: MessageListStore,
    private val mergeMessage: MessageInfo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MergedMessageDetailViewModel(messageListStore, mergeMessage) as T
    }
}
