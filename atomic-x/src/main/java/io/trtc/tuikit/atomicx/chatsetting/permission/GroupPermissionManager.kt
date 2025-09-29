package io.trtc.tuikit.atomicx.chatsetting.permission

import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo
import com.tencent.imsdk.v2.V2TIMManager


enum class GroupPermission {
    SET_GROUP_NAME,
    SET_GROUP_AVATAR,
    SEND_MESSAGE,
    SET_DO_NOT_DISTURB,
    PIN_GROUP,
    SET_GROUP_NOTICE,
    SET_GROUP_MANAGEMENT,
    GET_GROUP_TYPE,
    SET_JOIN_GROUP_APPROVAL_TYPE,
    SET_INVITE_TO_GROUP_APPROVAL_TYPE,
    SET_GROUP_REMARK,
    SET_BACKGROUND,
    GET_GROUP_MEMBER_LIST,
    SET_GROUP_MEMBER_ROLE,
    GET_GROUP_MEMBER_INFO,
    REMOVE_GROUP_MEMBER,
    ADD_GROUP_MEMBER,
    CLEAR_HISTORY_MESSAGES,
    DELETE_AND_QUIT,
    TRANSFER_OWNER,
    DISMISS_GROUP,
    REPORT_GROUP
}


object GroupPermissionManager {


    private val permissionMatrix: Map<String, Map<Int, Map<GroupPermission, Boolean>>> =
        buildPermissionMatrix()


    private fun buildPermissionMatrix(): Map<String, Map<Int, Map<GroupPermission, Boolean>>> {
        return mapOf(

            V2TIMManager.GROUP_TYPE_WORK to mapOf(
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER to createPermissionMap(
                    setGroupName = true,
                    setGroupAvatar = true,
                    sendMessage = true,
                    setDoNotDisturb = true,
                    pinGroup = true,
                    setGroupNotice = true,
                    setGroupManagement = true,
                    getGroupType = true,
                    setJoinGroupApprovalType = true,
                    setInviteToGroupApprovalType = true,
                    setGroupRemark = true,
                    setBackground = true,
                    getGroupMemberList = true,
                    setGroupMemberRole = false,
                    getGroupMemberInfo = true,
                    removeGroupMember = true, addGroupMember = true,
                    clearHistoryMessages = true,
                    deleteAndQuit = true,
                    transferOwner = true,
                    dismissGroup = false,
                    reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_ADMIN to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = false, setGroupManagement = false, getGroupType = true,
                    setJoinGroupApprovalType = true, setInviteToGroupApprovalType = false, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = false,
                    getGroupMemberInfo = true, removeGroupMember = false, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = false, setGroupManagement = false, getGroupType = true,
                    setJoinGroupApprovalType = false, setInviteToGroupApprovalType = false, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = false,
                    getGroupMemberInfo = true, removeGroupMember = false, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                )
            ),

            V2TIMManager.GROUP_TYPE_PUBLIC to mapOf(
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER to createPermissionMap(
                    setGroupName = true, setGroupAvatar = true, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = true, setGroupManagement = true, getGroupType = true,
                    setJoinGroupApprovalType = true, setInviteToGroupApprovalType = true, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = true,
                    getGroupMemberInfo = true, removeGroupMember = true, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = false, transferOwner = true,
                    dismissGroup = true, reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_ADMIN to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = true, setGroupManagement = true, getGroupType = true,
                    setJoinGroupApprovalType = true, setInviteToGroupApprovalType = true, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = false,
                    getGroupMemberInfo = true, removeGroupMember = true, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = false, setGroupManagement = false, getGroupType = true,
                    setJoinGroupApprovalType = false, setInviteToGroupApprovalType = false, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = false,
                    getGroupMemberInfo = true, removeGroupMember = false, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                )
            ),

            V2TIMManager.GROUP_TYPE_MEETING to mapOf(
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER to createPermissionMap(
                    setGroupName = true, setGroupAvatar = true, sendMessage = true, setDoNotDisturb = false,
                    pinGroup = true, setGroupNotice = true, setGroupManagement = true, getGroupType = true,
                    setJoinGroupApprovalType = false, setInviteToGroupApprovalType = true, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = true,
                    getGroupMemberInfo = true, removeGroupMember = true, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = false, transferOwner = true,
                    dismissGroup = true, reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_ADMIN to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = false,
                    pinGroup = true, setGroupNotice = true, setGroupManagement = true, getGroupType = true,
                    setJoinGroupApprovalType = false, setInviteToGroupApprovalType = true, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = false,
                    getGroupMemberInfo = true, removeGroupMember = true, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = false,
                    pinGroup = true, setGroupNotice = false, setGroupManagement = false, getGroupType = true,
                    setJoinGroupApprovalType = false, setInviteToGroupApprovalType = false, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = false,
                    getGroupMemberInfo = true, removeGroupMember = false, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                )
            ),

            V2TIMManager.GROUP_TYPE_COMMUNITY to mapOf(
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER to createPermissionMap(
                    setGroupName = true, setGroupAvatar = true, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = true, setGroupManagement = true, getGroupType = true,
                    setJoinGroupApprovalType = true, setInviteToGroupApprovalType = true, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = true,
                    getGroupMemberInfo = true, removeGroupMember = true, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = false, transferOwner = true,
                    dismissGroup = true, reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_ADMIN to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = true, setGroupManagement = true, getGroupType = true,
                    setJoinGroupApprovalType = true, setInviteToGroupApprovalType = true, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = false,
                    getGroupMemberInfo = true, removeGroupMember = true, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = false, setGroupManagement = false, getGroupType = true,
                    setJoinGroupApprovalType = false, setInviteToGroupApprovalType = false, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = true, setGroupMemberRole = false,
                    getGroupMemberInfo = true, removeGroupMember = false, addGroupMember = true,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                )
            ),

            V2TIMManager.GROUP_TYPE_AVCHATROOM to mapOf(
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER to createPermissionMap(
                    setGroupName = true, setGroupAvatar = true, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = true, setGroupManagement = true, getGroupType = true,
                    setJoinGroupApprovalType = false, setInviteToGroupApprovalType = false, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = false, setGroupMemberRole = false,
                    getGroupMemberInfo = false, removeGroupMember = false, addGroupMember = false,
                    clearHistoryMessages = true, deleteAndQuit = false, transferOwner = false,
                    dismissGroup = true, reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_ADMIN to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = false, setGroupManagement = false, getGroupType = true,
                    setJoinGroupApprovalType = false, setInviteToGroupApprovalType = false, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = false, setGroupMemberRole = false,
                    getGroupMemberInfo = false, removeGroupMember = false, addGroupMember = false,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                ),
                V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER to createPermissionMap(
                    setGroupName = false, setGroupAvatar = false, sendMessage = true, setDoNotDisturb = true,
                    pinGroup = true, setGroupNotice = false, setGroupManagement = false, getGroupType = true,
                    setJoinGroupApprovalType = false, setInviteToGroupApprovalType = false, setGroupRemark = true,
                    setBackground = true, getGroupMemberList = false, setGroupMemberRole = false,
                    getGroupMemberInfo = false, removeGroupMember = false, addGroupMember = false,
                    clearHistoryMessages = true, deleteAndQuit = true, transferOwner = false,
                    dismissGroup = false, reportGroup = true
                )
            )
        )
    }


