package io.trtc.tuikit.atomicx.messagelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.chatsetting.ui.displayName
import io.trtc.tuikit.atomicx.messagelist.ui.MessageContent
import io.trtc.tuikit.atomicx.messagelist.utils.DateTimeUtils
import io.trtc.tuikit.atomicx.messagelist.viewmodels.MessageReadReceiptViewModel
import io.trtc.tuikit.atomicxcore.api.contact.GroupMember
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

@Composable
fun MessageReadReceiptDialog(
    isVisible: Boolean,
    message: MessageInfo?,
    onDismiss: () -> Unit,
    onUserClick: (String) -> Unit = {}
) {
    if (isVisible && message != null) {
        FullScreenDialog(
            onDismissRequest = onDismiss
        ) {
            val viewModel: MessageReadReceiptViewModel = viewModel(
                factory = MessageReadReceiptViewModel.Factory(message)
            )

            MessageReadReceiptScreen(
                message = message,
                viewModel = viewModel,
                onBack = onDismiss,
                onUserClick = onUserClick
            )
        }
    }
}

@Composable
fun MessageReadReceiptScreen(
    message: MessageInfo,
    viewModel: MessageReadReceiptViewModel,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit = {}
) {
    val colors = LocalTheme.current.colors

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val readMemberList by viewModel.readMemberList.collectAsState()
    val hasMoreReadMembers by viewModel.hasMoreReadMembers.collectAsState()
    val unReadMemberList by viewModel.unReadMemberList.collectAsState()
    val hasMoreUnReadMembers by viewModel.hasMoreUnReadMembers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadReadMembers()
        viewModel.loadUnreadMembers()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
            .systemBarsPadding(),
        topBar = {
            MessageReadReceiptTopBar(onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.bgColorOperate)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            DateLabel(message = message)
            Spacer(modifier = Modifier.height(12.dp))
            MessageBubble(message)
            Spacer(modifier = Modifier.height(24.dp))

            CustomTabBar(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                message = message
            )

            when (selectedTabIndex) {
                0 -> {
                    MemberList(
                        memberList = readMemberList,
                        hasMoreMembers = hasMoreReadMembers,
                        onLoadMore = { viewModel.loadMoreReadMembers() },
                        onUserClick = onUserClick
                    )
                }

                1 -> {
                    MemberList(
                        memberList = unReadMemberList,
                        hasMoreMembers = hasMoreUnReadMembers,
                        onLoadMore = { viewModel.loadMoreUnreadMembers() },
                        onUserClick = onUserClick
                    )
                }
            }
        }
    }
}

@Composable
fun MessageReadReceiptTopBar(onBack: () -> Unit) {
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.bgColorOperate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.contact_list_back),
                    modifier = Modifier.size(20.dp),
                    tint = colors.textColorLink
                )
                Text(
                    text = stringResource(R.string.contact_list_back),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    color = colors.textColorLink
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.message_list_read_receipt_detail),
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1,
                color = colors.textColorPrimary
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(60.dp))
        }
        HorizontalDivider(thickness = 1.dp, color = colors.strokeColorSecondary)
    }
}

@Composable
fun CustomTabBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    message: MessageInfo
) {
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabItem(
                title = stringResource(R.string.message_list_read_receipt_read_by),
                iconResId = R.drawable.message_list_all_read_icon,
                isSelected = selectedTabIndex == 0,
                iconTintColor = colors.textColorLink,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )

            TabItem(
                title = stringResource(R.string.message_list_read_receipt_delivered_to),
                iconResId = R.drawable.message_list_unread_icon,
                isSelected = selectedTabIndex == 1,
                iconTintColor = colors.textColorSecondary,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun TabItem(
    title: String,
    iconResId: Int,
    iconTintColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(40.dp))
            .background(
                if (isSelected) {
                    colors.tabColorSelected
                } else {
                    colors.tabColorUnselected
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = iconTintColor
                )

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = if (isSelected) colors.textColorPrimary else colors.textColorSecondary
                )
            }

        }
    }
}

@Composable
fun DateLabel(message: MessageInfo) {
    val colors = LocalTheme.current.colors
    val formattedDate = DateTimeUtils.getTimeString(message.timestamp?.times(1000))
    if (!formattedDate.isNullOrEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    color = colors.textColorSecondary
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageInfo
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        val maxContentHeight = LocalWindowInfo.current.containerSize.let {
            with(LocalDensity.current) {
                it.height.toDp()
            }
        }
        Column(
            modifier = Modifier
                .heightIn(max = maxContentHeight * 0.4f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MessageContent(message)
        }
    }
}

@Composable
fun MemberList(
    memberList: List<GroupMember>,
    hasMoreMembers: Boolean,
    onLoadMore: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= memberList.size - 1 && hasMoreMembers
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        items(memberList, key = { it.userID }) { member ->
            UserRow(
                member = member,
                onUserClick = onUserClick
            )
        }

    }
}

@Composable
fun UserRow(
    member: GroupMember,
    onUserClick: (String) -> Unit
) {
    val colors = LocalTheme.current.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable { onUserClick(member.userID) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Avatar(
                url = member.avatarURL,
                name = member.displayName,
                size = AvatarSize.S
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = member.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = colors.textColorPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}
