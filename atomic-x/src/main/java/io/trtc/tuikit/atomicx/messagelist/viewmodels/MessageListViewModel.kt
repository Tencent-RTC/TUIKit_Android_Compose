package io.trtc.tuikit.atomicx.messagelist.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tencent.imsdk.v2.V2TIMImageElem
import com.tencent.imsdk.v2.V2TIMValueCallback
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.audioplayer.AudioPlayer
import io.trtc.tuikit.atomicx.audioplayer.AudioPlayerListener
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Toast
import io.trtc.tuikit.atomicx.basecomponent.config.AppBuilderConfig
import io.trtc.tuikit.atomicx.basecomponent.utils.appContext
import io.trtc.tuikit.atomicx.imageviewer.ImageElement
import io.trtc.tuikit.atomicx.imageviewer.ImageViewer
import io.trtc.tuikit.atomicx.messagelist.config.ChatMessageListConfig
import io.trtc.tuikit.atomicx.messagelist.config.MessageListConfigProtocol
import io.trtc.tuikit.atomicx.messagelist.ui.AudioPlayingState
import io.trtc.tuikit.atomicx.messagelist.utils.AuxiliaryTextVisibilityStore
import io.trtc.tuikit.atomicx.messagelist.utils.ClipboardUtils
import io.trtc.tuikit.atomicx.messagelist.utils.DateTimeUtils.getIntervalSeconds
import io.trtc.tuikit.atomicx.messagelist.utils.DateTimeUtils.getTimeString
import io.trtc.tuikit.atomicx.messagelist.utils.FileUtils
import io.trtc.tuikit.atomicx.messagelist.utils.TranslationTextParser
import io.trtc.tuikit.atomicx.messagelist.utils.buildForwardAbstractItem
import io.trtc.tuikit.atomicx.messagelist.utils.getMessageTypeAbstract
import io.trtc.tuikit.atomicx.messagelist.utils.isGroupChat
import io.trtc.tuikit.atomicx.messagelist.utils.isGroupConversation
import io.trtc.tuikit.atomicx.messagelist.utils.senderDisplayName
import io.trtc.tuikit.atomicx.videoplayer.VideoData
import io.trtc.tuikit.atomicx.videoplayer.VideoPlayer
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationListStore
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationMarkType
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.atomicxcore.api.message.MergedForwardInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageActionStore
import io.trtc.tuikit.atomicxcore.api.message.MessageBody
import io.trtc.tuikit.atomicxcore.api.message.MessageEvent
import io.trtc.tuikit.atomicxcore.api.message.MessageFetchDirection
import io.trtc.tuikit.atomicxcore.api.message.MessageFetchOption
import io.trtc.tuikit.atomicxcore.api.message.MessageForwardOption
import io.trtc.tuikit.atomicxcore.api.message.MessageForwardType
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageInputStore
import io.trtc.tuikit.atomicxcore.api.message.MessageListStore
import io.trtc.tuikit.atomicxcore.api.message.MessageMediaFileType
import io.trtc.tuikit.atomicxcore.api.message.MessageStatus
import io.trtc.tuikit.atomicxcore.api.message.MessageType
import io.trtc.tuikit.atomicxcore.api.message.OfflinePushInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class MessageUIAction(
    var name: String,
    var dangerousAction: Boolean = false,
    var icon: Int = R.drawable.message_list_menu_more_icon,
    var action: (MessageInfo) -> Unit
)

data class LoadingState(
    val isLoadingOlder: Boolean = false,
    val isLoadingNewer: Boolean = false
)

const val messageAggregationTime: Int = 300
const val forwardMessageCountLimit = 30

