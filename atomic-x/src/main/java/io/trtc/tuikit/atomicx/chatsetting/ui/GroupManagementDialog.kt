package io.trtc.tuikit.atomicx.chatsetting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Switch
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicxcore.api.GroupMember


@Composable
fun GroupManagementDialog(
    isVisible: Boolean,
    groupId: String,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        FullScreenDialog(
            onDismissRequest = onDismiss,
        ) {
            GroupManagementFullscreenContent(
                groupId = groupId,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun GroupManagementFullscreenContent(
    groupId: String,
    onDismiss: () -> Unit
) {
    val colors = LocalTheme.current.colors
    val groupViewModel = LocalGroupViewModel.current

    val isAllMuted by groupViewModel.isAllMuted.collectAsState()
    val currentUserRole by groupViewModel.currentUserRole.collectAsState()
    val silencedMembers by groupViewModel.silencedMembers.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(colors.bgColorInput)
        ) {
            GroupSilenceManagementTopBar(
                title = stringResource(R.string.chat_setting_group_management),
                onBackClick = onDismiss,
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    TotalSilenceSection(
                        isEnabled = isAllMuted,
                        onToggle = { shouldMute ->
                            groupViewModel.toggleGroupAllMute()
                        }
                    )
                }

                item {
                    SilenceExplanationText()
                }

                item {
                    var showMemberSelector by remember { mutableStateOf(false) }
                    AddSilenceMembersButton(
                        onClick = { showMemberSelector = true }
                    )

                    GroupMemberList(
                        isVisible = showMemberSelector,
                        groupId = groupId,
                        title = stringResource(R.string.chat_setting_select_muted_member),
                        isSelectionMode = true,
                        currentUserRole = currentUserRole,
                        preSelectedMembers = silencedMembers.map { it.userID },
                        onDismiss = { showMemberSelector = false },
                        onConfirm = { selectedMembers ->
                            selectedMembers.forEach { member ->
                                groupViewModel.muteGroupMember(member.userID, (7 * 24 * 60 * 60))
                            }
                            showMemberSelector = false
                        }
                    )
                }


                if (silencedMembers.isNotEmpty()) {
                    items(silencedMembers, key = { it.userID }) { member ->
                        SilencedMemberItem(
                            groupMember = member,
                            onRemoveSilence = {
                                groupViewModel.muteGroupMember(member.userID, 0)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun GroupSilenceManagementTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    val colors = LocalTheme.current.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.bgColorOperate)
            .padding(horizontal = 4.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.chat_setting_back),
                tint = colors.textColorPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textColorPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun TotalSilenceSection(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.bgColorOperate)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.chat_setting_mute_all),
            fontSize = 16.sp,
            color = colors.textColorPrimary,
            modifier = Modifier.weight(1f)
        )

        Switch(isEnabled, onCheckedChange = {
            onToggle(it)
        })

    }
}

@Composable
private fun SilencedMemberItem(
    groupMember: GroupMember,
    onRemoveSilence: () -> Unit
) {
    val colors = LocalTheme.current.colors
    val displayName = when {
        !groupMember.nameCard.isNullOrEmpty() -> groupMember.nameCard!!
        !groupMember.nickname.isNullOrEmpty() -> groupMember.nickname!!
        else -> groupMember.userID
    }

    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { },
                    onLongClick = {
                        showDropdownMenu = true
                    }
                )
                .background(colors.bgColorOperate)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Avatar(
                url = groupMember.avatarURL ?: "",
                name = displayName,
            )

            Text(
                text = displayName,
                fontSize = 14.sp,
                color = colors.textColorPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.chat_setting_cancel_mute),
                        fontSize = 14.sp,
                        color = colors.textColorLink
                    )
                },
                onClick = {
                    showDropdownMenu = false
                    onRemoveSilence()
                }
            )
        }
    }
}

@Composable
private fun AddSilenceMembersButton(
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgColorOperate)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = colors.textColorLink,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = stringResource(R.string.chat_setting_select_muted_member),
            fontSize = 12.sp,
            color = colors.textColorLink
        )
    }
}

@Composable
private fun SilenceExplanationText() {
    val colors = LocalTheme.current.colors
    Text(
        text = stringResource(R.string.chat_setting_mute_all_tips),
        fontSize = 12.sp,
        color = colors.textColorPrimary,
        lineHeight = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    )
}