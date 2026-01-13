package io.trtc.tuikit.atomicx.messagelist.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionItem
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionSheet
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AlertDialog
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Toast
import io.trtc.tuikit.atomicx.basecomponent.config.MessageAlignment
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.basecomponent.utils.EventBus
import io.trtc.tuikit.atomicx.emojipicker.RecentEmojiManager
import io.trtc.tuikit.atomicx.messagelist.MessageReadReceiptDialog
import io.trtc.tuikit.atomicx.messagelist.config.ChatMessageListConfig
import io.trtc.tuikit.atomicx.messagelist.config.MessageListConfigProtocol
import io.trtc.tuikit.atomicx.messagelist.model.MessageCustomAction
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MultiSelectBottomBar
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.ReactionDetailSheet
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.ReactionEmojiPicker
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.ReactionEmojiPickerSheet
import io.trtc.tuikit.atomicx.messagelist.viewmodels.HighlightManager
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MessageListViewModel
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MessageListViewModelFactory
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MessageUIAction
import io.trtc.tuikit.atomicx.messagelist.viewmodels.forwardMessageCountLimit
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.message.MessageEvent
import io.trtc.tuikit.atomicxcore.api.message.MessageForwardType
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageListStore
import io.trtc.tuikit.atomicxcore.api.message.MessageListType
import io.trtc.tuikit.atomicxcore.api.message.MessageStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.ceil

val LocalMessageListViewModel =
    compositionLocalOf<MessageListViewModel> { error("No ViewModel provided") }
val LocalMessageListConfig = compositionLocalOf<MessageListConfigProtocol> { ChatMessageListConfig() }
val LocalCustomActions = compositionLocalOf<List<MessageCustomAction>> { emptyList() }
val LocalMessageRenderConfig = compositionLocalOf { MessageRenderConfig() }

@Composable
fun MessageList(
    conversationID: String,
    modifier: Modifier = Modifier,
    config: MessageListConfigProtocol = ChatMessageListConfig(),
    customActions: List<MessageCustomAction> = emptyList(),
    locateMessage: MessageInfo? = null,
    messageListViewModelFactory: MessageListViewModelFactory = MessageListViewModelFactory(
        messageListStore = MessageListStore.create(conversationID, MessageListType.HISTORY),
        locateMessage = locateMessage,
        messageListConfig = config
    ),
    onMultiSelectStateChanged: (Boolean) -> Unit = {},
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
        LocalMessageListConfig provides config,
        LocalCustomActions provides customActions
    ) {
        MessageList(modifier, messageListViewModel, onMultiSelectStateChanged, onUserClick)
    }
}


