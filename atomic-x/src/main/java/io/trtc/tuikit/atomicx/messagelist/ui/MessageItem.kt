package io.trtc.tuikit.atomicx.messagelist.ui

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AlertDialog
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.config.MessageAlignment
import io.trtc.tuikit.atomicx.basecomponent.theme.Colors
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messagelist.MergedMessageDetailActivity
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.DefaultMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.AsrTextBubble
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageCheckBox
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageReactionBar
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.TranslationBubble
import io.trtc.tuikit.atomicx.messagelist.utils.FileUtils
import io.trtc.tuikit.atomicx.messagelist.utils.senderDisplayName
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MessageListViewModel
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageStatus
import io.trtc.tuikit.atomicxcore.api.message.MessageType


@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    index: Int,
    message: MessageInfo,
    onUserLongPress: (String) -> Unit = {},
    onUserClick: (String?) -> Unit
) {
    val colors = LocalTheme.current.colors
    val config = LocalMessageListConfig.current
    val context = LocalContext.current
    val messageViewModel = LocalMessageListViewModel.current
    val isMultiSelectMode by messageViewModel.isMultiSelectMode.collectAsState()
    val selectedMessages by messageViewModel.selectedMessages.collectAsState()
    val prevMessageIsAggregation = messageViewModel.checkPreviousMessageIsAggregation(index)
    val nextMessageIsAggregation = messageViewModel.checkNextMessageIsAggregation(index)
    val timeString = messageViewModel.getMessageTimeString(index)

    val isSelf = message.isSelf
    val isShowSelfAvatar = config.isShowRightAvatar
    val isShowSelfNickname = config.isShowRightNickname
    val isShowOtherAvatar = config.isShowLeftAvatar
    val isShowOtherNickname = config.isShowLeftNickname
    val isShowTimeMessage = config.isShowTimeMessage
    val isShowTimeInBubble = config.isShowTimeInBubble
    val isShowSystemMessage = config.isShowSystemMessage
    val isShowUnsupportMessage = config.isShowUnsupportMessage
    val cellSpacing = config.cellSpacing
    val avatarSpacing = config.avatarSpacing
    val isShowUserInfo = when {
        isSelf -> isShowSelfAvatar || isShowSelfNickname
        else -> isShowOtherAvatar || isShowOtherNickname
    }
    val isShowAvatar = when {
        isSelf -> isShowSelfAvatar
        else -> isShowOtherAvatar
    }
    val isShowNickname = when {
        isSelf -> isShowSelfNickname
        else -> isShowOtherNickname
    }

    val topPadding = if (prevMessageIsAggregation) 2.dp else cellSpacing
    val bottomPadding = if (nextMessageIsAggregation) 2.dp else cellSpacing
    val renderer = MessageRendererRegistry.getRenderer(message)
    val renderConfig = renderer.renderConfig
    CompositionLocalProvider(
        LocalMessageRenderConfig provides renderConfig,
        LocalAudioPlayingState provides messageViewModel.audioPlayingState
    ) {

        if (!isShowSystemMessage) {
            if (message.messageType == MessageType.SYSTEM) {
                Box {}
                return@CompositionLocalProvider
            }
        }
        if (!isShowUnsupportMessage) {
            if (renderer is DefaultMessageRenderer) {
                Box {}
                return@CompositionLocalProvider
            }
        }
        if (renderConfig.showMessageMeta) {

            val arrangement = when (config.alignment) {
                MessageAlignment.LEFT -> Arrangement.Start
                MessageAlignment.RIGHT -> Arrangement.End
                else -> if (message.isSelf) Arrangement.End else Arrangement.Start
            }

            Box(modifier = modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = topPadding, bottom = bottomPadding)
                ) {
                    if (!timeString.isNullOrEmpty() && isShowTimeMessage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp - topPadding, bottom = 20.dp - bottomPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = timeString,
                                fontSize = 14.sp,
                                color = colors.textColorSecondary,
                                fontWeight = FontWeight.W400
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        if (isMultiSelectMode) {
                            MessageCheckBox(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                checked = selectedMessages.contains(message)
                            )
                            Spacer(Modifier.width(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = arrangement,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val messageContent = @Composable {
                                val horizontalAlignment = when (config.alignment) {
                                    MessageAlignment.RIGHT -> Alignment.End
                                    MessageAlignment.LEFT -> Alignment.Start
                                    else -> if (isSelf) Alignment.End else Alignment.Start
                                }
                                val isStatusBeforeBubble = when (config.alignment) {
                                    MessageAlignment.RIGHT -> true
                                    MessageAlignment.LEFT -> false
                                    else -> isSelf
                                }
                                Column(horizontalAlignment = horizontalAlignment) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        if (isStatusBeforeBubble) {
                                            MessageStatusContent(message)
                                        }
                                        MessageContent(
                                            message,
                                            nextMessageIsAggregation,
                                            onLongPress = {
                                                messageViewModel.showLongPressActionDialog(message)
                                            },
                                            onTap = {
                                                if (message.messageType == MessageType.MERGED) {
                                                    MergedMessageDetailActivity.start(context, message)
                                                } else if (message.messageType == MessageType.FILE) {
                                                    val filePath = message.messageBody?.filePath
                                                    val fileName = message.messageBody?.fileName
                                                    if (filePath.isNullOrEmpty()) {
                                                        messageViewModel.downloadFile(message)
                                                    } else {
                                                        FileUtils.openFile(context, filePath, fileName)
                                                    }
                                                } else if (message.messageType == MessageType.VIDEO) {
                                                    messageViewModel.downloadOrShowVideo(context, message)
                                                } else if (message.messageType == MessageType.SOUND) {
                                                    messageViewModel.playAudioMessage(message)
                                                } else if (message.messageType == MessageType.IMAGE) {
                                                    messageViewModel.showImage(context, message)
                                                }
                                            },
                                            onRendered = {
                                                if (message.messageType == MessageType.VIDEO) {
                                                    messageViewModel.downloadVideoSnapShot(message)
                                                } else if (message.messageType == MessageType.IMAGE) {
                                                    messageViewModel.downloadThumbImage(message)
                                                } else if (message.messageType == MessageType.SOUND) {
                                                    messageViewModel.downloadSound(message)
                                                }
                                            })

                                        if (!isStatusBeforeBubble) {
                                            MessageStatusContent(message)
                                        }
                                    }

                                    // ASR Text Bubble for Sound messages
                                    if (message.messageType == MessageType.SOUND) {
                                        val processingAuxiliaryTextMessageIds by messageViewModel.processingAuxiliaryTextMessageIds.collectAsState()
                                        val hiddenAuxiliaryTextMessageIds by messageViewModel.hiddenAuxiliaryTextMessageIds.collectAsState()
                                        val msgID = message.msgID ?: ""
                                        val isConverting = processingAuxiliaryTextMessageIds.contains(msgID)
                                        val isAsrTextHidden = hiddenAuxiliaryTextMessageIds.contains(msgID)
                                        val asrText = message.messageBody?.asrText
                                        val shouldShowAsrBubble =
                                            isConverting || (!asrText.isNullOrEmpty() && !isAsrTextHidden)

                                        if (shouldShowAsrBubble) {
                                            var showAsrMenu by remember { mutableStateOf(false) }
                                            AsrTextBubble(
                                                isSelf = message.isSelf,
                                                isLoading = isConverting,
                                                asrText = asrText,
                                                showMenu = showAsrMenu,
                                                onLongPress = { showAsrMenu = true },
                                                onDismissMenu = { showAsrMenu = false },
                                                onHide = { messageViewModel.hideAsrText(message) },
                                                onForward = { messageViewModel.forwardAsrText(message) },
                                                onCopy = { messageViewModel.copyAsrText(message, context) }
                                            )
                                        }
                                    }

                                    // Translation Bubble for Text messages
                                    if (message.messageType == MessageType.TEXT) {
                                        val processingAuxiliaryTextMessageIds by messageViewModel.processingAuxiliaryTextMessageIds.collectAsState()
                                        val hiddenAuxiliaryTextMessageIds by messageViewModel.hiddenAuxiliaryTextMessageIds.collectAsState()
                                        val msgID = message.msgID ?: ""
                                        val isTranslating = processingAuxiliaryTextMessageIds.contains(msgID)
                                        val isTranslationHidden = hiddenAuxiliaryTextMessageIds.contains(msgID)
                                        val translatedTextMap = message.messageBody?.translatedText
                                        val shouldShowTranslationBubble =
                                            isTranslating || (!translatedTextMap.isNullOrEmpty() && !isTranslationHidden)

                                        if (shouldShowTranslationBubble) {
                                            var showTranslationMenu by remember { mutableStateOf(false) }
                                            val translatedText = messageViewModel.getTranslatedDisplayText(message)
                                            TranslationBubble(
                                                isSelf = message.isSelf,
                                                isLoading = isTranslating,
                                                translatedText = translatedText,
                                                showMenu = showTranslationMenu,
                                                onLongPress = { showTranslationMenu = true },
                                                onDismissMenu = { showTranslationMenu = false },
                                                onHide = { messageViewModel.hideTranslation(message) },
                                                onForward = { messageViewModel.forwardTranslatedText(message) },
                                                onCopy = { messageViewModel.copyTranslatedText(message, context) }
                                            )
                                        }
                                    }

                                    if (config.isSupportReaction && message.reactionList.isNotEmpty() && message.status == MessageStatus.SEND_SUCCESS) {
                                        MessageReactionBar(
                                            modifier = Modifier.padding(top = 4.dp),
                                            reactionList = message.reactionList,
                                            onClick = {
                                                messageViewModel.showReactionDetail(message)
                                            }
                                        )
                                    }
                                    if (message.status == MessageStatus.VIOLATION) {
                                        Text(
                                            stringResource(R.string.message_list_violation_received),
                                            fontSize = 12.sp,
                                            color = colors.textColorError
                                        )
                                    }
                                }
                            }
                            val userInfo = @Composable {
                                UserInfoColumn(
                                    message,
                                    isShowAvatar = isShowAvatar,
                                    isShowNickname = isShowNickname,
                                    nextMessageIsAggregation = nextMessageIsAggregation,
                                    isStart = when (config.alignment) {
                                        MessageAlignment.LEFT -> true
                                        MessageAlignment.RIGHT -> false
                                        else -> !message.isSelf
                                    },
                                    onUserLongPress = { onUserLongPress(it) },
                                    onUserClick = { onUserClick(message.sender.userID) }
                                )
                            }
                            val spacer = @Composable { Spacer(Modifier.width(avatarSpacing)) }

                            val components = when (config.alignment) {
                                MessageAlignment.TWO_SIDED -> {
                                    if (isSelf) {
                                        listOf(messageContent) +
                                                (if (isShowUserInfo) listOf(spacer, userInfo) else emptyList())
                                    } else {
                                        (if (isShowUserInfo) listOf(userInfo, spacer) else emptyList()) +
                                                listOf(messageContent)
                                    }
                                }

                                MessageAlignment.RIGHT -> {
                                    listOf(messageContent) +
                                            (if (isShowUserInfo) listOf(spacer, userInfo) else emptyList())
                                }

                                MessageAlignment.LEFT -> {
                                    (if (isShowUserInfo) listOf(userInfo, spacer) else emptyList()) +
                                            listOf(messageContent)
                                }
                            }

                            components.forEach { it() }
                        }
                    }
                }

                // MultiSelect Intercept Overlay
                if (isMultiSelectMode) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(indication = null, interactionSource = null) {
                                messageViewModel.toggleMessageSelection(message)
                            }
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                renderer.Render(message)
            }
        }
    }
}

