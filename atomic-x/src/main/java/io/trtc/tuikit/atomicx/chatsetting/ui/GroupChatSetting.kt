package io.trtc.tuikit.atomicx.chatsetting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionItem
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionSheet
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.chatsetting.permission.GroupPermission
import io.trtc.tuikit.atomicx.chatsetting.permission.GroupPermissionManager
import io.trtc.tuikit.atomicx.chatsetting.viewmodels.GroupChatSettingViewModel
import io.trtc.tuikit.atomicx.chatsetting.viewmodels.GroupChatSettingViewModelFactory
import io.trtc.tuikit.atomicx.chatsetting.viewmodels.getGroupAvatarUrls
import io.trtc.tuikit.atomicxcore.api.contact.GroupJoinOption
import io.trtc.tuikit.atomicxcore.api.contact.GroupMember
import io.trtc.tuikit.atomicxcore.api.contact.GroupMemberRole
import io.trtc.tuikit.atomicxcore.api.contact.GroupType
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val LocalGroupViewModel =
    compositionLocalOf<GroupChatSettingViewModel> { error("No LocalGroupViewModel") }

@Composable
fun GroupChatSetting(
    groupID: String,
    modifier: Modifier = Modifier,
    onSendMessageClick: () -> Unit = {},
    onGroupMemberClick: (GroupMember) -> Unit = {},
    onGroupDelete: () -> Unit = {},
    groupChatSettingViewModelFactory: GroupChatSettingViewModelFactory = GroupChatSettingViewModelFactory(
        groupID
    )
) {
    val colors = LocalTheme.current.colors
    val groupChatSettingViewModel = viewModel(
        GroupChatSettingViewModel::class,
        factory = groupChatSettingViewModelFactory
    )

    val isLoading by groupChatSettingViewModel.isLoading.collectAsState()
    val groupType by groupChatSettingViewModel.groupType.collectAsState()
    val groupName by groupChatSettingViewModel.groupName.collectAsState()
    val avatar by groupChatSettingViewModel.avatar.collectAsState()
    val notice by groupChatSettingViewModel.notice.collectAsState()
    val isNotDisturb by groupChatSettingViewModel.isNotDisturb.collectAsState()
    val isAllMuted by groupChatSettingViewModel.isAllMuted.collectAsState()
    val isPinned by groupChatSettingViewModel.isPinned.collectAsState()
    val groupOwner by groupChatSettingViewModel.groupOwner.collectAsState()
    val allMembers by groupChatSettingViewModel.allMembers.collectAsState()
    val currentUserRole by groupChatSettingViewModel.currentUserRole.collectAsState()
    val selfNameCard by groupChatSettingViewModel.selfNameCard.collectAsState()
    val memberCount by groupChatSettingViewModel.memberCount.collectAsState()
    val joinGroupApprovalType by groupChatSettingViewModel.joinGroupApprovalType.collectAsState()
    val inviteToGroupApprovalType by groupChatSettingViewModel.inviteToGroupApprovalType.collectAsState()

    CompositionLocalProvider(LocalGroupViewModel provides groupChatSettingViewModel) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(color = colors.bgColorOperate)
        ) {


            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.textColorSecondary,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                var memberListRefreshTrigger by remember { mutableStateOf(0) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        GroupInfoSection(
                            groupId = groupID,
                            groupName = groupName,
                            avatar = avatar,
                            groupType = groupType,
                            currentUserRole = currentUserRole
                        )
                    }

                    item {
                        GroupActionButtons(
                            onMessageClick = onSendMessageClick,
                        )
                    }

                    item {
                        MuteAndPinSettingsSection(
                            isNotDisturb = isNotDisturb,
                            isPinned = isPinned,
                            groupType = groupType,
                            currentUserRole = currentUserRole,
                            onDoNotDisturbToggle = { groupChatSettingViewModel.toggleGroupDoNotDisturb() },
                            onTopChatToggle = { groupChatSettingViewModel.toggleGroupTopChat() }
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {

                            GroupNoticeSection(
                                groupId = groupID,
                                notice = notice,
                                groupType = groupType,
                                currentUserRole = currentUserRole,
                            )
                            if (canPerformAction(
                                    groupType.value,
                                    currentUserRole.value,
                                    GroupPermission.SET_GROUP_MANAGEMENT
                                )
                            ) {
                                var showManageDialog by remember { mutableStateOf(false) }
                                InfoItem(
                                    title = stringResource(R.string.chat_setting_group_manage),
                                    hasArrow = true,
                                    onClick = { showManageDialog = true }
                                )

                                GroupManagementDialog(
                                    isVisible = showManageDialog,
                                    groupId = groupID,
                                    onDismiss = { showManageDialog = false }
                                )
                            }
                            JoinSettings(
                                groupId = groupID,
                                groupType = groupType,
                                joinGroupApprovalType = joinGroupApprovalType,
                                inviteToGroupApprovalType = inviteToGroupApprovalType,
                                currentUserRole = currentUserRole,
                            )
                        }
                    }

                    item {
                        var showAliasSelector by remember { mutableStateOf(false) }
                        InfoItem(
                            title = stringResource(R.string.chat_setting_my_alias_in_group),
                            value = selfNameCard ?: stringResource(R.string.chat_setting_not_set),
                            hasArrow = true,
                            onClick = { showAliasSelector = true }
                        )
                        TextInputBottomSheet(
                            isVisible = showAliasSelector,
                            title = stringResource(R.string.chat_setting_modify_group_name_card),
                            initialText = selfNameCard ?: "",
                            onDismiss = { showAliasSelector = false },
                            onConfirm = {
                                groupChatSettingViewModel.setGroupNickname(it)
                            })
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            var showMemberList by remember { mutableStateOf(false) }
                            InfoItem(
                                title = "${stringResource(R.string.chat_setting_group_members)}（${memberCount}）",
                                value = "",
                                hasArrow = true,
                                onClick = { showMemberList = true },
                            )

                            GroupMemberList(
                                isVisible = showMemberList,
                                title = stringResource(R.string.chat_setting_group_members),
                                isSelectionMode = false,
                                groupId = groupID,
                                currentUserRole = currentUserRole,
                                refreshKey = memberListRefreshTrigger,
                                onDismiss = {
                                    showMemberList = false
                                },
                                onMemberClick = onGroupMemberClick,
                                onSetAsAdmin = { member ->
                                    groupChatSettingViewModel.setGroupMemberRole(
                                        member.userID,
                                        GroupMemberRole.ADMIN
                                    )
                                },
                                onRemoveAdmin = { member ->
                                    groupChatSettingViewModel.setGroupMemberRole(
                                        member.userID,
                                        GroupMemberRole.MEMBER
                                    )
                                },
                                onRemoveMember = { member ->
                                    groupChatSettingViewModel.deleteGroupMember(listOf(member))
                                },
                                onViewInfo = { member ->
                                    onGroupMemberClick(member)
                                }
                            )

                            PreviewGroupMemberList(
                                groupType = groupType,
                                currentUserRole = currentUserRole,
                                inviteToGroupApprovalType = inviteToGroupApprovalType,
                                groupMembers = allMembers.take(3),
                                onMemberClick = {
                                    onGroupMemberClick(it)
                                }
                            )
                        }
                    }

                    item {
                        GroupDangerActions(
                            onDeleteAndQuitClick = {
                                groupChatSettingViewModel.quitGroup()
                                onGroupDelete()
                            },
                            onDismissGroupClick = {
                                groupChatSettingViewModel.dismissGroup()
                                onGroupDelete()
                            },
                            onTransferOwnerClick = { userID ->
                                groupChatSettingViewModel.transferGroupOwner(userID)
                            },

                            onClearHistoryClick = {
                                groupChatSettingViewModel.clearGroupChatHistoryConvenience()
                            },
                            onMemberListRefresh = { memberListRefreshTrigger++ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupInfoSection(
    groupId: String,
    groupName: String,
    avatar: String,
    groupType: GroupType,
    currentUserRole: GroupMemberRole
) {
    val colors = LocalTheme.current.colors
    val viewModel = LocalGroupViewModel.current
    var showAvatarSelector by remember { mutableStateOf(false) }
    var showGroupNameSelector by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(
            url = avatar,
            name = groupName,
            size = AvatarSize.XXL,
            onClick = {
                if (canPerformAction(
                        groupType.value,
                        currentUserRole.value,
                        GroupPermission.SET_GROUP_AVATAR
                    )
                ) {
                    showAvatarSelector = true
                }
            }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = groupName,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W600,
                    color = colors.textColorPrimary
                )
                if (canPerformAction(
                        groupType.value,
                        currentUserRole.value,
                        GroupPermission.SET_GROUP_NAME
                    )
                ) {
                    Icon(
                        modifier = Modifier
                            .size(15.dp)
                            .clickable {
                                showGroupNameSelector = true
                            },
                        painter = painterResource(R.drawable.chat_setting_group_name_edit_icon),
                        contentDescription = "",
                        tint = colors.textColorLink
                    )
                }

            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier,
                    text = "ID：",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorSecondary
                )
                SelectionContainer {

                    Text(
                        modifier = Modifier,
                        text = groupId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W400,
                        color = colors.textColorSecondary
                    )
                }
            }
        }
    }
    AvatarSelector(
        showAvatarSelector,
        onDismiss = { showAvatarSelector = false },
        imageUrls = getGroupAvatarUrls(),
        onImageSelected = { index, url ->
            viewModel.setGroupAvatar(url)
        })
    TextInputBottomSheet(
        isVisible = showGroupNameSelector,
        title = stringResource(R.string.chat_setting_modify_group_name),
        initialText = groupName,
        onDismiss = { showGroupNameSelector = false },
        onConfirm = {
            viewModel.setGroupName(it)
        })
}

