package io.trtc.tuikit.atomicx.chatsetting.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionItem
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionSheet
import io.trtc.tuikit.atomicxcore.api.GroupMember
import io.trtc.tuikit.atomicxcore.api.GroupMemberRole

@Composable
fun GroupMemberActionSheet(
    isVisible: Boolean,
    groupMember: GroupMember?,
    currentUserRole: GroupMemberRole,
    onDismiss: () -> Unit,
    onSetAsAdmin: (GroupMember) -> Unit = {},
    onRemoveAdmin: (GroupMember) -> Unit = {},
    onRemoveMember: (GroupMember) -> Unit = {},
    onViewInfo: (GroupMember) -> Unit = {}
) {
    if (!isVisible || groupMember == null) return

    val canSetAdmin = canPerformSetAdmin(currentUserRole.value, groupMember.role.value)
    val canRemoveAdmin = canPerformRemoveAdmin(currentUserRole.value, groupMember.role.value)
    val canRemoveMember = canPerformRemove(currentUserRole.value, groupMember.role.value)

    ActionSheet(
        isVisible = isVisible, options = mutableListOf(
            ActionItem(text = stringResource(R.string.chat_setting_member_info), value = "info")
        ).apply {
            if (canRemoveMember) {
                add(
                    ActionItem(
                        text = stringResource(R.string.chat_setting_delete_member),
                        value = "delete"
                    )
                )
            }
            if (canSetAdmin) {
                add(
                    ActionItem(
                        text = stringResource(R.string.chat_setting_set_admin),
                        value = "setAdmin"
                    )
                )
            }
            if (canRemoveAdmin) {
                add(
                    ActionItem(
                        text = stringResource(R.string.chat_setting_remove_admin),
                        value = "removeAdmin"
                    )
                )
            }
        }, onDismiss = onDismiss
    ) {
        when (it.value) {
            "info" -> onViewInfo(groupMember)
            "delete" -> onRemoveMember(groupMember)
            "setAdmin" -> onSetAsAdmin(groupMember)
            "removeAdmin" -> onRemoveAdmin(groupMember)
        }
    }
}

private fun canPerformSetAdmin(currentUserRole: Int, targetMemberRole: Int): Boolean {
    return currentUserRole == V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER &&
            targetMemberRole == V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER
}

private fun canPerformRemoveAdmin(currentUserRole: Int, targetMemberRole: Int): Boolean {
    return currentUserRole == V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER &&
            targetMemberRole == V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_ADMIN
}

private fun canPerformRemove(currentUserRole: Int, targetMemberRole: Int): Boolean {
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