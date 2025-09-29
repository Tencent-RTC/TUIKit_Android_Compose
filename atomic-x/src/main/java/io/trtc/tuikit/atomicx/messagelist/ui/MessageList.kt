package io.trtc.tuikit.atomicx.messagelist.ui

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AlertDialog
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.config.AppBuilderConfig
import io.trtc.tuikit.atomicx.basecomponent.config.MessageAlignment
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messagelist.viewmodels.HighlightManager
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MessageListViewModel
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MessageListViewModelFactory
import io.trtc.tuikit.atomicxcore.api.MessageActionStore
import io.trtc.tuikit.atomicxcore.api.MessageInfo
import io.trtc.tuikit.atomicxcore.api.MessageListChangeReason
import io.trtc.tuikit.atomicxcore.api.MessageListStore
import io.trtc.tuikit.atomicxcore.api.MessageListType
import io.trtc.tuikit.atomicxcore.api.MessageStatus
import kotlin.math.ceil

val LocalMessageListViewModel =
    compositionLocalOf<MessageListViewModel> { error("No ViewModel provided") }
val LocalMessage = compositionLocalOf<MessageInfo> { error("No Message provided") }

@Composable
fun MessageList(
    conversationID: String,
    modifier: Modifier = Modifier,
    locateMessage: MessageInfo? = null,
    messageListViewModelFactory: MessageListViewModelFactory = MessageListViewModelFactory(
        MessageListStore.create(conversationID, MessageListType.HISTORY),
        MessageActionStore.create(), locateMessage
    ),
    onUserClick: (String) -> Unit = {},
) {
    val messageListViewModel =
        viewModel(MessageListViewModel::class, factory = messageListViewModelFactory)

    DisposableEffect(Unit) {
        messageListViewModel.initializeAudioPlayer()
        messageListViewModel.clearMessageReadCount()
        onDispose {
            messageListViewModel.clearMessageReadCount()
            messageListViewModel.destroyAudioPlayer()
        }
    }

    CompositionLocalProvider(
        LocalMessageListViewModel provides messageListViewModel,
    ) {
        MessageList(modifier, messageListViewModel, onUserClick)
    }
}