@Composable
fun MuteAndPinSettingsSection(
    isNotDisturb: Boolean,
    isPinned: Boolean,
    groupType: GroupType,
    currentUserRole: GroupMemberRole,
    onDoNotDisturbToggle: () -> Unit,
    onTopChatToggle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        if (canPerformAction(
                groupType.value,
                currentUserRole.value,
                GroupPermission.SET_DO_NOT_DISTURB
            )
        ) {
            SettingItem(
                title = stringResource(R.string.chat_setting_do_not_disturb),
                isToggled = isNotDisturb,
                onToggle = onDoNotDisturbToggle
            )
        }
        if (canPerformAction(
                groupType.value,
                currentUserRole.value,
                GroupPermission.PIN_GROUP
            )
        ) {
            SettingItem(
                title = stringResource(R.string.chat_setting_pin),
                isToggled = isPinned,
                onToggle = onTopChatToggle
            )
        }
    }
}


@Composable
fun GroupDangerActions(
    onDeleteAndQuitClick: () -> Unit,
    onDismissGroupClick: () -> Unit,
    onTransferOwnerClick: (String) -> Unit,
    onClearHistoryClick: () -> Unit,
    onMemberListRefresh: () -> Unit = {}
) {
    val viewModel = LocalGroupViewModel.current
    val groupType by viewModel.groupType.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val groupID = viewModel.groupID
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {

        if (canPerformAction(
                groupType.value,
                currentUserRole.value,
                GroupPermission.CLEAR_HISTORY_MESSAGES
            )
        ) {
            DangerActionItem(
                title = stringResource(R.string.chat_setting_clear_history_messages),
                dangerHint = stringResource(R.string.chat_setting_clear_group_history_messages_tips),
                onClick = onClearHistoryClick
            )
        }
        if (canPerformAction(
                groupType.value,
                currentUserRole.value,
                GroupPermission.DELETE_AND_QUIT
            )
        ) {
            DangerActionItem(
                title = stringResource(R.string.chat_setting_delete_and_quit),
                dangerHint = stringResource(R.string.chat_setting_delete_and_quit_tips),
                onClick = onDeleteAndQuitClick
            )
        }
        if (canPerformAction(
                groupType.value,
                currentUserRole.value,
                GroupPermission.TRANSFER_OWNER
            )
        ) {
            var showTransferDialog by remember { mutableStateOf(false) }

            DangerActionItem(
                title = stringResource(R.string.chat_setting_transfer_group_owner),
                isAlert = false,
                onClick = { showTransferDialog = true }
            )
            GroupMemberList(
                isVisible = showTransferDialog,
                groupId = groupID,
                title = stringResource(R.string.chat_setting_transfer_group_owner),
                isSelectionMode = true,
                isSingleSelect = true,
                currentUserRole = currentUserRole,
                preSelectedMembers = listOf(
                    LoginStore.shared.loginState.loginUserInfo.value?.userID ?: ""
                ),
                onDismiss = { showTransferDialog = false },
                onConfirm = { list ->
                    if (list.isNotEmpty()) {
                        onTransferOwnerClick(list.first().userID)
                        coroutineScope.launch {
                            delay(1000)
                            onMemberListRefresh()
                        }
                    }
                    showTransferDialog = false
                })
        }
        if (canPerformAction(
                groupType.value,
                currentUserRole.value,
                GroupPermission.DISMISS_GROUP
            )
        ) {
            DangerActionItem(
                title = stringResource(R.string.chat_setting_dismiss_group),
                dangerHint = stringResource(R.string.chat_setting_dismiss_group_tips),
                onClick = onDismissGroupClick
            )
        }

    }
}