class MessageListViewModel(
    private val messageListStore: MessageListStore,
    var locateMessage: MessageInfo? = null,
    private val messageListConfig: MessageListConfigProtocol = ChatMessageListConfig()
) : ViewModel() {
    val conversationID = messageListStore.conversationID
    val conversationListStore = ConversationListStore.create()
    val conversationListState = conversationListStore.conversationListState
    val messageListState = messageListStore.messageListState
    val messageEvent = messageListStore.messageEventFlow
    val messageInputStore = MessageInputStore.create(messageListStore.conversationID)

    private val auxiliaryTextVisibilityStore = AuxiliaryTextVisibilityStore()

    val messageList =
        messageListState.messageList.map { list ->
            list.asReversed().filter { item -> !item.msgID.isNullOrEmpty() }
                .distinctBy { item -> item.msgID }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val hasMoreOlderMessage = messageListState.hasMoreOlderMessage
    val hasMoreNewerMessage = messageListState.hasMoreNewerMessage

    var audioPlayingState = mutableStateOf(AudioPlayingState())
        private set

    var loadingState by mutableStateOf(LoadingState())
        private set

    private var audioPlayer: AudioPlayer? = null

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _selectedMessages = MutableStateFlow<Set<MessageInfo>>(emptySet())
    val selectedMessages: StateFlow<Set<MessageInfo>> = _selectedMessages.asStateFlow()

    private val _onSingleMessageForward = MutableStateFlow<MessageInfo?>(null)
    val onSingleMessageForward: StateFlow<MessageInfo?> = _onSingleMessageForward.asStateFlow()

    private val _longPressActionMessage = MutableStateFlow<MessageInfo?>(null)
    val longPressActionMessage: StateFlow<MessageInfo?> = _longPressActionMessage.asStateFlow()

    private val _readReceiptMessage = MutableStateFlow<MessageInfo?>(null)
    val readReceiptMessage: StateFlow<MessageInfo?> = _readReceiptMessage.asStateFlow()

    // Auxiliary text (ASR / Translation) state management
    private val _processingAuxiliaryTextMessageIds = MutableStateFlow<Set<String>>(emptySet())
    val processingAuxiliaryTextMessageIds: StateFlow<Set<String>> = _processingAuxiliaryTextMessageIds.asStateFlow()

    val hiddenAuxiliaryTextMessageIds: StateFlow<Set<String>> = auxiliaryTextVisibilityStore.hiddenMessageIds

    private val _asrTextMenuMessage = MutableStateFlow<MessageInfo?>(null)
    val asrTextMenuMessage: StateFlow<MessageInfo?> = _asrTextMenuMessage.asStateFlow()

    var forwardType = MessageForwardType.SEPARATE

    init {
        viewModelScope.launch {
            messageEvent.collect { event ->
                when (event) {
                    is MessageEvent.FetchMessages -> {
                        fetchMessageReactions(event.messageList)
                        delay(100)
                        locateMessage = null
                    }

                    is MessageEvent.FetchMoreMessages -> {
                        fetchMessageReactions(event.messageList)
                    }

                    is MessageEvent.RecvMessage -> {
                        fetchMessageReactions(listOf(event.message))
                    }

                    else -> {}
                }
            }
        }
        conversationListStore.fetchConversationInfo(messageListStore.conversationID)
        messageListStore.fetchMessageList(
            MessageFetchOption(
                message = locateMessage,
                direction = if (locateMessage != null) {
                    MessageFetchDirection.BOTH
                } else {
                    MessageFetchDirection.OLDER
                },
            )
        )
    }

    override fun onCleared() {
        destroyAudioPlayer()
        auxiliaryTextVisibilityStore.clear()
    }

    fun clearMessageReadCount() {
        conversationListStore.clearConversationUnreadCount(messageListStore.conversationID)
        conversationListStore.markConversation(
            listOf(messageListStore.conversationID),
            ConversationMarkType.UNREAD,
            false
        )
    }

    fun initializeAudioPlayer() {
        if (audioPlayer == null) {
            audioPlayer = AudioPlayer.create().apply {
                setListener(object : AudioPlayerListener {
                    override fun onPlay() {
                        audioPlayingState.value = audioPlayingState.value.copy(isPlaying = true)
                    }

                    override fun onPause() {
                        audioPlayingState.value = audioPlayingState.value.copy(isPlaying = false)
                    }

                    override fun onProgressUpdate(currentPosition: Int, duration: Int) {
                        audioPlayingState.value = audioPlayingState.value.copy(playPosition = currentPosition)
                    }

                    override fun onResume() {
                        audioPlayingState.value = audioPlayingState.value.copy(isPlaying = true)
                    }

                    override fun onCompletion() {
                        audioPlayingState.value = AudioPlayingState()
                    }
                })
            }
        }
    }

    fun playAudioMessage(message: MessageInfo) {
        val soundPath = message.messageBody?.soundPath
        if (soundPath != null && audioPlayer != null) {
            when {
                audioPlayingState.value.isPlaying && audioPlayingState.value.playingMessageId == message.msgID -> {
                    audioPlayer?.pause()
                }

                !audioPlayingState.value.isPlaying && audioPlayingState.value.playingMessageId == message.msgID -> {
                    audioPlayer?.resume()
                }

                else -> {
                    if (audioPlayingState.value.isPlaying) {
                        audioPlayer?.stop()
                    }
                    audioPlayingState.value = audioPlayingState.value.copy(playingMessageId = message.msgID)
                    audioPlayer?.play(soundPath)
                }
            }
        }
    }

    fun destroyAudioPlayer() {
        audioPlayer?.stop()
        audioPlayer = null
        audioPlayingState.value = AudioPlayingState()
    }


    @Composable
    fun getActions(messageInfo: MessageInfo): List<MessageUIAction> {
        // Get the latest message from messageList to ensure we have updated asrText/translatedText
        val latestMessage = messageList.value
            .find { it.msgID == messageInfo.msgID } ?: messageInfo
        val messageActionStore = MessageActionStore.create(latestMessage)
        val context = LocalContext.current
        val actions = mutableListOf<MessageUIAction>()

        if (messageListConfig.isSupportMultiSelect) {
            actions.add(
                MessageUIAction(
                    name = context.getString(R.string.message_list_menu_multi_select),
                    icon = R.drawable.message_list_menu_multi_select_icon,
                    action = {
                        enterMultiSelectMode(messageInfo)
                    }
                )
            )
        }

        if (messageListConfig.isSupportForward && messageInfo.status == MessageStatus.SEND_SUCCESS) {
            actions.add(
                MessageUIAction(
                    name = context.getString(R.string.message_list_menu_forward),
                    icon = R.drawable.message_list_menu_forward_icon,
                    action = { messageInfo ->
                        _onSingleMessageForward.value = messageInfo
                    }
                )
            )
        }

        if (latestMessage.messageType == MessageType.TEXT && messageListConfig.isSupportCopy) {
            actions.add(
                MessageUIAction(
                    name = context.getString(R.string.message_list_menu_copy),
                    icon = R.drawable.message_list_menu_copy_icon,
                    action = { msg ->
                        ClipboardUtils.copyText(context, "Copied Text", msg.messageBody?.text)
                    })
            )
        }
        if (latestMessage.isSelf && messageInfo.status == MessageStatus.SEND_SUCCESS) {
            val currentTime = System.currentTimeMillis()
            val messageTime = latestMessage.timestamp ?: 0L
            val timeDifferenceSeconds = (currentTime - messageTime * 1000) / 1000

            if (timeDifferenceSeconds <= 120 && messageListConfig.isSupportRecall) {
                actions.add(
                    MessageUIAction(
                        name = context.getString(R.string.message_list_menu_recall),
                        icon = R.drawable.message_list_menu_recall_icon,
                        action = {
                            messageActionStore.recallMessage()
                        })
                )
            }
        }
        if (messageListConfig.isSupportDelete) {
            actions.add(
                MessageUIAction(
                    name = context.getString(R.string.message_list_menu_delete),
                    dangerousAction = true,
                    icon = R.drawable.message_list_menu_delete_icon,
                    action = {
                        messageActionStore.deleteMessage()
                    })
            )
        }

        if (latestMessage.isSelf && latestMessage.needReadReceipt && isGroupChat(conversationID) && messageInfo.status == MessageStatus.SEND_SUCCESS) {
            actions.add(
                MessageUIAction(
                    name = context.getString(R.string.message_list_menu_info),
                    icon = R.drawable.message_list_menu_info_icon,
                    action = { msg ->
                        showReadReceiptDialog(msg)
                    }
                )
            )
        }

        // Voice to Text menu item
        if (latestMessage.messageType == MessageType.SOUND && latestMessage.status == MessageStatus.SEND_SUCCESS) {
            val asrText = latestMessage.messageBody?.asrText
            val msgID = latestMessage.msgID ?: ""
            val isHidden = isAsrTextHidden(msgID)
            // Show menu item if asrText is empty or hidden
            if (asrText.isNullOrEmpty() || isHidden) {
                actions.add(
                    MessageUIAction(
                        name = context.getString(R.string.message_list_menu_convert_to_text),
                        icon = R.drawable.message_list_menu_convert_icon,
                        action = { msg ->
                            convertVoiceToText(msg)
                        }
                    )
                )
            }
        }

        // Text Translation menu item
        if (latestMessage.messageType == MessageType.TEXT && latestMessage.status == MessageStatus.SEND_SUCCESS) {
            val translatedText = latestMessage.messageBody?.translatedText
            val msgID = latestMessage.msgID ?: ""
            val isHidden = isTranslationHidden(msgID)
            // Show menu item if translatedText is empty or hidden
            if (translatedText.isNullOrEmpty() || isHidden) {
                actions.add(
                    MessageUIAction(
                        name = context.getString(R.string.message_list_menu_translate),
                        icon = R.drawable.message_list_menu_translate_icon,
                        action = { msg ->
                            translateText(msg)
                        }
                    )
                )
            }
        }

        return actions
    }


    fun loadMoreOlderMessage() {
        if (loadingState.isLoadingOlder || !hasMoreOlderMessage.value) {
            return
        }

        loadingState = loadingState.copy(isLoadingOlder = true)
        messageListStore.fetchMoreMessageList(MessageFetchDirection.OLDER, object : CompletionHandler {
            override fun onSuccess() {
                Log.e("MessageList", "loadMoreOlderMessage success")
                loadingState = loadingState.copy(isLoadingOlder = false)
            }

            override fun onFailure(code: Int, desc: String) {
                Log.e("MessageList", "loadMoreOlderMessage failure " + code + " " + desc)
                loadingState = loadingState.copy(isLoadingOlder = false)
            }
        })
    }

    fun loadMoreNewerMessage() {
        if (loadingState.isLoadingNewer || !hasMoreNewerMessage.value) {
            return
        }

        loadingState = loadingState.copy(isLoadingNewer = true)
        messageListStore.fetchMoreMessageList(MessageFetchDirection.NEWER, object : CompletionHandler {
            override fun onSuccess() {
                loadingState = loadingState.copy(isLoadingNewer = false)
            }

            override fun onFailure(code: Int, desc: String) {
                loadingState = loadingState.copy(isLoadingNewer = false)
            }
        })
    }

    fun downloadFile(messageInfo: MessageInfo) {
        messageListStore.downloadMessageResource(
            messageInfo,
            MessageMediaFileType.FILE,
            object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
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


    fun checkNextMessageIsAggregation(index: Int): Boolean {
        val message = messageList.value.getOrNull(index)
        val nextMessage = messageList.value.getOrNull(index - 1)
        if (message?.sender == nextMessage?.sender) {
            val timeInterval = getIntervalSeconds(message?.timestamp, nextMessage?.timestamp)
            if (timeInterval > messageAggregationTime) {
                return false
            }
            if (nextMessage?.status == MessageStatus.RECALLED) {
                return false
            }
            return true
        }
        return false
    }

    fun checkPreviousMessageIsAggregation(index: Int): Boolean {
        val message = messageList.value.getOrNull(index)
        val prevMessage = messageList.value.getOrNull(index + 1)
        if (message?.sender == prevMessage?.sender) {
            val timeInterval = getIntervalSeconds(message?.timestamp, prevMessage?.timestamp)
            if (timeInterval > messageAggregationTime) {
                return false
            }
            if (prevMessage?.status == MessageStatus.RECALLED) {
                return false
            }
            return true
        }
        return false
    }

    fun getMessageTimeString(index: Int): String? {
        val message = messageList.value.getOrNull(index)
        if (index == messageList.value.lastIndex) {
            return getTimeString(message?.timestamp?.times(1000))
        }
        val prevMessage = messageList.value.getOrNull(index + 1)
        if (message != null && prevMessage != null) {
            val timeInterval = getIntervalSeconds(message.timestamp, prevMessage.timestamp)
            if (timeInterval > messageAggregationTime) {
                return getTimeString(message.timestamp?.times(1000))
            }
        }
        return null
    }

    fun retrySendMessage(context: Context?, message: MessageInfo) {
        if (message.offlinePushInfo == null) {
            message.offlinePushInfo = createOfflinePushInfoForConversation(conversationID, message)
        }

        messageInputStore.sendMessage(message, object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
                context?.let { Toast.error(context, context.getString(R.string.message_input_send_failed)) }
            }
        })
    }

    private fun enterMultiSelectMode(initialMessage: MessageInfo? = null) {
        _isMultiSelectMode.value = true
        if (initialMessage != null) {
            _selectedMessages.value = setOf(initialMessage)
        } else {
            _selectedMessages.value = emptySet()
        }
    }

    fun exitMultiSelectMode() {
        _isMultiSelectMode.value = false
        _selectedMessages.value = emptySet()
    }

    fun toggleMessageSelection(message: MessageInfo) {
        val currentSet = _selectedMessages.value
        _selectedMessages.value = if (currentSet.contains(message)) {
            currentSet - message
        } else {
            currentSet + message
        }
    }

    fun clearSingleMessageForward() {
        _onSingleMessageForward.value = null
    }

    fun showReadReceiptDialog(message: MessageInfo) {
        _readReceiptMessage.value = message
    }

    fun clearReadReceiptDialog() {
        _readReceiptMessage.value = null
    }

    fun showLongPressActionDialog(message: MessageInfo) {
        _longPressActionMessage.value = message
    }

    fun clearLongPressActionDialog() {
        _longPressActionMessage.value = null
    }

    fun forwardMessages(
        messageList: List<MessageInfo>,
        conversationIDList: List<String>,
        completion: CompletionHandler? = null
    ) {
        // Sort messages by their order in the UI message list (older messages first)
        val currentMessageList = this.messageList.value
        val messageIdToIndex = currentMessageList.withIndex().associate { it.value.msgID to it.index }
        val sortedMessageList = messageList.sortedByDescending { messageIdToIndex[it.msgID] ?: Int.MAX_VALUE }

        conversationIDList.forEach { tempConversationID ->

            val mergedForwardInfo = when (forwardType) {
                MessageForwardType.MERGED -> MergedForwardInfo(
                    title = getForwardMessageTitle(sortedMessageList) ?: "",
                    abstractList = getAbstractList(sortedMessageList),
                    compatibleText = appContext.getString(R.string.message_list_forward_compatible_text),
                    needReadReceipt = AppBuilderConfig.enableReadReceipt,
                    supportExtension = false,
                    offlinePushInfo = createOfflinePushInfoForMultiConversation(
                        appContext.getString(R.string.message_list_message_type_merged), tempConversationID
                    )
                )

                else -> null
            }
            sortedMessageList.forEach {
                it.needReadReceipt = AppBuilderConfig.enableReadReceipt
                it.supportExtension = false
                it.offlinePushInfo =
                    createOfflinePushInfoForMultiConversation(getMessageTypeAbstract(it), tempConversationID)
            }
            messageListStore.forwardMessages(
                messageList = sortedMessageList,
                forwardOption = MessageForwardOption(
                    forwardType = forwardType,
                    mergedForwardInfo = mergedForwardInfo,
                ),
                conversationID = tempConversationID,
                completion = completion
            )

        }
    }

    private fun getForwardMessageTitle(messageList: List<MessageInfo>): String? {
        return if (isGroupConversation(messageListStore.conversationID)) {
            appContext.getString(R.string.message_list_forward_chats)
        } else {
            val loginUserInfo = LoginStore.shared.loginState.loginUserInfo.value
            val selfName = loginUserInfo?.nickname ?: loginUserInfo?.userID ?: ""

            val chatName = conversationListState.conversationList.value
                .firstOrNull { it.conversationID == messageListStore.conversationID }
                ?.title ?: run {
                val firstMessage = messageList.firstOrNull()
                if (firstMessage?.isSelf == true) {
                    firstMessage.receiver ?: firstMessage.rawMessage?.userID ?: ""
                } else {
                    firstMessage?.senderDisplayName ?: ""
                }
            }

            selfName + appContext.getString(R.string.message_list_and_text) + chatName + appContext.getString(
                R.string.message_list_forward_chats_c2c
            )
        }
    }

    private fun getAbstractList(messageList: List<MessageInfo>): List<String> {
        val messages = messageList.take(3)
        return messages.map { message ->
            val userName = message.senderDisplayName
            val messageAbstract = getMessageTypeAbstract(message)
            buildForwardAbstractItem(userName, messageAbstract)
        }
    }

    fun downloadOrShowVideo(context: Context, messageInfo: MessageInfo) {
        viewModelScope.launch {
            try {
                val localVideoPath = messageInfo.messageBody?.videoPath
                if (!localVideoPath.isNullOrEmpty()) {
                    Log.d("MessageListViewModel", "Using local video path: $localVideoPath")
                    showVideoFromPath(context, messageInfo, localVideoPath)
                    return@launch
                }

                val imMessage = messageInfo.rawMessage
                if (imMessage?.videoElem != null) {
                    Log.d("MessageListViewModel", "Getting video URL from network")
                    imMessage.videoElem.getVideoUrl(object : V2TIMValueCallback<String> {
                        override fun onSuccess(videoUrl: String?) {
                            if (!videoUrl.isNullOrEmpty()) {
                                Log.d("MessageListViewModel", "Got video URL: $videoUrl")
                                showVideoFromUrl(context, messageInfo, videoUrl)
                            } else {
                                Log.w("MessageListViewModel", "Video URL is empty")
                            }
                        }

                        override fun onError(code: Int, desc: String?) {
                        }
                    })
                } else {
                    Log.e("MessageListViewModel", "Video element is null")
                }
            } catch (e: Exception) {
                Log.e("MessageListViewModel", "Error in downloadOrShowVideo", e)

            }
        }
    }

    fun deleteSelectedMessages() {
        messageListStore.deleteMessages(selectedMessages.value.toList())
    }

    private fun showVideoFromPath(context: Context, messageInfo: MessageInfo, videoPath: String) {
        try {
            val uri = FileUtils.getUriFromPath(context, videoPath)
            if (uri != null) {
                val width = messageInfo.messageBody?.videoSnapshotWidth ?: 0
                val height = messageInfo.messageBody?.videoSnapshotHeight ?: 0

                Log.d(
                    "MessageListViewModel",
                    "Starting VideoPlayerActivity with local path: $videoPath"
                )
                VideoPlayer.play(VideoData(uri, width = width, height = height))
            } else {
                Log.e("MessageListViewModel", "Failed to get URI for video path: $videoPath")
            }
        } catch (e: Exception) {
            Log.e("MessageListViewModel", "Error showing video from path", e)
        }
    }

    private fun showVideoFromUrl(context: Context, messageInfo: MessageInfo, videoUrl: String) {
        try {
            val uri = android.net.Uri.parse(videoUrl)
            val width = messageInfo.messageBody?.videoSnapshotWidth ?: 0
            val height = messageInfo.messageBody?.videoSnapshotHeight ?: 0

            Log.d(
                "MessageListViewModel",
                "Starting VideoPlayerActivity with network URL: $videoUrl"
            )
            VideoPlayer.play(VideoData(uri, width = width, height = height))
        } catch (e: Exception) {
            Log.e("MessageListViewModel", "Error showing video from URL", e)
        }
    }

    fun showImage(context: Context, messageInfo: MessageInfo) {
        viewModelScope.launch {
            try {
                var initData: ImageElement? = null

                val dataList = mutableListOf<ImageElement>()

                createImageMediaData(messageInfo)?.let { mediaData ->
                    dataList.add(mediaData)
                    initData = mediaData
                }

                if (dataList.isNotEmpty()) {
                    Log.d(
                        "MessageListViewModel",
                        "Opening ImageViewer with ${dataList.size} media items"
                    )
                    withContext(Dispatchers.Main) {
                        ImageViewer.view(
                            imageElements = dataList.reversed(),
                            initialIndex = dataList.indexOf(initData)
                        )
                    }
                } else {
                    Log.w("MessageListViewModel", "No media data available to show")
                    withContext(Dispatchers.Main) {
                    }
                }
            } catch (e: Exception) {
                Log.e("MessageListViewModel", "Error showing media", e)
            }
        }
    }

    private suspend fun createImageMediaData(message: MessageInfo): ImageElement? {
        return withContext(Dispatchers.IO) {
            val localOriginPath = message.messageBody?.originalImagePath
            val originUrl =
                message.rawMessage?.imageElem?.imageList?.find { it.type == V2TIMImageElem.V2TIM_IMAGE_TYPE_ORIGIN }?.url
            return@withContext ImageElement(
                type = 0,
                data = if (!localOriginPath.isNullOrEmpty()) localOriginPath else originUrl,
                width = message.messageBody?.originalImageWidth ?: 0,
                height = message.messageBody?.originalImageHeight ?: 0
            )
        }
    }

    fun sendReadReceipts(messages: List<MessageInfo>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val filteredMessages = messages.filter { message ->
                    !message.isSelf && message.needReadReceipt
                }

                if (filteredMessages.isNotEmpty()) {
                    messageListStore.sendMessageReadReceipts(filteredMessages)
                }
            }
        }
    }

    // region Reaction

    private val _reactionDetailMessage = MutableStateFlow<MessageInfo?>(null)
    val reactionDetailMessage: StateFlow<MessageInfo?> = _reactionDetailMessage.asStateFlow()

    private val _showEmojiPickerForMessage = MutableStateFlow<MessageInfo?>(null)
    val showEmojiPickerForMessage: StateFlow<MessageInfo?> = _showEmojiPickerForMessage.asStateFlow()

    private var currentReactionActionStore: MessageActionStore? = null

    fun getCurrentUserID(): String? {
        return LoginStore.shared.loginState.loginUserInfo.value?.userID
    }

    fun fetchMessageReactions(messages: List<MessageInfo>) {
        if (!messageListConfig.isSupportReaction) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                messageListStore.fetchMessageReactions(messages, 10)
            }
        }
    }

    fun addMessageReaction(message: MessageInfo, reactionID: String) {
        viewModelScope.launch {
            val actionStore = MessageActionStore.create(message)
            actionStore.addMessageReaction(reactionID, object : CompletionHandler {
                override fun onSuccess() {
                    Log.d("MessageListViewModel", "Add reaction success: $reactionID")
                }

                override fun onFailure(code: Int, desc: String) {
                    Log.e("MessageListViewModel", "Add reaction failed: $code $desc")
                }
            })
        }
    }

    fun removeMessageReaction(message: MessageInfo, reactionID: String) {
        viewModelScope.launch {
            val actionStore = MessageActionStore.create(message)
            actionStore.removeMessageReaction(reactionID, object : CompletionHandler {
                override fun onSuccess() {
                    Log.d("MessageListViewModel", "Remove reaction success: $reactionID")
                }

                override fun onFailure(code: Int, desc: String) {
                    Log.e("MessageListViewModel", "Remove reaction failed: $code $desc")
                }
            })
        }
    }

    fun showReactionDetail(message: MessageInfo) {
        _reactionDetailMessage.value = message
        currentReactionActionStore = MessageActionStore.create(message)
    }

    fun clearReactionDetail() {
        _reactionDetailMessage.value = null
        currentReactionActionStore = null
    }

    fun fetchReactionUsers(reactionID: String) {
        currentReactionActionStore?.fetchMessageReactionUsers(reactionID, 20, object : CompletionHandler {
            override fun onSuccess() {
                Log.d("MessageListViewModel", "Fetch reaction users success")
            }

            override fun onFailure(code: Int, desc: String) {
                Log.e("MessageListViewModel", "Fetch reaction users failed: $code $desc")
            }
        })
    }

    fun showEmojiPicker(message: MessageInfo) {
        _showEmojiPickerForMessage.value = message
    }

    fun clearEmojiPicker() {
        _showEmojiPickerForMessage.value = null
    }

    // endregion

    // region Voice to Text (ASR)

    private val _shouldScrollToBottomAfterAsr = MutableStateFlow(false)
    val shouldScrollToBottomAfterAsr: StateFlow<Boolean> = _shouldScrollToBottomAfterAsr.asStateFlow()

    fun clearScrollToBottomAfterAsr() {
        _shouldScrollToBottomAfterAsr.value = false
    }

    fun convertVoiceToText(message: MessageInfo) {
        val msgID = message.msgID ?: return
        if (_processingAuxiliaryTextMessageIds.value.contains(msgID)) return

        // Check if this is the last message (first in reversed list)
        val isLastMessage = messageList.value.firstOrNull()?.msgID == msgID

        // Clear hidden state and start converting
        auxiliaryTextVisibilityStore.unhide(msgID)
        _processingAuxiliaryTextMessageIds.value = _processingAuxiliaryTextMessageIds.value + msgID

        viewModelScope.launch {
            val actionStore = MessageActionStore.create(message)
            actionStore.convertVoiceToText("", object : CompletionHandler {
                override fun onSuccess() {
                    Log.d("MessageListViewModel", "Voice to text conversion success for $msgID")
                    _processingAuxiliaryTextMessageIds.value = _processingAuxiliaryTextMessageIds.value - msgID

                    // Trigger scroll to bottom if this was the last message
                    if (isLastMessage) {
                        _shouldScrollToBottomAfterAsr.value = true
                    }
                }

                override fun onFailure(code: Int, desc: String) {
                    Log.e("MessageListViewModel", "Voice to text conversion failed: $code $desc")
                    _processingAuxiliaryTextMessageIds.value = _processingAuxiliaryTextMessageIds.value - msgID
                    Toast.error(appContext, appContext.getString(R.string.message_list_convert_to_text_failed))
                }
            })
        }
    }

    fun isMessageProcessingAuxiliaryText(msgID: String): Boolean {
        return _processingAuxiliaryTextMessageIds.value.contains(msgID)
    }

    fun isAsrTextHidden(msgID: String): Boolean {
        return auxiliaryTextVisibilityStore.isHidden(msgID)
    }

    fun hideAsrText(message: MessageInfo) {
        val msgID = message.msgID ?: return
        auxiliaryTextVisibilityStore.hide(msgID)
        clearAsrTextMenu()
    }

    fun showAsrTextMenu(message: MessageInfo) {
        _asrTextMenuMessage.value = message
    }

    fun clearAsrTextMenu() {
        _asrTextMenuMessage.value = null
    }

    fun copyAsrText(message: MessageInfo, context: Context) {
        val asrText = message.messageBody?.asrText ?: return
        ClipboardUtils.copyText(context, "ASR Text", asrText)
        Toast.success(context, context.getString(R.string.message_list_copied))
        clearAsrTextMenu()
    }

    fun forwardAsrText(message: MessageInfo) {
        val asrText = message.messageBody?.asrText
        if (!asrText.isNullOrEmpty()) {
            _auxiliaryTextForwardContent.value = asrText
        }
        clearAsrTextMenu()
    }

    // region Auxiliary Text Forward (ASR / Translation)

    private val _auxiliaryTextForwardContent = MutableStateFlow<String?>(null)
    val auxiliaryTextForwardContent: StateFlow<String?> = _auxiliaryTextForwardContent.asStateFlow()

    fun clearAuxiliaryTextForward() {
        _auxiliaryTextForwardContent.value = null
    }

    fun sendAuxiliaryTextToConversations(
        text: String,
        conversationIDList: List<String>,
        completion: CompletionHandler? = null
    ) {
        viewModelScope.launch {
            var successCount = 0
            var failureCount = 0

            for (conversationID in conversationIDList) {
                val inputStore = MessageInputStore.create(conversationID)
                val message = MessageInfo().apply {
                    messageType = MessageType.TEXT
                    messageBody = MessageBody().apply {
                        this.text = text
                    }
                    needReadReceipt = AppBuilderConfig.enableReadReceipt
                    offlinePushInfo = createOfflinePushInfoForConversation(conversationID, this)
                }
                inputStore.sendMessage(message, object : CompletionHandler {
                    override fun onSuccess() {
                        successCount++
                        if (successCount + failureCount == conversationIDList.size) {
                            if (failureCount == 0) {
                                completion?.onSuccess()
                            } else {
                                completion?.onFailure(-1, "Some messages failed to send")
                            }
                        }
                    }

                    override fun onFailure(code: Int, desc: String) {
                        failureCount++
                        if (successCount + failureCount == conversationIDList.size) {
                            completion?.onFailure(code, desc)
                        }
                    }
                })
            }
        }
    }

    private val _shouldScrollToBottomAfterTranslation = MutableStateFlow(false)
    val shouldScrollToBottomAfterTranslation: StateFlow<Boolean> = _shouldScrollToBottomAfterTranslation.asStateFlow()

    fun clearScrollToBottomAfterTranslation() {
        _shouldScrollToBottomAfterTranslation.value = false
    }

    fun translateText(message: MessageInfo) {
        val msgID = message.msgID ?: return
        if (_processingAuxiliaryTextMessageIds.value.contains(msgID)) return

        // Check if this is the last message (first in reversed list)
        val isLastMessage = messageList.value.firstOrNull()?.msgID == msgID

        // Clear hidden state and start translating
        auxiliaryTextVisibilityStore.unhide(msgID)
        _processingAuxiliaryTextMessageIds.value = _processingAuxiliaryTextMessageIds.value + msgID

        viewModelScope.launch {
            val actionStore = MessageActionStore.create(message)
            // Get text to translate
            val text = message.messageBody?.text
            if (text.isNullOrEmpty()) {
                _processingAuxiliaryTextMessageIds.value = _processingAuxiliaryTextMessageIds.value - msgID
                return@launch
            }

            // Parse text to separate emoji and @ from translatable text
            val splitResult = TranslationTextParser.splitTextByEmojiAndAtUsers(text, null)
            val textArray =
                splitResult?.get(TranslationTextParser.KEY_SPLIT_STRING_TEXT) as? List<String> ?: emptyList()

            // If nothing to translate (pure emoji/@ message), just show original
            if (textArray.isEmpty()) {
                _processingAuxiliaryTextMessageIds.value = _processingAuxiliaryTextMessageIds.value - msgID
                if (isLastMessage) {
                    _shouldScrollToBottomAfterTranslation.value = true
                }
                return@launch
            }

            val targetLanguage = AppBuilderConfig.translateTargetLanguage

            actionStore.translateText(textArray, null, targetLanguage, object : CompletionHandler {
                override fun onSuccess() {
                    Log.d("MessageListViewModel", "Text translation success for $msgID")
                    _processingAuxiliaryTextMessageIds.value = _processingAuxiliaryTextMessageIds.value - msgID
                    // Trigger scroll to bottom if this was the last message
                    if (isLastMessage) {
                        _shouldScrollToBottomAfterTranslation.value = true
                    }
                }

                override fun onFailure(code: Int, desc: String) {
                    Log.e("MessageListViewModel", "Text translation failed: $code $desc")
                    _processingAuxiliaryTextMessageIds.value = _processingAuxiliaryTextMessageIds.value - msgID
                    Toast.error(appContext, appContext.getString(R.string.message_list_translate_failed))
                }
            })
        }
    }

    fun isTranslationHidden(msgID: String): Boolean {
        return auxiliaryTextVisibilityStore.isHidden(msgID)
    }

    fun hideTranslation(message: MessageInfo) {
        val msgID = message.msgID ?: return
        auxiliaryTextVisibilityStore.hide(msgID)
    }

    fun copyTranslatedText(message: MessageInfo, context: Context) {
        val translatedText = getTranslatedDisplayText(message) ?: return
        ClipboardUtils.copyText(context, "Translated Text", translatedText)
        Toast.success(context, context.getString(R.string.message_list_copied))
    }

    fun forwardTranslatedText(message: MessageInfo) {
        val translatedText = getTranslatedDisplayText(message)
        if (!translatedText.isNullOrEmpty()) {
            _auxiliaryTextForwardContent.value = translatedText
        }
    }

    /**
     * Get the translated display text for a message.
     * This reconstructs the full translated text including emoji and @ mentions.
     */
    fun getTranslatedDisplayText(message: MessageInfo): String? {
        val translatedTextMap = message.messageBody?.translatedText
        if (translatedTextMap.isNullOrEmpty()) return null

        val originalText = message.messageBody?.text ?: return null

        // Parse original text to get structure
        val splitResult = TranslationTextParser.splitTextByEmojiAndAtUsers(originalText, null)
        val resultArray =
            splitResult?.get(TranslationTextParser.KEY_SPLIT_STRING_RESULT) as? List<String> ?: return null
        val textIndexArray = splitResult[TranslationTextParser.KEY_SPLIT_STRING_TEXT_INDEX] as? List<Int> ?: return null

        // Reconstruct with translated text
        return TranslationTextParser.replacedStringWithArray(resultArray, textIndexArray, translatedTextMap)
    }

    private fun createOfflinePushInfoForConversation(conversationID: String, message: MessageInfo): OfflinePushInfo {
        val loginUserInfo = LoginStore.shared.loginState.loginUserInfo.value
        val selfUserId = loginUserInfo?.userID.orEmpty()
        val selfName = loginUserInfo?.nickname ?: selfUserId

        val isGroup = isGroupConversation(conversationID)
        val groupId = if (isGroup) conversationID.removePrefix("group_") else ""

        val chatName = conversationListState.conversationList.value
            .firstOrNull { it.conversationID == conversationID }
            ?.title
            ?.takeIf { it.isNotBlank() }

        val title = if (isGroup) chatName ?: groupId else selfName
        val description = trimPushDescription(getMessageTypeAbstract(message))

        val ext = createOfflinePushExtJson(
            isGroup = isGroup,
            description = description,
            senderId = if (isGroup) groupId else selfUserId,
            senderNickName = title,
            faceUrl = loginUserInfo?.avatarURL
        )

        val extensionInfo: Map<String, Any> = mapOf(
            "ext" to ext,
            "AndroidOPPOChannelID" to "tuikit",
            "AndroidHuaWeiCategory" to "IM",
            "AndroidVIVOCategory" to "IM",
            "AndroidHonorImportance" to "NORMAL",
            "AndroidMeizuNotifyType" to 1,
            "iOSInterruptionLevel" to "time-sensitive",
            "enableIOSBackgroundNotification" to false
        )

        return OfflinePushInfo(
            title = title,
            description = description,
            extensionInfo = extensionInfo
        )
    }

    private fun createOfflinePushInfoForMultiConversation(
        description: String,
        conversationID: String
    ): OfflinePushInfo {

        val loginUserInfo = LoginStore.shared.loginState.loginUserInfo.value
        val selfUserId = loginUserInfo?.userID.orEmpty()

        val title = loginUserInfo?.nickname ?: selfUserId
        val isGroup = isGroupConversation(conversationID)
        val groupId = if (isGroup) conversationID.removePrefix("group_") else ""

        val ext = createOfflinePushExtJson(
            isGroup = isGroup,
            description = description,
            senderId = if (isGroup) groupId else selfUserId,
            senderNickName = title,
            faceUrl = loginUserInfo?.avatarURL
        )
        val extensionInfo: Map<String, Any> = mapOf(
            "ext" to ext,
            "AndroidOPPOChannelID" to "tuikit",
            "AndroidHuaWeiCategory" to "IM",
            "AndroidVIVOCategory" to "IM",
            "AndroidHonorImportance" to "NORMAL",
            "AndroidMeizuNotifyType" to 1,
            "iOSInterruptionLevel" to "time-sensitive",
            "enableIOSBackgroundNotification" to false
        )

        return OfflinePushInfo(
            title = title,
            description = trimPushDescription(description),
            extensionInfo = extensionInfo
        )
    }

    private fun trimPushDescription(text: String, maxLength: Int = 50): String {
        val normalized = text.trim().replace("\n", " ").replace("\r", " ")
        if (normalized.length <= maxLength) return normalized
        return normalized.substring(0, maxLength)
    }

    private fun createOfflinePushExtJson(
        isGroup: Boolean,
        description: String,
        senderId: String,
        senderNickName: String,
        faceUrl: String?,
    ): String {
        val businessInfo = JSONObject().apply {
            putOpt("content", description.takeIf { it.isNotEmpty() })
            putOpt("sender", senderId)
            putOpt("faceUrl", faceUrl)
            putOpt("nickname", senderNickName)
            putOpt("chatType", if (isGroup) 2 else 1)
        }

        val configInfo = JSONObject().apply {
            putOpt("fcmPushType", 0)
            putOpt("fcmNotificationType", 0)
        }

        return JSONObject()
            .putOpt("entity", businessInfo)
            .putOpt("timPushFeatures", configInfo)
            .toString()
    }

}
