package io.trtc.tuikit.atomicx.messagelist.viewmodels

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
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
import io.trtc.tuikit.atomicx.basecomponent.config.MessageAction
import io.trtc.tuikit.atomicx.imageviewer.ImageElement
import io.trtc.tuikit.atomicx.imageviewer.ImageViewer
import io.trtc.tuikit.atomicx.messagelist.config.MessageListConfig
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRendererRegistry
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.CreateGroupMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.FaceMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.FileMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.ImageMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.SoundMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.SystemMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.TextMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.VideoMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.utils.FileUtils
import io.trtc.tuikit.atomicx.messagelist.utils.collectAsState
import io.trtc.tuikit.atomicx.videoplayer.VideoData
import io.trtc.tuikit.atomicx.videoplayer.VideoPlayer
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.ConversationListStore
import io.trtc.tuikit.atomicxcore.api.MessageActionStore
import io.trtc.tuikit.atomicxcore.api.MessageFetchDirection
import io.trtc.tuikit.atomicxcore.api.MessageFetchOption
import io.trtc.tuikit.atomicxcore.api.MessageInfo
import io.trtc.tuikit.atomicxcore.api.MessageInputStore
import io.trtc.tuikit.atomicxcore.api.MessageListChangeReason
import io.trtc.tuikit.atomicxcore.api.MessageListStore
import io.trtc.tuikit.atomicxcore.api.MessageMediaFileType
import io.trtc.tuikit.atomicxcore.api.MessageStatus
import io.trtc.tuikit.atomicxcore.api.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class MessageUIAction(
    var name: String,
    var dangerousAction: Boolean = false,
    var icon: Int = R.drawable.message_list_menu_more_icon,
    var action: (MessageInfo) -> Unit
)

data class AudioPlayingState(
    val isPlaying: Boolean = false,
    val playingMessageId: String? = null,
    val playPosition: Int = 0
)

data class LoadingState(
    val isLoadingOlder: Boolean = false,
    val isLoadingNewer: Boolean = false
)

