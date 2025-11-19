package io.trtc.tuikit.atomicx.chatsetting.ui

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.chatsetting.viewmodels.GroupMemberListViewModel
import io.trtc.tuikit.atomicxcore.api.contact.GroupMember
import io.trtc.tuikit.atomicxcore.api.contact.GroupMemberRole
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun GroupMemberList(
    isVisible: Boolean,
    groupId: String,
    title: String = "",
    isSelectionMode: Boolean = true,
    isSingleSelect: Boolean = false,
    preSelectedMembers: List<String> = emptyList(),
    currentUserRole: GroupMemberRole = GroupMemberRole.MEMBER,
    refreshKey: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (List<GroupMember>) -> Unit = {},
    onMemberClick: (GroupMember) -> Unit = {},
    onSetAsAdmin: (GroupMember) -> Unit = {},
    onRemoveAdmin: (GroupMember) -> Unit = {},
    onRemoveMember: (GroupMember) -> Unit = {},
    onViewInfo: (GroupMember) -> Unit = {}
) {
    if (isVisible) {
        val viewModel: GroupMemberListViewModel = viewModel<GroupMemberListViewModel>(
            key = refreshKey.toString()
        ) {
            GroupMemberListViewModel(groupId)
        }

        var selectedMembers by remember { mutableStateOf(preSelectedMembers.toSet()) }

        LaunchedEffect(isVisible, groupId) {
            if (isVisible) {
                selectedMembers = preSelectedMembers.toSet()
            }
        }

        FullScreenDialog(
            onDismissRequest = onDismiss,
        ) {
            GroupMemberPickerFullscreenContent(
                title = title,
                isSelectionMode = isSelectionMode,
                isSingleSelect = isSingleSelect,
                selectedMembers = selectedMembers,
                currentUserRole = currentUserRole,
                onSelectionChanged = { userID, isSelected ->
                    selectedMembers = if (isSingleSelect) {
                        if (isSelected) setOf(userID) else emptySet()
                    } else {
                        if (isSelected) selectedMembers + userID else selectedMembers - userID
                    }
                },
                onDismiss = onDismiss,
                onConfirm = { members ->
                    if (isSelectionMode) {
                        val selectedMemberItems = members.filter {
                            selectedMembers.contains(it.userID)
                        }
                        onConfirm(selectedMemberItems)
                    }
                },
                onMemberClick = onMemberClick,
                onSetAsAdmin = onSetAsAdmin,
                onRemoveAdmin = onRemoveAdmin,
                onRemoveMember = onRemoveMember,
                onViewInfo = onViewInfo,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun GroupMemberPickerFullscreenContent(
    title: String,
    isSelectionMode: Boolean,
    isSingleSelect: Boolean,
    selectedMembers: Set<String>,
    currentUserRole: GroupMemberRole,
    onSelectionChanged: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (List<GroupMember>) -> Unit,
    onMemberClick: (GroupMember) -> Unit,
    onSetAsAdmin: (GroupMember) -> Unit,
    onRemoveAdmin: (GroupMember) -> Unit,
    onRemoveMember: (GroupMember) -> Unit,
    onViewInfo: (GroupMember) -> Unit,
    viewModel: GroupMemberListViewModel
) {
    val colors = LocalTheme.current.colors
    val members by viewModel.members.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showActionSheet by remember { mutableStateOf(false) }
    var selectedGroupMemberForAction by remember { mutableStateOf<GroupMember?>(null) }
    var isLoadingMoreRequested by remember { mutableStateOf(false) }

    LaunchedEffect(members.size) {
        isLoadingMoreRequested = false
    }

    LaunchedEffect(listState, members.size) {
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            Pair(total > 0 && lastVisible >= total - 1, total)
        }.distinctUntilChanged().collect { (endReached, _) ->
            if (endReached && !isLoadingMoreRequested && members.isNotEmpty()) {
                isLoadingMoreRequested = true
                viewModel.loadMoreMembers()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            GroupMemberPickerTopBar(
                title = title,
                isSelectionMode = isSelectionMode,
                selectedCount = selectedMembers.size,
                onBackClick = onDismiss,
                onConfirmClick = { onConfirm(members) },
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState
            ) {
                items(items = members, key = { it.userID }) { member ->
                    GroupMemberItem(
                        groupMember = member,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedMembers.contains(member.userID),
                        currentUserRole = currentUserRole,
                        onSelectionChanged = { isSelected ->
                            onSelectionChanged(member.userID, isSelected)
                        },
                        onMemberClick = { clickedMember ->
                            if (isSelectionMode) {
                                onMemberClick(clickedMember)
                            } else {
                                if (hasActionPermission(
                                        currentUserRole.value,
                                        clickedMember.role.value
                                    )
                                ) {
                                    selectedGroupMemberForAction = clickedMember
                                    showActionSheet = true
                                } else {
                                    onMemberClick(clickedMember)
                                }
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        GroupMemberActionSheet(
            isVisible = showActionSheet,
            groupMember = selectedGroupMemberForAction,
            currentUserRole = currentUserRole,
            onDismiss = {
                showActionSheet = false
                selectedGroupMemberForAction = null
            },
            onSetAsAdmin = { member ->
                onSetAsAdmin(member)
                showActionSheet = false
                selectedGroupMemberForAction = null
            },
            onRemoveAdmin = { member ->
                onRemoveAdmin(member)
                showActionSheet = false
                selectedGroupMemberForAction = null
            },
            onRemoveMember = { member ->
                onRemoveMember(member)
                showActionSheet = false
                selectedGroupMemberForAction = null
            },
            onViewInfo = { member ->
                onViewInfo(member)
                showActionSheet = false
                selectedGroupMemberForAction = null
            }
        )
    }
}

@Composable
private fun GroupMemberPickerTopBar(
    title: String,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    val colors = LocalTheme.current.colors

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colors.bgColorOperate)
                .padding(horizontal = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onBackClick() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = colors.textColorLink,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = stringResource(R.string.chat_setting_back),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorLink
                )
            }


            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textColorPrimary,
                modifier = Modifier.align(Alignment.Center)
            )

            if (isSelectionMode) {

                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onConfirmClick() },
                    text = if (selectedCount > 0) "${stringResource(R.string.base_component_confirm)}($selectedCount)"
                    else stringResource(R.string.base_component_confirm),
                    fontSize = 14.sp,
                    color = colors.textColorLink,
                    fontWeight = FontWeight.Medium
                )
            }
        }

    }
}

@Composable
private fun GroupMemberItem(
    groupMember: GroupMember,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    currentUserRole: GroupMemberRole,
    onSelectionChanged: (Boolean) -> Unit,
    onMemberClick: (GroupMember) -> Unit
) {
    val colors = LocalTheme.current.colors

    val roleText = when (groupMember.role) {
        GroupMemberRole.OWNER -> stringResource(R.string.chat_setting_member_type_owner)
        GroupMemberRole.ADMIN -> stringResource(R.string.chat_setting_member_type_administrator)
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode) {
                    onSelectionChanged(!isSelected)
                } else {
                    onMemberClick(groupMember)
                }
            }
            .background(colors.bgColorOperate)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (isSelectionMode) {
            ChatSettingCheckbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Avatar(
            url = groupMember.avatarURL ?: "",
            name = groupMember.displayName,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = groupMember.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                roleText?.let {
                    Box(
                        modifier = Modifier
                            .height(15.dp)
                            .background(
                                color = colors.textColorLinkDisabled,
                                shape = RoundedCornerShape(3.dp)
                            )
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = roleText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.W400,
                            color = colors.textColorLink,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                        )
                    }

                }

            }

        }

    }
}

private fun hasActionPermission(currentUserRole: Int, targetMemberRole: Int): Boolean {
    return when (currentUserRole) {
        V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER -> {
            targetMemberRole != V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER
        }

        V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_ADMIN -> {

            targetMemberRole == V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER
        }

        else -> false
    }
}