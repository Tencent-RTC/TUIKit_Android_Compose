package io.trtc.tuikit.atomicx.messageinput.viewmodels

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.albumpicker.AlbumPicker
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerModel
import io.trtc.tuikit.atomicx.albumpicker.PickMediaType
import io.trtc.tuikit.atomicx.albumpicker.interfaces.AlbumPickerListener
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Toast
import io.trtc.tuikit.atomicx.basecomponent.utils.appContext
import io.trtc.tuikit.atomicx.emojipicker.replaceEmojiKeysWithNames
import io.trtc.tuikit.atomicx.filepicker.FilePicker
import io.trtc.tuikit.atomicx.filepicker.FilePickerListener
import io.trtc.tuikit.atomicx.filepicker.util.FilePickerUtils
import io.trtc.tuikit.atomicx.messageinput.config.ChatMessageInputConfig
import io.trtc.tuikit.atomicx.messageinput.config.MessageInputConfigProtocol
import io.trtc.tuikit.atomicx.messageinput.data.MessageInputMenuAction
import io.trtc.tuikit.atomicx.messageinput.model.MentionInfo
import io.trtc.tuikit.atomicx.messageinput.utils.FileUtils
import io.trtc.tuikit.atomicx.messageinput.utils.ImageUtil
import io.trtc.tuikit.atomicx.messagelist.utils.isGroupConversation
import io.trtc.tuikit.atomicx.videorecorder.RecordMode
import io.trtc.tuikit.atomicx.videorecorder.VideoRecordListener
import io.trtc.tuikit.atomicx.videorecorder.VideoRecorder
import io.trtc.tuikit.atomicx.videorecorder.VideoRecorderConfig
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationListStore
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.atomicxcore.api.message.MessageBody
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageInputStore
import io.trtc.tuikit.atomicxcore.api.message.MessageType
import io.trtc.tuikit.atomicxcore.api.message.OfflinePushInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date
import kotlin.math.roundToInt
import kotlin.random.Random

const val FILE_MAX_SIZE = 100 * 1024 * 1024
const val VIDEO_MAX_SIZE = 100 * 1024 * 1024
const val IMAGE_MAX_SIZE = 28 * 1024 * 1024
const val GIF_IMAGE_MAX_SIZE = 10 * 1024 * 1024
const val AUDIO_MAX_RECORD_TIME = 60 * 1000
const val AUDIO_MIN_RECORD_TIME = 2 * 1000