    private fun createPermissionMap(
        setGroupName: Boolean,
        setGroupAvatar: Boolean,
        sendMessage: Boolean,
        setDoNotDisturb: Boolean,
        pinGroup: Boolean,
        setGroupNotice: Boolean,
        setGroupManagement: Boolean,
        getGroupType: Boolean,
        setJoinGroupApprovalType: Boolean,
        setInviteToGroupApprovalType: Boolean,
        setGroupRemark: Boolean,
        setBackground: Boolean,
        getGroupMemberList: Boolean,
        setGroupMemberRole: Boolean,
        getGroupMemberInfo: Boolean,
        removeGroupMember: Boolean,
        addGroupMember: Boolean,
        clearHistoryMessages: Boolean,
        deleteAndQuit: Boolean,
        transferOwner: Boolean,
        dismissGroup: Boolean,
        reportGroup: Boolean
    ): Map<GroupPermission, Boolean> {
        return mapOf(
            GroupPermission.SET_GROUP_NAME to setGroupName,
            GroupPermission.SET_GROUP_AVATAR to setGroupAvatar,
            GroupPermission.SEND_MESSAGE to sendMessage,
            GroupPermission.SET_DO_NOT_DISTURB to setDoNotDisturb,
            GroupPermission.PIN_GROUP to pinGroup,
            GroupPermission.SET_GROUP_NOTICE to setGroupNotice,
            GroupPermission.SET_GROUP_MANAGEMENT to setGroupManagement,
            GroupPermission.GET_GROUP_TYPE to getGroupType,
            GroupPermission.SET_JOIN_GROUP_APPROVAL_TYPE to setJoinGroupApprovalType,
            GroupPermission.SET_INVITE_TO_GROUP_APPROVAL_TYPE to setInviteToGroupApprovalType,
            GroupPermission.SET_GROUP_REMARK to setGroupRemark,
            GroupPermission.SET_BACKGROUND to setBackground,
            GroupPermission.GET_GROUP_MEMBER_LIST to getGroupMemberList,
            GroupPermission.SET_GROUP_MEMBER_ROLE to setGroupMemberRole,
            GroupPermission.GET_GROUP_MEMBER_INFO to getGroupMemberInfo,
            GroupPermission.REMOVE_GROUP_MEMBER to removeGroupMember,
            GroupPermission.ADD_GROUP_MEMBER to addGroupMember,
            GroupPermission.CLEAR_HISTORY_MESSAGES to clearHistoryMessages,
            GroupPermission.DELETE_AND_QUIT to deleteAndQuit,
            GroupPermission.TRANSFER_OWNER to transferOwner,
            GroupPermission.DISMISS_GROUP to dismissGroup,
            GroupPermission.REPORT_GROUP to reportGroup
        )
    }


    fun hasPermission(
        groupType: String,
        memberRole: Int,
        permission: GroupPermission
    ): Boolean {
        return permissionMatrix[groupType]?.get(memberRole)?.get(permission) == true
    }


    fun getAvailablePermissions(
        groupType: String,
        memberRole: Int
    ): List<GroupPermission> {
        val rolePermissions = permissionMatrix[groupType]?.get(memberRole) ?: return emptyList()

        return rolePermissions.mapNotNull { (permission, isAllowed) ->
            if (isAllowed) permission else null
        }
    }


    fun canPerformAction(
        groupType: String,
        memberRole: Int,
        action: GroupPermission
    ): Boolean {
        return hasPermission(groupType, memberRole, action)
    }
}