@Composable
private fun UserInfoColumn(
    message: MessageInfo,
    isShowAvatar: Boolean,
    isShowNickname: Boolean,
    nextMessageIsAggregation: Boolean,
    isStart: Boolean,
    onUserLongPress: (String) -> Unit = {},
    onUserClick: () -> Unit,
) {
    val colors = LocalTheme.current.colors
    val userID = message.sender.userID ?: ""
    val nicknameInteractionSource = remember { MutableInteractionSource() }
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = if (isStart) Alignment.Start else Alignment.End
    ) {
        if (isShowNickname && !nextMessageIsAggregation) {
            Text(
                modifier = Modifier.combinedClickable(
                    interactionSource = nicknameInteractionSource,
                    indication = null,
                    onClick = { onUserClick() },
                    onLongClick = { onUserLongPress(userID) }
                ),
                text = message.senderDisplayName,
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = colors.textColorSecondary
            )
        }
        if (isShowAvatar) {
            MessageAvatar(message, nextMessageIsAggregation, onUserLongPress) {
                onUserClick()
            }
        }
    }
}

@Composable
fun MessageAvatar(
    message: MessageInfo,
    isAggregation: Boolean = false,
    onLongPress: (String) -> Unit = {},
    onClick: () -> Unit
) {
    val faceUrl = message.rawMessage?.faceUrl
    val title = message.senderDisplayName.firstOrNull()?.uppercase() ?: ""
    val userID = message.sender.userID ?: ""
    Box(modifier = Modifier) {
        if (isAggregation) {
            Spacer(Modifier.size(32.dp))
        } else {
            Box {
                Avatar(url = faceUrl, name = title, size = AvatarSize.S)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(userID) {
                            detectTapGestures(
                                onTap = { onClick() },
                                onLongPress = { onLongPress(userID) }
                            )
                        }
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MessageContent(
    message: MessageInfo,
    isAggregation: Boolean = false,
    enableGesture: Boolean = true,
    onLongPress: () -> Unit = {},
    onTap: () -> Unit = {},
    onRendered: () -> Unit = {}
) {
    val config = LocalMessageListConfig.current
    val colors = LocalTheme.current.colors
    val context = LocalContext.current
    val renderConfig = LocalMessageRenderConfig.current
    val normalRadius = 16.dp
    val smallRadius = 1.dp
    val bottomStartRadius = if (isAggregation) {
        normalRadius
    } else {
        when (config.alignment) {
            MessageAlignment.LEFT -> smallRadius
            MessageAlignment.RIGHT -> normalRadius
            else -> if (message.isSelf) normalRadius else smallRadius
        }
    }

    val bottomEndRadius = if (isAggregation) {
        normalRadius
    } else {
        when (config.alignment) {
            MessageAlignment.LEFT -> normalRadius
            MessageAlignment.RIGHT -> smallRadius
            else -> if (message.isSelf) smallRadius else normalRadius
        }
    }

    val shape = RoundedCornerShape(normalRadius, normalRadius, bottomEndRadius, bottomStartRadius)
    BoxWithConstraints(
        modifier = Modifier
            .pointerInput(message) {
                if (enableGesture) {
                    detectTapGestures(
                        onLongPress = {
                            onLongPress()
                        },
                    )
                }
            }
            .clip(shape)
            .highlightBackground(
                highlightKey = message.msgID ?: "",
                color = if (renderConfig.useDefaultBubble) {
                    (if (message.isSelf) colors.bgColorBubbleOwn else colors.bgColorBubbleReciprocal)
                } else {
                    Colors.Transparent
                },
                shape = if (renderConfig.useDefaultBubble) shape else RectangleShape
            )) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth * 0.9f)
        ) {
            CompositionLocalProvider(
                LocalInteractionHandler provides InteractionHandler(
                    onTap = onTap,
                    onLongPress = onLongPress,
                    onRendered = onRendered
                )
            ) {
                val renderer = MessageRendererRegistry.getRenderer(message)
                renderer.Render(message)
            }
        }
    }

}


@Composable
fun MessageStatusContent(message: MessageInfo, modifier: Modifier = Modifier) {
    var viewModel: MessageListViewModel = viewModel()
    val colors = LocalTheme.current.colors
    val activity = LocalActivity.current
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (message.status == MessageStatus.SEND_FAIL || message.status == MessageStatus.VIOLATION) {
            var showResendTips by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .clickable {
                        if (message.status == MessageStatus.SEND_FAIL) {
                            showResendTips = true
                        }
                    }) {
                Text(
                    modifier = Modifier
                        .size(14.dp)
                        .background(color = colors.textColorError, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 5.dp)
                        .clip(CircleShape)
                        .wrapContentSize(),
                    text = "!",
                    color = colors.textColorButton,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600
                )
            }
            AlertDialog(
                isVisible = showResendTips,
                message = stringResource(R.string.messaeg_list_resend_tips),
                onDismiss = { showResendTips = false },
                onCancel = { showResendTips = false },
                onConfirm = { viewModel.retrySendMessage(activity, message) })
        } else if (message.status == MessageStatus.SENDING) {
            Box(
                modifier = Modifier
                    .padding(6.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = colors.textColorAntiSecondary,
                    strokeWidth = 1.5.dp
                )
            }
        }
    }
}