class MessageInputViewModel(
    private val messageInputStore: MessageInputStore,
    private val messageInputConfig: MessageInputConfigProtocol = ChatMessageInputConfig()
) : ViewModel() {

    val conversationListStore = ConversationListStore.create()
    val conversationListState = conversationListStore.conversationListState
    val conversationID = messageInputStore.conversationID
    val conversationInfo =
        conversationListState.conversationList.map { list -> list.firstOrNull { it.conversationID == messageInputStore.conversationID } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    init {
        conversationListStore.fetchConversationInfo(messageInputStore.conversationID)
    }

    @Composable
    fun getActions(): List<MessageInputMenuAction> {
        val context = LocalActivity.current
        context ?: return emptyList()
        val pickAlbum = MessageInputMenuAction().apply {
            title = stringResource(R.string.message_input_album)
            iconResID = R.drawable.message_input_menu_album_icon
            onClick = {
                pickMediaAndSend(context)
            }
        }
        val takePhoto = MessageInputMenuAction().apply {
            title = stringResource(R.string.message_input_take_photo)
            iconResID = R.drawable.message_input_menu_camera_icon
            onClick = {
                captureImageAndSend(context)
            }
        }
        val recordVideo = MessageInputMenuAction().apply {
            title = stringResource(R.string.message_input_record_video)
            iconResID = R.drawable.message_input_menu_record_icon
            onClick = {
                recordVideoAndSend(context)
            }
        }
        val file = MessageInputMenuAction().apply {
            title = stringResource(R.string.message_input_file)
            iconResID = R.drawable.message_input_menu_file_icon
            onClick = {
                pickFileAndSend(context)
            }
        }

        return listOf(pickAlbum, takePhoto, recordVideo, file)
    }

    fun pickMediaAndSend(context: Context) {
        AlbumPicker.pickMedia(listener = object : AlbumPickerListener {
            override fun onFinishedSelect(count: Int) {
                Log.i("MessageInputViewModel", "on finished select. count:$count")
            }

            override fun onProgress(model: AlbumPickerModel, index: Int, progress: Double) {
                viewModelScope.launch(Dispatchers.IO) {
                    if (progress < 1.0) {
                        return@launch
                    }
                    val mediaPath = model.mediaPath ?: return@launch
                    val originalUri = Uri.parse(mediaPath)
                    val fileSize = FilePickerUtils.getFileSize(context, originalUri)

                    var isSizeTooLarge = false
                    if (model.mediaType == PickMediaType.VIDEO) {
                        isSizeTooLarge = fileSize > VIDEO_MAX_SIZE
                    } else if (model.mediaType == PickMediaType.GIF) {
                        isSizeTooLarge = fileSize > GIF_IMAGE_MAX_SIZE
                    } else {
                        isSizeTooLarge = fileSize > IMAGE_MAX_SIZE
                    }

                    if (isSizeTooLarge) {
                        Toast.error(
                            context,
                            context.resources.getString(com.tencent.qcloud.tuicore.R.string.TUIKitErrorFileTooLarge)
                        )
                        return@launch
                    }
                    val path = FileUtils.getPathFromUri(context, originalUri)
                    if (model.mediaType == PickMediaType.VIDEO) {
                        sendVideoMessage(context, path)
                    } else {
                        sendImageMessage(context, path)
                    }
                }
            }
        })
    }

    fun captureImageAndSend(context: Context) {
        VideoRecorder.startRecord(
            VideoRecorderConfig(
                recordMode = RecordMode.PHOTO_ONLY,
            ), object : VideoRecordListener {
                override fun onPhotoCaptured(filePath: String?) {
                    filePath?.let {
                        sendImageMessage(context, filePath)
                    }
                }
            })
    }

    fun recordVideoAndSend(context: Context) {
        VideoRecorder.startRecord(
            VideoRecorderConfig(
                recordMode = RecordMode.MIXED,
            ), object : VideoRecordListener {
                override fun onVideoCaptured(filePath: String?, durationMs: Int, thumbnailPath: String?) {
                    filePath?.let {
                        sendVideoMessage(context, filePath)
                    }
                }

                override fun onPhotoCaptured(filePath: String?) {
                    filePath?.let {
                        sendImageMessage(context, filePath)
                    }
                }
            })
    }

    fun sendTextMessage(context: Context?, text: String, mentionList: List<MentionInfo>) {
        val message = MessageInfo().apply {
            this.messageType = MessageType.TEXT
            if (mentionList.isNotEmpty()) {
                this.atUserList = mentionList.map { it.userID }
            }
        }
        message.messageBody = MessageBody().apply {
            this.text = text
        }
        message.needReadReceipt = messageInputConfig.enableReadReceipt
        message.offlinePushInfo = createOfflinePushInfo(context, message)
        messageInputStore.sendMessage(message, object : CompletionHandler {
            override fun onSuccess() {
            }

            override fun onFailure(code: Int, desc: String) {
                context?.let { Toast.error(it, context.getString(R.string.message_input_send_failed)) }
            }
        })
    }

    fun sendImageMessage(context: Context, filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val path: String = ImageUtil.getImagePathAfterRotate(context, filePath)
            val size = ImageUtil.getImageSize(path)
            val fileSize = FileUtils.getFileSize(filePath)
            val fileName: String = FileUtils.getFileName(path) ?: ""
            val fileExtension: String = FileUtils.getFileExtensionFromUrl(fileName)
            val mimeType =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
            if (TextUtils.equals(mimeType, "image/gif")) {
                if (fileSize > GIF_IMAGE_MAX_SIZE) {
                    Toast.error(
                        context,
                        context.resources.getString(com.tencent.qcloud.tuicore.R.string.TUIKitErrorFileTooLarge)
                    )
                    return@launch
                }
            } else {
                if (fileSize > IMAGE_MAX_SIZE) {
                    Toast.error(
                        context,
                        context.resources.getString(com.tencent.qcloud.tuicore.R.string.TUIKitErrorFileTooLarge)
                    )
                    return@launch
                }
            }
            val message = MessageInfo().apply {
                this.messageType = MessageType.IMAGE
                this.messageBody = MessageBody().apply {
                    this.originalImagePath = path
                    this.originalImageWidth = size[0]
                    this.originalImageHeight = size[1]
                }
                this.offlinePushInfo = createOfflinePushInfo(context, this)
            }
            message.needReadReceipt = messageInputConfig.enableReadReceipt
            messageInputStore.sendMessage(message, object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                    Toast.error(context, context.getString(R.string.message_input_send_failed))
                }
            })
        }
    }

    fun sendVideoMessage(context: Context, filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileSize = FileUtils.getFileSize(filePath)
            if (fileSize > VIDEO_MAX_SIZE) {
                Toast.error(
                    context,
                    context.resources.getString(com.tencent.qcloud.tuicore.R.string.TUIKitErrorFileTooLarge)
                )
                return@launch
            }
            val mmr = MediaMetadataRetriever()
            try {
                mmr.setDataSource(filePath)
                val sDuration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val bitmap = mmr.frameAtTime
                if (bitmap == null) {
                    Log.e("MessageInputViewModel", "build video message, get bitmap failed.")
                    return@launch
                }
                val sdkAppID = LoginStore.shared.sdkAppID
                val userID = LoginStore.shared.loginState.loginUserInfo.value?.userID
                val uuid = "${(Date().time / 1000)}_${Random.nextInt(1000)}"
                val basePath = context.filesDir?.absolutePath + "/atomicx_data/image/"
                val bitmapPath = "${basePath}_${sdkAppID}_${userID ?: ""}$uuid.jpg"
                val result: Boolean = FileUtils.saveBitmap(bitmapPath, bitmap)
                if (!result) {
                    Log.e("MessageInputViewModel", "build video message, save bitmap failed.")
                    return@launch
                }
                val imgWidth = bitmap!!.width
                val imgHeight = bitmap.height
                val duration = sDuration!!.toLong()

                val message = MessageInfo().apply {
                    this.messageType = MessageType.VIDEO
                    this.messageBody = MessageBody().apply {
                        this.videoSnapshotPath = bitmapPath
                        this.videoSnapshotWidth = imgWidth
                        this.videoSnapshotHeight = imgHeight
                        this.videoDuration = (duration / 1000f).roundToInt()
                        this.videoType = "mp4"
                        this.videoPath = filePath
                    }
                    this.offlinePushInfo = createOfflinePushInfo(context, this)
                }
                message.needReadReceipt = messageInputConfig.enableReadReceipt
                messageInputStore.sendMessage(message, object : CompletionHandler {
                    override fun onSuccess() {
                        Log.i("MessageInputViewModel", "send video message success.")
                    }

                    override fun onFailure(code: Int, desc: String) {
                        Log.e(
                            "MessageInputViewModel",
                            "send video message failed, code: $code, desc: $desc."
                        )
                        Toast.error(context, context.getString(R.string.message_input_send_failed))
                    }
                })
            } catch (ex: Exception) {
                Log.e(
                    "MessageInputViewModel",
                    "MediaMetadataRetriever exception $ex"
                )
            } finally {
                mmr.release()
            }
        }
    }

    fun pickFileAndSend(context: Context) {
        FilePicker.pickFiles(listener = object : FilePickerListener {
            override fun onPicked(result: List<Uri>) {
                viewModelScope.launch(Dispatchers.IO) {
                    result.forEach { uri ->
                        val fileSize = FilePickerUtils.getFileSize(context, uri)
                        if (fileSize > FILE_MAX_SIZE) {
                            Toast.error(
                                context,
                                context.resources.getString(com.tencent.qcloud.tuicore.R.string.TUIKitErrorFileTooLarge)
                            )
                            return@forEach
                        }
                        sendFileMessage(context, uri)
                        delay(100)
                    }
                }
            }

            override fun onCanceled() {
            }
        })

    }

    fun sendFileMessage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val filePath = FileUtils.getPathFromUri(context, uri)
            val fileName = FileUtils.getFileName(filePath)
            val fileSize = FileUtils.getFileSize(filePath)
            if (fileSize > FILE_MAX_SIZE) {
                Toast.error(
                    context,
                    context.resources.getString(com.tencent.qcloud.tuicore.R.string.TUIKitErrorFileTooLarge)
                )
                return@launch
            }
            val message = MessageInfo().apply {
                this.messageType = MessageType.FILE
                this.messageBody = MessageBody().apply {
                    this.filePath = filePath
                    this.fileName = fileName
                    this.fileSize = fileSize.toInt()
                }
                this.offlinePushInfo = createOfflinePushInfo(context, this)
            }
            message.needReadReceipt = messageInputConfig.enableReadReceipt
            messageInputStore.sendMessage(message, object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
        }
    }

    fun sendAudioMessage(filePath: String, duration: Int) {
        val message = MessageInfo().apply {
            this.messageType = MessageType.SOUND
            this.messageBody = MessageBody().apply {
                this.soundPath = filePath
                this.soundDuration = duration
            }
        }
        message.needReadReceipt = messageInputConfig.enableReadReceipt
        message.offlinePushInfo = createOfflinePushInfo(null, message)
        messageInputStore.sendMessage(message, object : CompletionHandler {
            override fun onSuccess() {
                Log.i("MessageInputViewModel", "send audio message success.")
            }

            override fun onFailure(code: Int, desc: String) {
                Log.e(
                    "MessageInputViewModel",
                    "send audio message failed, code: $code, desc: $desc."
                )
            }
        })
    }

    private fun createOfflinePushInfo(context: Context?, message: MessageInfo): OfflinePushInfo {
        val isGroup = isGroupConversation(conversationID)
        val groupId = if (isGroup) conversationID.removePrefix("group_") else ""

        val loginUserInfo = LoginStore.shared.loginState.loginUserInfo.value
        val selfUserId = loginUserInfo?.userID.orEmpty()
        val selfName = loginUserInfo?.nickname ?: selfUserId

        val chatName = conversationInfo.value?.title?.takeIf { it.isNotBlank() }

        val senderNickName = if (isGroup) {
            chatName ?: groupId
        } else {
            selfName
        }

        val description = createOfflinePushDescription(context, message)
        val ext = createOfflinePushExtJson(
            isGroup = isGroup,
            description = description,
            senderId = if (isGroup) groupId else selfUserId,
            senderNickName = senderNickName,
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
            title = senderNickName,
            description = description,
            extensionInfo = extensionInfo
        )
    }

    private fun createOfflinePushDescription(context: Context?, message: MessageInfo): String {
        val actualContext = context ?: appContext

        val content = when (message.messageType) {
            MessageType.TEXT -> replaceEmojiKeysWithNames(message.messageBody?.text.orEmpty())
            MessageType.IMAGE -> actualContext.getString(R.string.message_list_message_type_image)
            MessageType.VIDEO -> actualContext.getString(R.string.message_list_message_type_video)
            MessageType.FILE -> actualContext.getString(R.string.message_list_message_type_file)
            MessageType.SOUND -> actualContext.getString(R.string.message_list_message_type_voice)
            MessageType.FACE -> actualContext.getString(R.string.message_list_message_type_animate_emoji)
            MessageType.MERGED -> actualContext.getString(R.string.message_list_message_type_merged)
            else -> ""
        }

        return trimPushDescription(content)
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

    fun setDraft(draft: String?) {
        conversationListStore.setConversationDraft(messageInputStore.conversationID, draft)
    }

}