@Composable
fun GroupNoticeSection(
    groupId: String,
    notice: String,
    groupType: GroupType,
    currentUserRole: GroupMemberRole,
) {
    val colors = LocalTheme.current.colors
    var showSelector by remember { mutableStateOf(false) }
    val text =
        if (notice.isEmpty()) stringResource(R.string.chat_setting_no_group_notice) else notice
    val viewModel = LocalGroupViewModel.current
    val canPerform =
        canPerformAction(groupType.value, currentUserRole.value, GroupPermission.SET_GROUP_NOTICE)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (canPerform) {
                    showSelector = true
                }
            }
            .background(color = colors.bgColorTopBar)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.chat_setting_group_notice),
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            color = colors.textColorPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                color = colors.textColorSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (canPerform) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Arrow",
                    tint = colors.textColorSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    GroupNoticeFullScreenDialog(
        isVisible = showSelector,
        initialText = notice,
        onDismiss = { showSelector = false },
        onConfirm = {
            if (notice != it) {
                viewModel.setGroupNotice(it)
            }
        }
    )
}

@Composable
fun JoinSettings(
    groupId: String,
    groupType: GroupType,
    joinGroupApprovalType: GroupJoinOption,
    inviteToGroupApprovalType: GroupJoinOption,
    currentUserRole: GroupMemberRole,
) {
    val viewModel = LocalGroupViewModel.current
    val colors = LocalTheme.current.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        var showJoinTypeSelector by remember { mutableStateOf(false) }
        var showInviteTypeSelector by remember { mutableStateOf(false) }
        InfoItem(
            title = stringResource(R.string.chat_setting_group_type),
            value = getGroupTypeString(groupType),
        )

        InfoItem(
            title = stringResource(R.string.chat_setting_join_group_method),
            value = getAddGroupWayString(joinGroupApprovalType),
            hasArrow = canPerformAction(
                groupType.value,
                currentUserRole.value,
                GroupPermission.SET_JOIN_GROUP_APPROVAL_TYPE
            ),
            onClick = {
                if (canPerformAction(
                        groupType.value,
                        currentUserRole.value,
                        GroupPermission.SET_JOIN_GROUP_APPROVAL_TYPE
                    )
                ) {
                    showJoinTypeSelector = true
                }
            }
        )
        ActionSheet(
            isVisible = showJoinTypeSelector,
            options = listOf(
                ActionItem(
                    text = stringResource(R.string.chat_setting_join_method_forbid),
                    value = GroupJoinOption.FORBID
                ),
                ActionItem(
                    text = stringResource(R.string.chat_setting_method_auth),
                    value = GroupJoinOption.AUTH
                ),
                ActionItem(
                    text = stringResource(R.string.chat_setting_method_auto),
                    value = GroupJoinOption.ANY
                ),
            ),
            onDismiss = { showJoinTypeSelector = false },
            onActionSelected = {
                viewModel.setJoinGroupApproveType(it.value as GroupJoinOption)
            })

        InfoItem(
            title = stringResource(R.string.chat_setting_group_invited_method),
            value = getInviteGroupWayString(inviteToGroupApprovalType),
            hasArrow = canPerformAction(
                groupType.value,
                currentUserRole.value,
                GroupPermission.SET_INVITE_TO_GROUP_APPROVAL_TYPE
            ),
            onClick = {
                if (canPerformAction(
                        groupType.value,
                        currentUserRole.value,
                        GroupPermission.SET_INVITE_TO_GROUP_APPROVAL_TYPE
                    )
                ) {
                    showInviteTypeSelector = true
                }
            }
        )
        ActionSheet(
            isVisible = showInviteTypeSelector,
            options = listOf(
                ActionItem(
                    text = stringResource(R.string.chat_setting_invite_method_forbid),
                    value = GroupJoinOption.FORBID
                ),
                ActionItem(
                    text = stringResource(R.string.chat_setting_method_auth),
                    value = GroupJoinOption.AUTH
                ),
                ActionItem(
                    text = stringResource(R.string.chat_setting_method_auto),
                    value = GroupJoinOption.ANY
                ),
            ),
            onDismiss = { showInviteTypeSelector = false },
            onActionSelected = {
                viewModel.setInviteGroupApproveType(it.value as GroupJoinOption)
            })
    }
}