class MessageListViewModel(
    private val messageListStore: MessageListStore,
    private val messageActionStore: MessageActionStore,
    var locateMessage: MessageInfo?
) : ViewModel() {
    val conversationListStore = ConversationListStore.create()
    val messageListState = messageListStore.messageListState
    val messageListChangeSource by collectAsState(messageListState.messageListChangeReason)
    val messageInputStore = MessageInputStore.create(messageListStore.conversationID)
    val messageList =
        messageListState.messageList.map {
            it.asReversed().filter { item -> !item.msgID.isNullOrEmpty() }
                .distinctBy { item -> item.msgID }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val hasMoreOlderMessage by collectAsState(messageListState.hasMoreOlderMessage)
    val hasMoreNewerMessage by collectAsState(messageListState.hasMoreNewerMessage)

    var audioPlayingState by mutableStateOf(AudioPlayingState())
        private set

    var loadingState by mutableStateOf(LoadingState())
        private set

    private var audioPlayer: AudioPlayer? = null


    init {
        MessageRendererRegistry.registerRenderer(MessageType.TEXT, TextMessageRenderer())
        MessageRendererRegistry.registerRenderer(MessageType.FILE, FileMessageRenderer())
        MessageRendererRegistry.registerRenderer(MessageType.IMAGE, ImageMessageRenderer())
        MessageRendererRegistry.registerRenderer(MessageType.VIDEO, VideoMessageRenderer())
        MessageRendererRegistry.registerRenderer(MessageType.SOUND, SoundMessageRenderer())
        MessageRendererRegistry.registerRenderer(MessageType.FACE, FaceMessageRenderer())
        MessageRendererRegistry.registerRenderer(MessageType.SYSTEM, SystemMessageRenderer())
        MessageRendererRegistry.registerCustomMessageRenderer(
            "group_create",
            CreateGroupMessageRenderer()
        )
        viewModelScope.launch {
            messageListState.messageListChangeReason.collect {
                if (it == MessageListChangeReason.FETCH_MESSAGES) {
                    delay(100)
                    locateMessage = null
                }
            }
        }
        messageListStore.fetchMessageList(
            MessageFetchOption(
                message = locateMessage,
                direction = if (locateMessage != null) {
                    MessageFetchDirection.BOTH
                } else {
                    MessageFetchDirection.OLDER
                },
            ), object : CompletionHandler {
                override fun onSuccess() {
                    Log.i("MessageListViewModel", "fetchMessages success")
                }

                override fun onFailure(code: Int, desc: String) {
                    Log.i("MessageListViewModel", "fetchMessages failed: $code, $desc")
                }
            })
    }

    override fun onCleared() {
        destroyAudioPlayer()
    }

    fun clearMessageReadCount() {
        conversationListStore.clearConversationUnreadCount(
            conversationID = messageListStore.conversationID,
            object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
    }

    fun initializeAudioPlayer() {
        if (audioPlayer == null) {
            audioPlayer = AudioPlayer.create().apply {
                setListener(object : AudioPlayerListener {
                    override fun onPlay() {
                        audioPlayingState = audioPlayingState.copy(isPlaying = true)
                    }

                    override fun onPause() {
                        audioPlayingState = audioPlayingState.copy(isPlaying = false)
                    }

                    override fun onProgressUpdate(currentPosition: Int, duration: Int) {
                        audioPlayingState = audioPlayingState.copy(playPosition = currentPosition)
                    }

                    override fun onResume() {
                        audioPlayingState = audioPlayingState.copy(isPlaying = true)
                    }

                    override fun onCompletion() {
                        audioPlayingState = AudioPlayingState()
                    }
                })
            }
        }
    }

    fun playAudioMessage(message: MessageInfo) {
        val soundPath = message.messageBody?.soundPath
        if (soundPath != null && audioPlayer != null) {
            when {
                audioPlayingState.isPlaying && audioPlayingState.playingMessageId == message.msgID -> {
                    audioPlayer?.pause()
                }

                !audioPlayingState.isPlaying && audioPlayingState.playingMessageId == message.msgID -> {
                    audioPlayer?.resume()
                }

                else -> {
                    if (audioPlayingState.isPlaying) {
                        audioPlayer?.stop()
                    }
                    audioPlayingState = audioPlayingState.copy(playingMessageId = message.msgID)
                    audioPlayer?.play(soundPath)
                }
            }
        }
    }

    fun destroyAudioPlayer() {
        audioPlayer?.stop()
        audioPlayer = null
        audioPlayingState = AudioPlayingState()
    }

    fun isMessagePlaying(messageId: String): Boolean {
        return audioPlayingState.isPlaying && audioPlayingState.playingMessageId == messageId
    }

    @Composable
    fun getActions(messageInfo: MessageInfo): List<MessageUIAction> {
        val context = LocalContext.current
        val actions = mutableListOf<MessageUIAction>()
        if (messageInfo.messageType == MessageType.TEXT && AppBuilderConfig.messageActionList.contains(
                MessageAction.COPY
            )
        ) {
            actions.add(
                MessageUIAction(
                    name = context.getString(R.string.message_list_menu_copy),
                    icon = R.drawable.message_list_menu_copy_icon,
                    action = { messageInfo ->
                        val clipboard =
                            context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText(
                                "Copied Text",
                                messageInfo.messageBody?.text
                            )
                        )
                    })
            )
        }
        if (messageInfo.isSelf) {
            val currentTime = System.currentTimeMillis()
            val messageTime = messageInfo.timestamp ?: 0L
            val timeDifferenceSeconds = (currentTime - messageTime * 1000) / 1000

            if (timeDifferenceSeconds <= 120 && AppBuilderConfig.messageActionList.contains(
                    MessageAction.RECALL
                )
            ) {
                actions.add(
                    MessageUIAction(
                        name = context.getString(R.string.message_list_menu_recall),
                        icon = R.drawable.message_list_menu_recall_icon,
                        action = { messageInfo ->
                            messageActionStore.recallMessage(
                                messageInfo,
                                object :
                                    CompletionHandler {
                                    override fun onSuccess() {
                                    }

                                    override fun onFailure(code: Int, desc: String) {
                                    }
                                })
                        })
                )
            }
        }
        if (AppBuilderConfig.messageActionList.contains(MessageAction.DELETE)) {
            actions.add(
                MessageUIAction(
                    name = context.getString(R.string.message_list_menu_delete),
                    dangerousAction = true,
                    icon = R.drawable.message_list_menu_delete_icon,
                    action = { messageInfo ->
                        messageActionStore.deleteMessage(messageInfo, object : CompletionHandler {
                            override fun onSuccess() {
                            }

                            override fun onFailure(code: Int, desc: String) {
                            }
                        })
                    })
            )
        }

        return actions
    }


    fun loadMoreOlderMessage() {
        if (loadingState.isLoadingOlder || !hasMoreOlderMessage) {
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
        if (loadingState.isLoadingNewer || !hasMoreNewerMessage) {
            return
        }

        loadingState = loadingState.copy(isLoadingNewer = true)
        messageListStore.fetchMoreMessageList(MessageFetchDirection.NEWER, object : CompletionHandler {
            override fun onSuccess() {
                Log.e("MessageList", "loadMoreNewerMessage success")
                loadingState = loadingState.copy(isLoadingNewer = false)
            }

            override fun onFailure(code: Int, desc: String) {
                Log.e("MessageList", "loadMoreNewerMessage failure " + code + " " + desc)
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

    fun downloadImage(messageInfo: MessageInfo) {
        messageListStore.downloadMessageResource(
            messageInfo,
            MessageMediaFileType.THUMB_IMAGE,
            object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
    }


    fun downloadSound(messageInfo: MessageInfo) {
        messageListStore.downloadMessageResource(
            messageInfo,
            MessageMediaFileType.SOUND,
            object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
    }


    fun downloadVideoSnapShot(messageInfo: MessageInfo) {
        messageListStore.downloadMessageResource(
            messageInfo,
            MessageMediaFileType.VIDEO_SNAPSHOT,
            object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
    }


    fun checkNextMessageIsAggregation(index: Int): Boolean {
        val message = messageList.value.getOrNull(index)
        val nextMessage = messageList.value.getOrNull(index - 1)
        if (message?.sender == nextMessage?.sender) {
            val timeInterval = getIntervalSeconds(message?.timestamp, nextMessage?.timestamp)
            if (timeInterval > MessageListConfig.messageAggregationTime) {
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
            if (timeInterval > MessageListConfig.messageAggregationTime) {
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
            if (timeInterval > MessageListConfig.messageAggregationTime) {
                return getTimeString(message.timestamp?.times(1000))
            }
        }
        return null
    }

    private fun getIntervalSeconds(timeStamp1: Long?, timeStamp2: Long?): Long {
        if (timeStamp1 == null || timeStamp2 == null) return 0L
        if (timeStamp1 == 0L || timeStamp2 == 0L) return 0L
        return abs(timeStamp2 - timeStamp1)
    }

    fun retrySendMessage(context: Context?, message: MessageInfo) {
        messageInputStore.sendMessage(message, object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
                context?.let { Toast.error(context, context.getString(R.string.message_input_send_failed)) }
            }
        })
    }

    private fun getTimeString(timeStamp: Long?): String? {
        val date = timeStamp?.let { Date(it) }
        if (date == null) return null
        if (date.time == 0L) return null

        val calendar = Calendar.getInstance()
        val customCalendar = Calendar.getInstance()
        customCalendar.firstDayOfWeek = Calendar.SATURDAY

        val now = Date()
        customCalendar.time = now
        val nowYear = customCalendar.get(Calendar.YEAR)
        val nowMonth = customCalendar.get(Calendar.MONTH)
        val nowWeekOfMonth = customCalendar.get(Calendar.WEEK_OF_MONTH)
        val nowDay = customCalendar.get(Calendar.DAY_OF_MONTH)

        customCalendar.time = date
        val dateYear = customCalendar.get(Calendar.YEAR)
        val dateMonth = customCalendar.get(Calendar.MONTH)
        val dateWeekOfMonth = customCalendar.get(Calendar.WEEK_OF_MONTH)
        val dateDay = customCalendar.get(Calendar.DAY_OF_MONTH)

        val dateFmt: DateFormat

        if (nowYear == dateYear) {
            if (nowMonth == dateMonth) {
                if (nowWeekOfMonth == dateWeekOfMonth) {
                    if (nowDay == dateDay) {
                        dateFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    } else {
                        val locale = Locale.getDefault()
                        dateFmt = SimpleDateFormat("EEEE", locale)
                    }
                } else {
                    dateFmt = SimpleDateFormat("MM/dd", Locale.getDefault())
                }
            } else {
                dateFmt = SimpleDateFormat("MM/dd", Locale.getDefault())
            }
        } else {
            dateFmt = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        }

        return dateFmt.format(date)
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
                withContext(Dispatchers.Main) {
                }
            }
        }
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

}