@Composable
private fun MessageList(
    modifier: Modifier,
    messageListViewModel: MessageListViewModel,
    onMultiSelectStateChanged: (Boolean) -> Unit,
    onUserClick: (String) -> Unit = {},
) {
    val colors = LocalTheme.current.colors
    val density = LocalDensity.current
    val context = LocalContext.current
    val config = LocalMessageListConfig.current
    val configuration = LocalConfiguration.current
    val messageList by messageListViewModel.messageList.collectAsState()
    val isMultiSelectMode by messageListViewModel.isMultiSelectMode.collectAsState()
    val selectedMessages by messageListViewModel.selectedMessages.collectAsState()
    val singleMessageToForward by messageListViewModel.onSingleMessageForward.collectAsState()
    val readReceiptMessage by messageListViewModel.readReceiptMessage.collectAsState()
    val longPressActionMessage by messageListViewModel.longPressActionMessage.collectAsState()
    val reactionDetailMessage by messageListViewModel.reactionDetailMessage.collectAsState()
    val showReactionEmojiPickerForMessage by messageListViewModel.showEmojiPickerForMessage.collectAsState()
    val isListInitialized = messageList.isNotEmpty()
    var showForwardDialog by remember { mutableStateOf(false) }
    var showForwardTypeSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var forwardingMessages by remember { mutableStateOf<List<MessageInfo>>(emptyList()) }

    LaunchedEffect(singleMessageToForward) {
        singleMessageToForward?.let { message ->
            forwardingMessages = listOf(message)
            messageListViewModel.forwardType = MessageForwardType.SEPARATE
            showForwardDialog = true
        }
    }

    LaunchedEffect(isMultiSelectMode) {
        if (isMultiSelectMode) {
            onMultiSelectStateChanged(true)
        } else {
            onMultiSelectStateChanged(false)
        }
    }

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
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    suspend fun animateScrollToEnd() {
        isProgrammaticScroll = true
        canLoadMore = false
        try {
            listState.animateScrollToItem(0)
            delay(100)
        } finally {
            isProgrammaticScroll = false
        }
    }

    suspend fun scrollToEnd() {
        val itemCount = listState.layoutInfo.totalItemsCount
        val firstVisible = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: -1

        if (itemCount == 0) {
            return
        }

        isProgrammaticScroll = true
        canLoadMore = false
        try {
            listState.scrollToItem(0)
            delay(100)
            val afterScroll = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: -1
        } finally {
            isProgrammaticScroll = false
        }
    }

    LaunchedEffect(messageList) {
        messageListViewModel.messageEvent.collect { event ->
            when (event) {
                is MessageEvent.RecvMessage -> {
                    val shouldScrollToEnd = with(listState) {
                        val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                        val result = firstVisible <= 2
                        result
                    }

                    if (shouldScrollToEnd) {
                        snapshotFlow { listState.layoutInfo.totalItemsCount }
                            .first { it > 0 }
                        scrollToEnd()
                    }
                }

                is MessageEvent.FetchMessages -> {
                }

                is MessageEvent.FetchMoreMessages -> {
                }

                is MessageEvent.SendMessage -> {
                    snapshotFlow { listState.layoutInfo.totalItemsCount }
                        .first { it > 0 }
                    scrollToEnd()
                }

                is MessageEvent.DeleteMessages -> {
                }
            }
        }
    }

    CompositionLocalProvider(LocalMessageListViewModel provides messageListViewModel) {

        Box(modifier = modifier) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colors.bgColorOperate)
                    .padding(horizontal = config.horizontalPadding)
            ) {
                LaunchedEffect(listState) {

                    snapshotFlow {
                        listState.layoutInfo.visibleItemsInfo
                    }.collect { visibleItems ->
                        launch {
                            val visibleMessages = visibleItems.mapNotNull { itemInfo ->
                                messageList.getOrNull(itemInfo.index)
                            }
                            messageListViewModel.sendReadReceipts(visibleMessages)
                        }

                        if (!canLoadMore || isProgrammaticScroll) {
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
                        .weight(1f)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.any { it.pressed || it.positionChanged() }) {
                                        if (!isProgrammaticScroll) {
                                            canLoadMore = true
                                        }
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
                            message = message,
                            onUserLongPress = {
                                EventBus.post {
                                    mapOf(
                                        "source" to "MessageList",
                                        "event" to "onUserLongPress",
                                        "userID" to it,
                                        "message" to message
                                    )
                                }
                            }, onUserClick = {
                                onUserClick(it ?: "")
                            })
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

                if (isMultiSelectMode) {
                    MultiSelectBottomBar(
                        selectedCount = selectedMessages.size,
                        onCancel = { messageListViewModel.exitMultiSelectMode() },
                        onDelete = {
                            if (selectedMessages.isNotEmpty()) {
                                showDeleteDialog = true
                            }
                        },
                        onForward = {
                            if (selectedMessages.isNotEmpty()) {
                                val hasFailedMessage = selectedMessages.any { it.status != MessageStatus.SEND_SUCCESS }
                                if (hasFailedMessage) {
                                    Toast.error(context, context.getString(R.string.message_list_forward_failed_tip))
                                    return@MultiSelectBottomBar
                                }
                                forwardingMessages = selectedMessages.toList()
                                showForwardTypeSheet = true
                            }
                        }
                    )
                }
            }

            ActionSheet(
                isVisible = showForwardTypeSheet,
                options = listOf(
                    ActionItem(
                        text = stringResource(R.string.message_list_forward_by_separate),
                        value = MessageForwardType.SEPARATE
                    ),
                    ActionItem(
                        text = stringResource(R.string.message_list_forward_by_merge),
                        value = MessageForwardType.MERGED
                    )
                ),
                onDismiss = {
                    showForwardTypeSheet = false
                }) {
                val selectedForwardType = it.value as MessageForwardType
                if (selectedForwardType == MessageForwardType.SEPARATE && forwardingMessages.size > forwardMessageCountLimit) {
                    Toast.error(context, context.getString(R.string.message_list_forward_oneByOne_limit_number_tip))
                    showForwardTypeSheet = false
                    return@ActionSheet
                }
                messageListViewModel.forwardType = selectedForwardType
                showForwardTypeSheet = false
                showForwardDialog = true
            }
            if (showForwardDialog) {
                ForwardTargetSelector(
                    onDismiss = {
                        showForwardDialog = false
                        messageListViewModel.clearSingleMessageForward()
                    },
                    onConfirm = { conversationIDList ->
                        showForwardDialog = false
                        val messagesToForward = forwardingMessages.toList()
                        if (isMultiSelectMode) {
                            messageListViewModel.exitMultiSelectMode()
                        }
                        messageListViewModel.forwardMessages(
                            messageList = messagesToForward,
                            conversationIDList = conversationIDList,
                            completion = object : CompletionHandler {
                                override fun onSuccess() {
                                    messageListViewModel.clearSingleMessageForward()
                                }

                                override fun onFailure(code: Int, desc: String) {
                                    messageListViewModel.clearSingleMessageForward()
                                }
                            }
                        )
                    }
                )
            }
            MessageReadReceiptDialog(
                isVisible = readReceiptMessage != null,
                message = readReceiptMessage,
                onDismiss = {
                    messageListViewModel.clearReadReceiptDialog()
                },
                onUserClick = { userID ->
                    onUserClick(userID)
                }
            )
            AlertDialog(
                isVisible = showDeleteDialog,
                onDismiss = { showDeleteDialog = false },
                message = stringResource(R.string.message_list_delete_messages_tips),
                onCancel = {
                    showDeleteDialog = false
                },
                onConfirm = {
                    messageListViewModel.deleteSelectedMessages()
                    messageListViewModel.exitMultiSelectMode()
                    showDeleteDialog = false
                })

            if (longPressActionMessage != null) {
                val actionItems = messageListViewModel.getActions(longPressActionMessage!!)
                val customActions = LocalCustomActions.current
                if (actionItems.isEmpty() && customActions.isEmpty()) {
                    messageListViewModel.clearLongPressActionDialog()
                } else {
                    ActionDialogBackground()
                    FullScreenDialog(
                        onDismissRequest = { messageListViewModel.clearLongPressActionDialog() },
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { messageListViewModel.clearLongPressActionDialog() },
                            contentAlignment = Alignment.Center
                        ) {
                            val startPadding = when (config.alignment) {
                                MessageAlignment.LEFT -> 64.dp
                                MessageAlignment.RIGHT -> 16.dp
                                else -> if (longPressActionMessage!!.isSelf) 16.dp else 64.dp
                            }
                            val endPadding = when (config.alignment) {
                                MessageAlignment.LEFT -> 16.dp
                                MessageAlignment.RIGHT -> 64.dp
                                else -> 16.dp
                            }
                            val alignment = when (config.alignment) {
                                MessageAlignment.LEFT -> Alignment.Start
                                MessageAlignment.RIGHT -> Alignment.End
                                else -> if (longPressActionMessage!!.isSelf) Alignment.End else Alignment.Start
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = startPadding, end = endPadding),
                                horizontalAlignment = alignment,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (config.isSupportReaction && longPressActionMessage!!.status == MessageStatus.SEND_SUCCESS) {
                                    ReactionEmojiPicker(
                                        onEmojiClick = { emoji ->
                                            val isReacted =
                                                longPressActionMessage?.reactionList?.any { it.reactionID == emoji.key && it.reactedByMyself }
                                            if (isReacted == true) {
                                                messageListViewModel.removeMessageReaction(
                                                    longPressActionMessage!!,
                                                    emoji.key
                                                )
                                            } else {
                                                messageListViewModel.addMessageReaction(
                                                    longPressActionMessage!!,
                                                    emoji.key
                                                )
                                                RecentEmojiManager.updateRecentEmoji(emoji.key)
                                            }
                                            messageListViewModel.clearLongPressActionDialog()
                                        },
                                        onExpandClick = {
                                            messageListViewModel.showEmojiPicker(longPressActionMessage!!)
                                            messageListViewModel.clearLongPressActionDialog()
                                        }
                                    )
                                }

                                // Limit message content height to prevent overflow
                                val maxContentHeight = LocalWindowInfo.current.containerSize.let {
                                    with(LocalDensity.current) {
                                        it.height.toDp()
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .heightIn(max = maxContentHeight * 0.4f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    MessageContent(longPressActionMessage!!, false, enableGesture = false)
                                }

                                MessageActions(longPressActionMessage!!) {
                                    messageListViewModel.clearLongPressActionDialog()
                                }
                            }
                        }
                    }
                }
            }

            ReactionDetailSheet(
                isVisible = reactionDetailMessage != null,
                reactionList = reactionDetailMessage?.reactionList ?: emptyList(),
                currentUserID = messageListViewModel.getCurrentUserID(),
                onDismiss = { messageListViewModel.clearReactionDetail() },
                onFetchUsers = { reactionID -> messageListViewModel.fetchReactionUsers(reactionID) },
                onRemoveReaction = { reactionID ->
                    reactionDetailMessage?.let { message ->
                        messageListViewModel.removeMessageReaction(message, reactionID)
                    }
                    messageListViewModel.clearReactionDetail()
                }
            )

            if (showReactionEmojiPickerForMessage != null) {
                ReactionEmojiPickerSheet(
                    onDismiss = { messageListViewModel.clearEmojiPicker() },
                    onEmojiClick = { emoji ->
                        val isReacted = showReactionEmojiPickerForMessage!!.reactionList.any {
                            it.reactionID == emoji.key && it.reactedByMyself
                        }
                        if (isReacted) {
                            messageListViewModel.removeMessageReaction(showReactionEmojiPickerForMessage!!, emoji.key)
                        } else {
                            messageListViewModel.addMessageReaction(showReactionEmojiPickerForMessage!!, emoji.key)
                            RecentEmojiManager.updateRecentEmoji(emoji.key)
                        }
                        messageListViewModel.clearEmojiPicker()
                    }
                )
            }

            // Auxiliary Text Forward handling (ASR / Translation)
            val auxiliaryTextForwardContent by messageListViewModel.auxiliaryTextForwardContent.collectAsState()

            if (auxiliaryTextForwardContent != null) {
                ForwardTargetSelector(
                    onDismiss = {
                        messageListViewModel.clearAuxiliaryTextForward()
                    },
                    onConfirm = { conversationIDList ->
                        val textToForward = auxiliaryTextForwardContent!!
                        messageListViewModel.sendAuxiliaryTextToConversations(
                            text = textToForward,
                            conversationIDList = conversationIDList,
                            completion = object : CompletionHandler {
                                override fun onSuccess() {
                                    messageListViewModel.clearAuxiliaryTextForward()
                                }

                                override fun onFailure(code: Int, desc: String) {
                                    messageListViewModel.clearAuxiliaryTextForward()
                                }
                            }
                        )
                    }
                )
            }

            // Handle scroll to bottom after ASR conversion success
            val shouldScrollToBottomAfterAsr by messageListViewModel.shouldScrollToBottomAfterAsr.collectAsState()
            LaunchedEffect(shouldScrollToBottomAfterAsr) {
                if (shouldScrollToBottomAfterAsr) {
                    delay(100) // Wait for UI update
                    animateScrollToEnd()
                    messageListViewModel.clearScrollToBottomAfterAsr()
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
fun MessageActions(message: MessageInfo, onActionClick: () -> Unit) {
    val config = LocalMessageListConfig.current
    val customActions = LocalCustomActions.current
    val viewModel = LocalMessageListViewModel.current
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }
    val allItems = viewModel.getActions(message) + customActions.map {
        MessageUIAction(
            name = it.title,
            icon = it.iconResID,
            action = it.action
        )
    }
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



