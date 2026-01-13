package io.trtc.tuikit.atomicx.messageinput.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messageinput.model.MentionInfo
import io.trtc.tuikit.atomicx.userpicker.SelectionCheckBox
import io.trtc.tuikit.atomicx.userpicker.UserPicker
import io.trtc.tuikit.atomicx.userpicker.UserPickerData
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.contact.GroupMember
import io.trtc.tuikit.atomicxcore.api.contact.GroupMemberRole
import io.trtc.tuikit.atomicxcore.api.contact.GroupSettingStore

fun GroupMember.getDisplayName(): String {
    return nameCard?.takeIf { it.isNotBlank() }
        ?: nickname?.takeIf { it.isNotBlank() }
        ?: userID
}

fun GroupMember.toUserPickerData(): UserPickerData<GroupMember> {
    return UserPickerData(
        key = userID,
        label = getDisplayName(),
        avatarUrl = avatarURL,
        extraData = this
    )
}

@Composable
fun MentionMemberDialog(
    isVisible: Boolean,
    groupID: String,
    onDismiss: () -> Unit,
    onConfirm: (List<MentionInfo>) -> Unit
) {
    if (!isVisible) return

    FullScreenDialog(onDismissRequest = onDismiss) {
        MentionMemberContent(
            groupID = groupID,
            onBackClick = onDismiss,
            onConfirm = { mentionInfoList ->
                onConfirm(mentionInfoList)
                onDismiss()
            }
        )
    }
}

@Composable
private fun MentionMemberContent(
    groupID: String,
    onBackClick: () -> Unit,
    onConfirm: (List<MentionInfo>) -> Unit
) {
    val colors = LocalTheme.current.colors
    val groupSettingStore = remember(groupID) { GroupSettingStore.create(groupID) }
    val groupMembers = remember { mutableStateListOf<GroupMember>() }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasLoadedInitial by remember { mutableStateOf(false) }
    var atAll by remember { mutableStateOf(false) }
    val atAllDisplayName = stringResource(R.string.message_input_mention_all)
    val selectedMembers = remember { mutableListOf<GroupMember>() }

    LaunchedEffect(groupID) {
        if (!hasLoadedInitial) {
            groupSettingStore.fetchGroupMemberList(
                role = GroupMemberRole.ALL,
                completion = object : CompletionHandler {
                    override fun onSuccess() {
                        groupMembers.clear()
                        groupMembers.addAll(groupSettingStore.groupSettingState.allMembers.value)
                        hasLoadedInitial = true
                    }

                    override fun onFailure(code: Int, desc: String) {
                        hasLoadedInitial = true
                    }
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.bgColorOperate)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
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
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = stringResource(R.string.message_input_back),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorLink
                )
            }

            Text(
                text = stringResource(R.string.message_input_mention_select_member),
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                color = colors.textColorPrimary
            )

            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val mentionInfos = buildList {
                            if (atAll) {
                                add(
                                    MentionInfo(
                                        userID = MentionInfo.AT_ALL_USER_ID,
                                        displayName = atAllDisplayName,
                                    )
                                )
                            }
                            addAll(selectedMembers.map {
                                MentionInfo(
                                    userID = it.userID,
                                    displayName = it.getDisplayName(),
                                )
                            })
                        }
                        onConfirm(mentionInfos)
                    },
                text = stringResource(R.string.base_component_confirm),
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                color = colors.textColorLink
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.strokeColorPrimary
        )

        AtAllItem(
            isSelected = atAll,
            onClick = {
                atAll = !atAll
            }
        )

        val dataSource = remember(groupMembers.toList()) {
            groupMembers.map { it.toUserPickerData() }
        }

        UserPicker(
            modifier = Modifier.fillMaxSize(),
            dataSource = dataSource,
            onSelectedChanged = { selected ->
                selectedMembers.clear()
                selectedMembers.addAll(selected.map { it.extraData })
            },
            onReachEnd = {
                if (!isLoadingMore) {
                    isLoadingMore = true
                    groupSettingStore.fetchMoreGroupMemberList(object : CompletionHandler {
                        override fun onSuccess() {
                            groupMembers.clear()
                            groupMembers.addAll(groupSettingStore.groupSettingState.allMembers.value)
                            isLoadingMore = false
                        }

                        override fun onFailure(code: Int, desc: String) {
                            isLoadingMore = false
                        }
                    })
                }
            }
        )
    }
}

@Composable
private fun AtAllItem(isSelected: Boolean = false, onClick: () -> Unit) {
    val colors = LocalTheme.current.colors
    val atAllText = stringResource(R.string.message_input_mention_all)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .background(color = colors.bgColorOperate)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Avatar(url = null, name = atAllText)
            Spacer(modifier = Modifier.width(13.dp))
            Text(
                text = "@$atAllText",
                color = colors.textColorPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400
            )
        }

        SelectionCheckBox(
            checked = isSelected,
            onCheckedChange = { _ ->
                onClick()
            }
        )
        Spacer(modifier = Modifier.width(26.dp))
    }
}
