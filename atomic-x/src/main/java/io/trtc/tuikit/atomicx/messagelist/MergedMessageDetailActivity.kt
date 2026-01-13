package io.trtc.tuikit.atomicx.messagelist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.config.MessageAlignment
import io.trtc.tuikit.atomicx.basecomponent.theme.Colors
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.basecomponent.utils.SetActivitySystemBarAppearance
import io.trtc.tuikit.atomicx.messagelist.ui.InteractionHandler
import io.trtc.tuikit.atomicx.messagelist.ui.LocalAudioPlayingState
import io.trtc.tuikit.atomicx.messagelist.ui.LocalInteractionHandler
import io.trtc.tuikit.atomicx.messagelist.ui.LocalMessageListConfig
import io.trtc.tuikit.atomicx.messagelist.ui.LocalMessageListViewModel
import io.trtc.tuikit.atomicx.messagelist.ui.LocalMessageRenderConfig
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRendererRegistry
import io.trtc.tuikit.atomicx.messagelist.ui.highlightBackground
import io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers.DefaultMessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageReactionBar
import io.trtc.tuikit.atomicx.messagelist.utils.FileUtils
import io.trtc.tuikit.atomicx.messagelist.utils.senderDisplayName
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MergedMessageDetailViewModel
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MergedMessageDetailViewModelFactory
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MessageListViewModel
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MessageListViewModelFactory
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageListStore
import io.trtc.tuikit.atomicxcore.api.message.MessageListType
import io.trtc.tuikit.atomicxcore.api.message.MessageType

val LocalMergedMessageDetailViewModel =
    compositionLocalOf<MergedMessageDetailViewModel> { error("No ViewModel provided") }

class MergedMessageDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_MSG = "extra_msg"

        fun start(context: Context, mergedMessage: MessageInfo) {
            val intent = Intent(context, MergedMessageDetailActivity::class.java).apply {
                putExtra(EXTRA_MSG, mergedMessage)
            }
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val message = intent.getParcelableExtra<MessageInfo>(EXTRA_MSG)

        if (message !is MessageInfo) {
            finish()
            return
        }
        val messageListStore = MessageListStore.create("", MessageListType.MERGED)
        val viewModel by viewModels<MergedMessageDetailViewModel> {
            MergedMessageDetailViewModelFactory(messageListStore, message)
        }
        val messageListViewModel by viewModels<MessageListViewModel> {
            MessageListViewModelFactory(messageListStore, null)
        }

        setContent {
            DisposableEffect(Unit) {
                messageListViewModel.initializeAudioPlayer()
                onDispose {
                    messageListViewModel.destroyAudioPlayer()
                }
            }

            SetActivitySystemBarAppearance()

            CompositionLocalProvider(
                LocalMergedMessageDetailViewModel provides viewModel,
                LocalMessageListViewModel provides messageListViewModel,
                LocalAudioPlayingState provides messageListViewModel.audioPlayingState
            ) {
                MergedMessageDetailScreen(
                    message = message,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun MergedMessageDetailScreen(
    message: MessageInfo,
    onBack: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
            .systemBarsPadding(),
        topBar = {
            MergedMessageTopBar(message, onBack = onBack)
        }
    ) { paddingValues ->
        MergedMessageList(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

@Composable
fun MergedMessageTopBar(
    message: MessageInfo,
    onBack: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = colors.bgColorOperate)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()

                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.contact_list_back),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() },
                tint = colors.textColorLink
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = message.messageBody?.mergedMessage?.title ?: stringResource(R.string.message_list_merge_message),
                fontSize = 17.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1,
                color = colors.textColorPrimary
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(24.dp))
        }
        HorizontalDivider(thickness = 1.dp, color = colors.strokeColorSecondary)
    }
}

@Composable
fun MergedMessageList(
    modifier: Modifier = Modifier,
) {
    val colors = LocalTheme.current.colors
    val viewModel = LocalMergedMessageDetailViewModel.current
    val messageList by viewModel.messageList.collectAsState()


    Column(
        modifier = modifier
            .background(color = colors.bgColorOperate)
            .padding(vertical = 12.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = false,
            verticalArrangement = Arrangement.Top
        ) {

            itemsIndexed(
                items = messageList,
                key = { _, item -> item.msgID!! }
            ) { index, message ->
                MessageItem(
                    modifier = Modifier.animateItem(),
                    message = message,
                )
            }
        }
    }
}

