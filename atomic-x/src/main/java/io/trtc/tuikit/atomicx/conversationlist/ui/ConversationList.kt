package io.trtc.tuikit.atomicx.conversationlist.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionItem
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionSheet
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarBadge
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarContent
import io.trtc.tuikit.atomicx.basecomponent.config.AppBuilderConfig
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.conversationlist.model.ConversationActionType
import io.trtc.tuikit.atomicx.conversationlist.viewmodels.ConversationListViewModelFactory
import io.trtc.tuikit.atomicx.conversationlist.viewmodels.ConversationViewModel
import io.trtc.tuikit.atomicxcore.api.ConversationInfo
import io.trtc.tuikit.atomicxcore.api.ConversationListStore
import io.trtc.tuikit.atomicxcore.api.ConversationReceiveOption
import kotlin.math.roundToInt

val LocalViewModel = compositionLocalOf<ConversationViewModel> { error("No ViewModel provided") }
val LocalConversation =
    compositionLocalOf<ConversationInfo> { error("No ConversationInfo provided") }
val LocalIsMultiSelect = compositionLocalOf { false }

@Composable
fun ConversationList(
    modifier: Modifier = Modifier,
    conversationListViewModelFactory: ConversationListViewModelFactory = ConversationListViewModelFactory(
        ConversationListStore.create()
    ),
    onConversationClick: (ConversationInfo) -> Unit = {},
) {
    val colors = LocalTheme.current.colors
    val multiSelect: Boolean = false

    val conversationViewModel =
        viewModel(ConversationViewModel::class, factory = conversationListViewModelFactory)

    val conversationList = conversationViewModel.conversationList
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    val currentSwipedItem = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(conversationList, listState) {
        snapshotFlow { conversationList to listState.firstVisibleItemIndex }
            .collect { (conversations, firstVisibleIndex) ->
                if (conversations.isNotEmpty() && firstVisibleIndex == 1) {
                    listState.animateScrollToItem(0)
                }
            }
    }

    LaunchedEffect(multiSelect) {
        conversationViewModel.clearSelection()
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (isScrolling) {
                    currentSwipedItem.value = null
                }
            }
    }

    CompositionLocalProvider(
        LocalViewModel provides conversationViewModel,
        LocalIsMultiSelect provides multiSelect
    ) {

        Box(
            modifier = modifier
                .background(color = colors.bgColorOperate)
        ) {

            LaunchedEffect(conversationList, listState) {

                snapshotFlow {
                    conversationList to listState.layoutInfo.visibleItemsInfo
                }.collect { (list, visibleItems) ->
                    val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: -1

                    if (lastVisibleIndex >= 0 && lastVisibleIndex >= list.size - 3) {
                        conversationViewModel.loadMoreConversation()
                    }
                }
            }

            var isShowMenu by remember { mutableStateOf(false) }
            var currentClickItem by remember { mutableStateOf<ConversationInfo?>(null) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.Top
            ) {
                itemsIndexed(
                    conversationList,
                    key = { index, item -> item.conversationID }) { index, conversation ->
                    SwipeConversationItem(
                        modifier = Modifier.animateItem(),
                        conversationInfo = conversation,
                        onClick = onConversationClick,
                        onReadClick = {
                            if (conversation.unreadCount > 0) {
                                conversationViewModel.setConversationAction(
                                    conversation,
                                    ConversationActionType.CLEAR_UNREAD_COUNT
                                )
                                conversationViewModel.setConversationAction(
                                    conversation,
                                    ConversationActionType.MARK_READ
                                )
                            } else {
                                conversationViewModel.setConversationAction(
                                    conversation,
                                    ConversationActionType.MARK_UNREAD
                                )
                            }
                        },
                        onMoreClick = { isShowMenu = true; currentClickItem = it },
                        currentSwipedItem = currentSwipedItem
                    )
                }
            }

            if (isShowMenu) {
                val actions =
                    conversationViewModel.getActions(conversationInfo = currentClickItem!!)
                if (!actions.isEmpty()) {
                    ActionSheet(
                        isVisible = isShowMenu,
                        options = actions.map {
                            ActionItem(
                                text = stringResource(it.titleResID),
                                isDestructive = it.dangerous,
                                value = it.type
                            )
                        },
                        onDismiss = { isShowMenu = false },
                    ) {
                        conversationViewModel.setConversationAction(
                            currentClickItem!!,
                            it.value as ConversationActionType
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeConversationItem(
    conversationInfo: ConversationInfo,
    onClick: (ConversationInfo) -> Unit,
    modifier: Modifier = Modifier,
    onReadClick: (ConversationInfo) -> Unit = {},
    onMoreClick: (ConversationInfo) -> Unit = {},
    currentSwipedItem: MutableState<String?> = mutableStateOf(null)
) {
    val colors = LocalTheme.current.colors
    val density = LocalDensity.current
    val itemId = conversationInfo.conversationID ?: ""
    val hasUnreadCount = conversationInfo.unreadCount > 0
    val showActions = AppBuilderConfig.conversationActionList.isNotEmpty()

    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeItemWidth = with(density) { 68.dp.toPx() }
    val swipeThreshold =
        if (hasUnreadCount && showActions) swipeItemWidth * 2
        else if (hasUnreadCount || showActions) swipeItemWidth
        else 0f
    var targetOffsetX by remember { mutableFloatStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = targetOffsetX,
        label = "swipe_animation"
    )

    LaunchedEffect(currentSwipedItem.value) {
        if (currentSwipedItem.value != itemId) {
            offsetX = 0f
            targetOffsetX = 0f
        }
    }

    val draggableState = rememberDraggableState { delta ->
        val newOffset = offsetX + delta
        if (newOffset <= 0 && newOffset >= -swipeThreshold) {
            offsetX = newOffset
            targetOffsetX = newOffset

            if (newOffset < -10f) {
                currentSwipedItem.value = itemId
            } else if (newOffset >= 0f && currentSwipedItem.value == itemId) {
                currentSwipedItem.value = null
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .wrapContentWidth()
                .align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showActions) {
                Column(
                    modifier = Modifier
                        .width(68.dp)
                        .background(color = colors.textColorAntiPrimary)
                        .fillMaxHeight()
                        .clickable {
                            currentSwipedItem.value = null
                            onMoreClick(conversationInfo)
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.conversation_list_more_icon),
                        contentDescription = "More",
                        tint = colors.textColorButton,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.conversation_list_more),
                        fontSize = 12.sp,
                        color = colors.textColorButton,
                        fontWeight = FontWeight.W400
                    )
                }
            }
            if (hasUnreadCount) {
                Column(
                    modifier = Modifier
                        .width(68.dp)
                        .background(color = if (hasUnreadCount) colors.textColorLink else colors.textColorTertiary)
                        .padding(horizontal = 8.dp)
                        .fillMaxHeight()
                        .clickable {
                            currentSwipedItem.value = null
                            onReadClick(conversationInfo)
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(if (hasUnreadCount) R.drawable.conversation_list_mark_read_icon else R.drawable.conversation_list_mark_unread_icon),
                        contentDescription = "Read",
                        tint = colors.textColorButton,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasUnreadCount) stringResource(R.string.conversation_list_mark_read) else stringResource(
                            R.string.conversation_list_mark_unread
                        ),
                        fontSize = 12.sp,
                        color = colors.textColorButton,
                        fontWeight = FontWeight.W400
                    )
                }
            }
        }

        val multiSelect = LocalIsMultiSelect.current
        val draggableModifier = if (multiSelect) {
            Modifier
        } else {
            Modifier.draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    val newOffset = if (offsetX < -swipeThreshold / 2) -swipeThreshold else 0f
                    offsetX = newOffset
                    targetOffsetX = newOffset
                    if (newOffset == 0f) {
                        if (currentSwipedItem.value == itemId) {
                            currentSwipedItem.value = null
                        }
                    } else {
                        currentSwipedItem.value = itemId
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .then(draggableModifier)

        ) {
            ConversationItem(
                conversationInfo = conversationInfo,
                onConversationClick = { conversation ->
                    if (currentSwipedItem.value != conversation.conversationID) {
                        onClick(conversation)
                    }
                    currentSwipedItem.value = null
                }
            )
        }
    }
}

@Composable
fun ConversationItem(
    conversationInfo: ConversationInfo,
    onConversationClick: (ConversationInfo) -> Unit
) {
    val colors = LocalTheme.current.colors
    val multiSelect = LocalIsMultiSelect.current
    val viewModel = LocalViewModel.current
    val selectedConversations by LocalViewModel.current.selectedConversations.collectAsState()
    val isReceiveMessage = conversationInfo.receiveOption == ConversationReceiveOption.RECEIVE
    val onClick = {
        if (multiSelect) {
            if (selectedConversations.contains(conversationInfo)) {
                viewModel.removeSelection(conversationInfo)
            } else {
                viewModel.addSelection(conversationInfo)
            }
        } else {
            onConversationClick(conversationInfo)
        }
    }
    CompositionLocalProvider(LocalConversation provides conversationInfo) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable {
                    onClick()
                }
                .background(color = if (conversationInfo.isPinned) colors.bgColorInput else colors.bgColorOperate)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (multiSelect) {
                ConversationCheckBox(
                    checked = selectedConversations.contains(conversationInfo),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Avatar(
                content = AvatarContent.Image(
                    url = conversationInfo.avatarURL,
                    fallbackName = conversationInfo.title ?: conversationInfo.conversationID
                ),
                badge = if (!isReceiveMessage && conversationInfo.unreadCount > 0) AvatarBadge.Dot else AvatarBadge.None,
            ) {
                onClick()
            }
            Spacer(modifier = Modifier.width(8.dp))
            ConversationContent()
        }
    }
}

@Composable
fun ConversationCheckBox(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTheme.current.colors
    Box(
        modifier = modifier
            .size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = colors.textColorLink,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checked",
                    tint = colors.textColorButton,
                    modifier = Modifier.size(12.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(
                        width = 1.dp,
                        color = colors.scrollbarColorDefault,
                        shape = CircleShape
                    )
            )
        }
    }
}

