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
import io.trtc.tuikit.atomicx.albumpicker.interfaces.AlbumPickerListener
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Toast
import io.trtc.tuikit.atomicx.basecomponent.theme.DefaultTheme
import io.trtc.tuikit.atomicx.filepicker.FilePicker
import io.trtc.tuikit.atomicx.filepicker.FilePickerListener
import io.trtc.tuikit.atomicx.filepicker.util.FilePickerUtils
import io.trtc.tuikit.atomicx.messageinput.data.MessageInputMenuAction
import io.trtc.tuikit.atomicx.messageinput.utils.FileUtils
import io.trtc.tuikit.atomicx.messageinput.utils.ImageUtil
import io.trtc.tuikit.atomicx.videorecorder.RecordListener
import io.trtc.tuikit.atomicx.videorecorder.RecordMode
import io.trtc.tuikit.atomicx.videorecorder.TakePhotoConfig
import io.trtc.tuikit.atomicx.videorecorder.TakeVideoConfig
import io.trtc.tuikit.atomicx.videorecorder.VideoRecorder
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.LoginStore
import io.trtc.tuikit.atomicxcore.api.MessageBody
import io.trtc.tuikit.atomicxcore.api.MessageInfo
import io.trtc.tuikit.atomicxcore.api.MessageInputStore
import io.trtc.tuikit.atomicxcore.api.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.random.Random

const val FILE_MAX_SIZE = 100 * 1024 * 1024
const val VIDEO_MAX_SIZE = 100 * 1024 * 1024
const val IMAGE_MAX_SIZE = 28 * 1024 * 1024
const val GIF_IMAGE_MAX_SIZE = 10 * 1024 * 1024
const val AUDIO_MAX_RECORD_TIME = 60

class MessageInputViewModel(private val messageInputStore: MessageInputStore) : ViewModel() {

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

            override fun onPicked(result: List<Pair<Uri, Boolean>>) {
                viewModelScope.launch(Dispatchers.IO) {
                    result.forEach { (originalUri, needTranscode) ->
                        val fileName: String = FileUtils.getFileName(context, originalUri) ?: ""
                        val fileExtension: String = FileUtils.getFileExtensionFromUrl(fileName)
                        val mimeType =
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
                        if (TextUtils.isEmpty(mimeType)) {
                            Log.e("MessageInputViewModel", "mimeType is empty.")
                            return@launch
                        }
                        var isVideo = false
                        if (mimeType!!.contains("video")) {
                            isVideo = true
                        } else if (mimeType.contains("image")) {
                            isVideo = false
                        } else {
                            Log.e("MessageInputViewModel", "mimeType is not image or video.")
                            return@launch
                        }
                        val fileSize = FilePickerUtils.getFileSize(context, originalUri)
                        if (isVideo) {
                            if (fileSize >= 0 && fileSize > VIDEO_MAX_SIZE) {
                                Toast.error(
                                    context,
                                    context.resources.getString(com.tencent.qcloud.tuicore.R.string.TUIKitErrorFileTooLarge)
                                )
                                return@forEach
                            }
                        } else {
                            if (TextUtils.equals(mimeType, "image/gif")) {
                                if (fileSize >= 0 && fileSize > GIF_IMAGE_MAX_SIZE) {
                                    Toast.error(
                                        context,
                                        context.resources.getString(com.tencent.qcloud.tuicore.R.string.TUIKitErrorFileTooLarge)
                                    )
                                    return@forEach
                                }
                            } else {
                                if (fileSize >= 0 && fileSize > IMAGE_MAX_SIZE) {
                                    Toast.error(
                                        context,
                                        context.resources.getString(com.tencent.qcloud.tuicore.R.string.TUIKitErrorFileTooLarge)
                                    )
                                    return@forEach
                                }
                            }
                        }
                        val path = FileUtils.getPathFromUri(context, originalUri)
                        if (isVideo) {
                            sendVideoMessage(context, path)
                        } else {
                            sendImageMessage(context, path)
                        }
                    }
                }
            }

            override fun onCanceled() {
            }
        })
    }

    fun captureImageAndSend(context: Context) {
        val takePhotoConfig = TakePhotoConfig(
            primaryColor = DefaultTheme.currentPrimaryColor
        )
        VideoRecorder.takePhoto(takePhotoConfig, object : RecordListener {
            override fun onPhotoCaptured(filePath: String) {
                sendImageMessage(context, filePath)
            }
        })
    }

    fun recordVideoAndSend(context: Context) {
        val takeVideoConfig =
            TakeVideoConfig(
                recordMode = RecordMode.MIXED,
                primaryColor = DefaultTheme.currentPrimaryColor
            )
        VideoRecorder
            .takeVideo(takeVideoConfig, object : RecordListener {
                override fun onVideoCaptured(filePath: String, duration: Int) {
                    sendVideoMessage(context, filePath)
                }

                override fun onPhotoCaptured(filePath: String) {
                    sendImageMessage(context, filePath)
                }
            })
    }

    fun sendTextMessage(context: Context?, text: String) {
        val message = MessageInfo().apply { this.messageType = MessageType.TEXT }
        message.messageBody = MessageBody().apply { this.text = text }
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
            }
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
                        this.videoDuration = duration.toInt()
                        this.videoType = "mp4"
                        this.videoPath = filePath
                    }
                }
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
            }
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
}