@Composable
fun MessageItem(
    message: MessageInfo,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTheme.current.colors
    val config = LocalMessageListConfig.current

    val isSelf = message.isSelf
    val isShowSelfAvatar = true
    val isShowSelfNickname = config.isShowRightNickname
    val isShowOtherAvatar = true
    val isShowOtherNickname = config.isShowLeftNickname
    val isShowSystemMessage = false
    val isShowUnsupportMessage = config.isShowUnsupportMessage
    val avatarSpacing = config.avatarSpacing
    val isShowUserInfo = when {
        isSelf -> isShowSelfAvatar || isShowSelfNickname
        else -> isShowOtherAvatar || isShowOtherNickname
    }
    val isShowAvatar = true
    val isShowNickname = when {
        isSelf -> isShowSelfNickname
        else -> isShowOtherNickname
    }

    val renderer = MessageRendererRegistry.getRenderer(message)
    val renderConfig = renderer.renderConfig
    CompositionLocalProvider(LocalMessageRenderConfig provides renderConfig) {

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

            val arrangement = Arrangement.Start
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom,
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = arrangement,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val messageContent = @Composable {
                            Column {
                                MessageContent(message = message, false, enableGesture = false)
                                if (config.isSupportReaction && message.reactionList.isNotEmpty()) {
                                    MessageReactionBar(
                                        modifier = Modifier.padding(top = 4.dp),
                                        reactionList = message.reactionList,
                                        onClick = {}
                                    )
                                }
                            }
                        }
                        val userInfo = @Composable {
                            UserInfoColumn(
                                message = message,
                                isShowAvatar = isShowAvatar,
                                isShowNickname = isShowNickname,
                                nextMessageIsAggregation = false,
                                isStart = when (config.alignment) {
                                    MessageAlignment.LEFT -> true
                                    MessageAlignment.RIGHT -> false
                                    else -> !message.isSelf
                                }
                            ) {
                            }
                        }
                        val spacer = @Composable { Spacer(Modifier.width(avatarSpacing)) }

                        val components =
                            (if (isShowUserInfo) listOf(userInfo, spacer) else emptyList()) +
                                    listOf(messageContent)

                        components.forEach { it() }
                    }
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
    onUserClick: () -> Unit,
) {
    val colors = LocalTheme.current.colors
    Column(
        modifier = Modifier.clickable(indication = null, interactionSource = null) {
            onUserClick()
        },
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = if (isStart) Alignment.Start else Alignment.End
    ) {
        if (isShowNickname && !nextMessageIsAggregation) {
            Text(
                text = message.senderDisplayName,
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = colors.textColorSecondary
            )
        }
        if (isShowAvatar) {
            Avatar(message, nextMessageIsAggregation) {
                onUserClick()
            }
        }
    }
}

@Composable
fun Avatar(
    message: MessageInfo, isAggregation: Boolean = false, onClick: () -> Unit
) {
    val faceUrl = message.rawMessage?.faceUrl
    val title = message.senderDisplayName.firstOrNull()?.uppercase() ?: ""
    Box(modifier = Modifier) {
        if (isAggregation) {
            Spacer(Modifier.size(32.dp))
        } else {
            Avatar(url = faceUrl, name = title, size = AvatarSize.S) {
                onClick()
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MessageContent(
    message: MessageInfo, isAggregation: Boolean = false, enableGesture: Boolean = true
) {
    val messageListViewModel = LocalMessageListViewModel.current
    val viewModel = LocalMergedMessageDetailViewModel.current
    val config = LocalMessageListConfig.current
    val colors = LocalTheme.current.colors
    val context = LocalContext.current
    val renderConfig = LocalMessageRenderConfig.current
    val normalRadius = 16.dp
    val smallRadius = 1.dp
    val bottomStartRadius = if (isAggregation) {
        normalRadius
    } else {
        smallRadius
    }
    val bottomEndRadius = normalRadius

    val shape = RoundedCornerShape(normalRadius, normalRadius, bottomEndRadius, bottomStartRadius)
    BoxWithConstraints(
        modifier = Modifier
            .clip(shape)
            .highlightBackground(
                highlightKey = message.msgID ?: "",
                color = if (renderConfig.useDefaultBubble) {
                    (if (message.isSelf) colors.bgColorBubbleOwn else colors.bgColorBubbleReciprocal)
                } else {
                    Colors.Transparent
                },
                shape = if (renderConfig.useDefaultBubble) shape else RectangleShape
            )
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth * 0.9f)
        ) {
            CompositionLocalProvider(
                LocalInteractionHandler provides InteractionHandler(
                    onTap = {
                        if (message.messageType == MessageType.MERGED) {
                            MergedMessageDetailActivity.start(context, message)
                        } else if (message.messageType == MessageType.FILE) {
                            val filePath = message.messageBody?.filePath
                            val fileName = message.messageBody?.fileName
                            if (filePath.isNullOrEmpty()) {
                                messageListViewModel.downloadFile(message)
                            } else {
                                FileUtils.openFile(context, filePath, fileName)
                            }
                        } else if (message.messageType == MessageType.VIDEO) {
                            messageListViewModel.downloadOrShowVideo(context, message)
                        } else if (message.messageType == MessageType.SOUND) {
                            messageListViewModel.playAudioMessage(message)
                        } else if (message.messageType == MessageType.IMAGE) {
                            messageListViewModel.showImage(context, message)
                        }
                    },
                    onRendered = {
                        if (message.messageType == MessageType.VIDEO) {
                            viewModel.downloadVideoSnapShot(message)
                        } else if (message.messageType == MessageType.IMAGE) {
                            viewModel.downloadThumbImage(message)
                        } else if (message.messageType == MessageType.SOUND) {
                            viewModel.downloadSound(message)
                        }
                    },
                )
            ) {
                val renderer = MessageRendererRegistry.getRenderer(message)
                renderer.Render(message)
            }
        }

    }
}