@Composable
private fun MessageList(
    modifier: Modifier,
    messageListViewModel: MessageListViewModel,
    onUserClick: (String) -> Unit = {},
) {
    val colors = LocalTheme.current.colors
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val messageList by messageListViewModel.messageList.collectAsState()
    val isListInitialized = messageList.isNotEmpty()
    val listState = remember(isListInitialized) {
        if (isListInitialized) {
            if (messageListViewModel.locateMessage != null) {
                messageListViewModel.locateMessage?.msgID?.let {
                    HighlightManager.addHighlight(
                        key = messageListViewModel.locateMessage!!.msgID!!
                    )
                }
                val index =
                    messageList.indexOfFirst { it.msgID == messageListViewModel.locateMessage?.msgID }
                LazyListState(firstVisibleItemIndex = index.coerceIn(0, Int.MAX_VALUE))
            } else {
                LazyListState()
            }
        } else {
            LazyListState()
        }
    }

    var canLoadMore by remember { mutableStateOf(true) }

    suspend fun animateScrollToEnd() {
        canLoadMore = false
        listState.animateScrollToItem(0)
    }

    suspend fun scrollToEnd() {
        canLoadMore = false
        listState.scrollToItem(0)
    }

    LaunchedEffect(messageListViewModel.messageListChangeSource) {
        when (messageListViewModel.messageListChangeSource) {
            MessageListChangeReason.RECV_MESSAGE -> {
                val shouldScrollToEnd = with(listState) {
                    val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                    firstVisible <= 2
                }

                if (shouldScrollToEnd) {
                    animateScrollToEnd()
                }
            }

            MessageListChangeReason.FETCH_MESSAGES -> {
            }

            MessageListChangeReason.FETCH_MORE_MESSAGES -> {}
            MessageListChangeReason.SEND_MESSAGE -> {
                scrollToEnd()
            }

            MessageListChangeReason.DELETE_MESSAGE -> {}
            MessageListChangeReason.UNKNOWN -> {
                //do nothing
            }

            else -> {}
        }
    }

    CompositionLocalProvider(LocalMessageListViewModel provides messageListViewModel) {

        Column(
            modifier = modifier
                .background(color = colors.bgColorOperate)
                .padding(horizontal = 16.dp)
        ) {
            LaunchedEffect(listState) {

                snapshotFlow {
                    listState.layoutInfo.visibleItemsInfo
                }.collect { visibleItems ->
                    if (!canLoadMore) {
                        return@collect
                    }
                    val firstVisibleIndex = visibleItems.firstOrNull()?.index ?: -1
                    val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: -1

                    if (firstVisibleIndex >= 0 && firstVisibleIndex <= 2) {
                        messageListViewModel.loadMoreNewerMessage()
                    }

                    if (lastVisibleIndex >= 0 && lastVisibleIndex >= messageList.size - 3) {
                        messageListViewModel.loadMoreOlderMessage()
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.any { it.pressed || it.positionChanged() }) {
                                    canLoadMore = true
                                }
                            }
                        }
                    },
                state = listState,
                reverseLayout = true,
                verticalArrangement = Arrangement.Top
            ) {
                if (messageListViewModel.loadingState.isLoadingNewer) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colors.textColorSecondary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                itemsIndexed(
                    items = messageList,
                    key = { _, item -> item.msgID!! }
                ) { index, message ->
                    MessageItem(
                        modifier = Modifier.animateItem(),
                        index = index,
                        message = message
                    ) {
                        onUserClick(it ?: "")
                    }
                }

                if (messageListViewModel.loadingState.isLoadingOlder) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colors.textColorSecondary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    index: Int,
    message: MessageInfo,
    onUserClick: (String?) -> Unit
) {
    val colors = LocalTheme.current.colors
    val messageViewModel = LocalMessageListViewModel.current
    val prevMessageIsAggregation = messageViewModel.checkPreviousMessageIsAggregation(index)
    val nextMessageIsAggregation = messageViewModel.checkNextMessageIsAggregation(index)
    val timeString = messageViewModel.getMessageTimeString(index)
//    val prevMessageIsAggregation = false
//    val nextMessageIsAggregation = false

    val topPadding = if (prevMessageIsAggregation) 2.dp else 10.dp
    val bottomPadding = if (nextMessageIsAggregation) 2.dp else 10.dp
    val isShowSelfAvatar = false

    CompositionLocalProvider(LocalMessage provides message) {

        val renderer = MessageRendererRegistry.getRenderer(message)
        if (renderer.showMessageMeta()) {

            val arrangement = when (AppBuilderConfig.messageAlignment) {
                MessageAlignment.LEFT -> Arrangement.Start
                MessageAlignment.RIGHT -> Arrangement.End
                else -> if (message.isSelf) Arrangement.End else Arrangement.Start
            }

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = topPadding, bottom = bottomPadding)
            ) {
                if (!timeString.isNullOrEmpty()) {
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
                    horizontalArrangement = arrangement,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    when (AppBuilderConfig.messageAlignment) {
                        MessageAlignment.TWO_SIDED -> {
                            if (message.isSelf) {
                                MessageStatusContent(modifier = Modifier.align(Alignment.CenterVertically))
                                MessageContent(nextMessageIsAggregation)
                                if (isShowSelfAvatar) {
                                    Spacer(Modifier.width(8.dp))
                                    Avatar(nextMessageIsAggregation) {
                                        onUserClick(message.sender)
                                    }
                                }
                            } else {
                                Avatar(nextMessageIsAggregation) {
                                    onUserClick(message.sender)
                                }
                                Spacer(Modifier.width(8.dp))
                                MessageContent(nextMessageIsAggregation)
                                MessageStatusContent(modifier = Modifier.align(Alignment.CenterVertically))
                            }
                        }

                        MessageAlignment.RIGHT -> {
                            MessageStatusContent(modifier = Modifier.align(Alignment.CenterVertically))
                            MessageContent(nextMessageIsAggregation)
                            Spacer(Modifier.width(8.dp))
                            Avatar(nextMessageIsAggregation) {
                                onUserClick(message.sender)
                            }
                        }

                        MessageAlignment.LEFT -> {
                            Avatar(nextMessageIsAggregation) {
                                onUserClick(message.sender!!)
                            }
                            Spacer(Modifier.width(8.dp))
                            MessageContent(nextMessageIsAggregation)
                            MessageStatusContent(modifier = Modifier.align(Alignment.CenterVertically))
                        }
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
fun Avatar(isAggregation: Boolean = false, onClick: () -> Unit) {
    val message = LocalMessage.current
    val faceUrl = message.rawMessage?.faceUrl
    val title = message.sender?.takeIf { it.isNotEmpty() }?.first()?.uppercase() ?: ""
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
fun MessageContent(isAggregation: Boolean = false, enableGesture: Boolean = true) {
    val message = LocalMessage.current
    val colors = LocalTheme.current.colors
    val viewModel = LocalMessageListViewModel.current
    var showActionDialog by remember { mutableStateOf(false) }

    val normalRadius = 16.dp
    val smallRadius = 1.dp
    val bottomStartRadius = if (isAggregation) {
        normalRadius
    } else {
        when (AppBuilderConfig.messageAlignment) {
            MessageAlignment.LEFT -> smallRadius
            MessageAlignment.RIGHT -> normalRadius
            else -> if (message.isSelf) normalRadius else smallRadius
        }
    }

    val bottomEndRadius = if (isAggregation) {
        normalRadius
    } else {
        when (AppBuilderConfig.messageAlignment) {
            MessageAlignment.LEFT -> normalRadius
            MessageAlignment.RIGHT -> smallRadius
            else -> if (message.isSelf) smallRadius else normalRadius
        }
    }

    val onMessageLongPress = {
        if (enableGesture) {
            showActionDialog = true
        }
    }

    val shape = RoundedCornerShape(normalRadius, normalRadius, bottomEndRadius, bottomStartRadius)
    BoxWithConstraints(
        modifier = Modifier
            .pointerInput(showActionDialog) {
                if (enableGesture) {
                    detectTapGestures(
                        onLongPress = {
                            onMessageLongPress()
                        },
                    )
                }
            }
            .clip(shape)
            .highlightBackground(
                highlightKey = message.msgID ?: "",
                color = if (message.isSelf) colors.bgColorBubbleOwn else colors.bgColorBubbleReciprocal,
                shape = shape
            )) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth * 0.9f)
        ) {
            CompositionLocalProvider(
                LocalMessageInteraction provides MessageInteraction(
                    onClick = {},
                    onLongPress = onMessageLongPress
                )
            ) {
                val renderer = MessageRendererRegistry.getRenderer(message)
                renderer.Render(message)
            }
        }
    }

    if (showActionDialog) {
        val actionItems = viewModel.getActions(message)
        if (actionItems.isEmpty()) {
            showActionDialog = false
        } else {
            ActionDialogBackground()
            FullScreenDialog(
                onDismissRequest = { showActionDialog = false },
            ) {
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showActionDialog = false }, contentAlignment = Alignment.Center
                ) {
                    val horizontalPadding = when (AppBuilderConfig.messageAlignment) {
                        MessageAlignment.LEFT -> 64.dp
                        MessageAlignment.RIGHT -> 64.dp
                        else -> if (message.isSelf) 16.dp else 64.dp
                    }
                    val alignment = when (AppBuilderConfig.messageAlignment) {
                        MessageAlignment.LEFT -> Alignment.Start
                        MessageAlignment.RIGHT -> Alignment.End
                        else -> if (message.isSelf) Alignment.End else Alignment.Start
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        horizontalAlignment = alignment,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MessageContent(isAggregation, enableGesture = false)
                        MessageActions() {
                            showActionDialog = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionDialogBackground() {
    val window = LocalActivity.current?.window
    val rootView = window?.decorView?.rootView
    rootView?.let {
        DisposableEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val renderEffect: RenderEffect =
                    RenderEffect.createBlurEffect(16f, 16f, Shader.TileMode.MIRROR)
                rootView.setRenderEffect(renderEffect)
                onDispose {
                    rootView.setRenderEffect(null)
                }
            } else {
                onDispose {}
            }
        }
    }
}

@Composable
fun MessageActions(onActionClick: () -> Unit) {
    val message = LocalMessage.current
    val viewModel = LocalMessageListViewModel.current
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }
    val allItems = viewModel.getActions(message)
    val pageSize = 4

    val colors = LocalTheme.current.colors
    val visibleItems by remember {
        derivedStateOf {
            val start = currentPage * pageSize
            val end = minOf(start + pageSize, allItems.size)
            allItems.subList(start, end)
        }
    }
    Column(
        horizontalAlignment = if (message.isSelf) Alignment.End else Alignment.Start,
        modifier = Modifier
            .background(color = colors.dropdownColorDefault, shape = RoundedCornerShape(16.dp))
            .widthIn(180.dp, 180.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        LazyColumn {
            items(visibleItems) { action ->
                MessageAction(action.name, action.icon, action.dangerousAction) {
                    onActionClick()
                    action.action(message)
                }
            }

        }
        if (allItems.size > pageSize) {
            HorizontalDivider(
                thickness = 0.5.dp, color = colors.strokeColorPrimary
            )

            MessageAction(
                text = stringResource(R.string.message_list_menu_more),
                resourceId = R.drawable.message_list_menu_more_icon,
                onClick = {
                    currentPage =
                        ((currentPage + 1) % ceil(allItems.size * 1.0f / pageSize)).toInt()
                })
        }
    }
}

@Composable
fun MessageAction(
    text: String,
    resourceId: Int,
    dangerousAction: Boolean = false,
    onClick: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier,
            maxLines = 1,
            fontWeight = FontWeight.W500,
            text = text,
            color = if (dangerousAction) colors.textColorError else colors.textColorPrimary
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .widthIn(16.dp, 80.dp)
        )
        Icon(
            painter = painterResource(resourceId),
            contentDescription = "",
            modifier = Modifier.size(16.dp, 16.dp),
            tint = colors.textColorLink
        )
    }

}

@Composable
fun MessageStatusContent(modifier: Modifier = Modifier) {
    var viewModel: MessageListViewModel = viewModel()
    val message = LocalMessage.current
    val colors = LocalTheme.current.colors
    val activity = LocalActivity.current
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (message.status == MessageStatus.SEND_FAIL) {
            var showResendTips by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .clickable {
                        showResendTips = true
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