@Composable
fun PreviewGroupMemberList(
    groupType: GroupType,
    currentUserRole: GroupMemberRole,
    inviteToGroupApprovalType: GroupJoinOption,
    groupMembers: List<GroupMember>,
    onMemberClick: (GroupMember) -> Unit
) {
    val colors = LocalTheme.current.colors
    val viewModel = LocalGroupViewModel.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        if (canPerformAction(
                groupType.value,
                currentUserRole.value,
                GroupPermission.ADD_GROUP_MEMBER
            ) && inviteToGroupApprovalType != GroupJoinOption.FORBID
        ) {
            var showAddMemberDialog by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddMemberDialog = true }
                    .background(colors.bgColorTopBar)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "+ ${stringResource(R.string.chat_setting_add_group_members)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorLink,
                    modifier = Modifier.weight(1f)
                )
            }
            FriendPickerDialog(
                isVisible = showAddMemberDialog,
                title = stringResource(R.string.chat_setting_add_group_members),
                onDismiss = { showAddMemberDialog = false },
                onConfirm = { list ->
                    viewModel.addGroupMember(list.map { it.contactID })
                })
        }
        groupMembers.forEach { member ->
            GroupMemberItem(
                groupMember = member,
                onClick = { onMemberClick(member) }
            )
        }
    }
}

@Composable
fun GroupMemberItem(
    groupMember: GroupMember,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(colors.bgColorTopBar)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Avatar(
                url = groupMember.avatarURL,
                name = groupMember.displayName,
                size = AvatarSize.XS,
            )

            Text(
                text = groupMember.displayName,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W400,
                color = colors.textColorPrimary
            )
        }

        when (groupMember.role) {
            GroupMemberRole.ADMIN -> {
                Text(
                    text = stringResource(R.string.chat_setting_member_type_administrator),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorSecondary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Arrow",
                    tint = colors.textColorSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            else -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Arrow",
                    tint = colors.textColorSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun GroupActionButtons(
    onMessageClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ActionButton(
            title = stringResource(R.string.chat_setting_send_messages),
            iconResID = R.drawable.chat_setting_message_icon,
            onClick = onMessageClick,
            modifier = Modifier
        )
    }
}

val GroupMember.displayName
    get() = when {
        !nameCard.isNullOrEmpty() -> nameCard!!
        !nickname.isNullOrEmpty() -> nickname!!
        else -> userID
    }

@Composable
private fun getAddGroupWayString(joinType: GroupJoinOption): String {
    return when (joinType) {
        GroupJoinOption.FORBID -> stringResource(R.string.chat_setting_join_method_forbid)
        GroupJoinOption.AUTH -> stringResource(R.string.chat_setting_method_auth)
        GroupJoinOption.ANY -> stringResource(R.string.chat_setting_method_auto)
        else -> ""
    }
}

@Composable
private fun getInviteGroupWayString(inviteType: GroupJoinOption): String {
    return when (inviteType) {
        GroupJoinOption.FORBID -> stringResource(R.string.chat_setting_invite_method_forbid)
        GroupJoinOption.AUTH -> stringResource(R.string.chat_setting_method_auth)
        GroupJoinOption.ANY -> stringResource(R.string.chat_setting_method_auto)
        else -> ""
    }
}

@Composable
private fun getGroupTypeString(groupType: GroupType): String {
    return when (groupType) {
        GroupType.PUBLIC -> stringResource(R.string.chat_setting_group_type_public)
        GroupType.WORK -> stringResource(R.string.chat_setting_group_type_work)
        GroupType.MEETING -> stringResource(R.string.chat_setting_group_type_meeting)
        GroupType.AVCHATROOM -> stringResource(R.string.chat_setting_group_type_avchatroom)
        GroupType.COMMUNITY -> stringResource(R.string.chat_setting_group_type_community)
        else -> stringResource(R.string.chat_setting_other)
    }
}

private fun canPerformAction(
    groupType: String,
    currentUserRole: Int,
    permission: GroupPermission
): Boolean {
    return GroupPermissionManager.canPerformAction(groupType, currentUserRole, permission)
}