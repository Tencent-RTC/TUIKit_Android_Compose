package io.trtc.tuikit.atomicx.contactlist.utils

import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.utils.ContextProvider
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo
import io.trtc.tuikit.atomicxcore.api.contact.FriendApplicationInfo
import io.trtc.tuikit.atomicxcore.api.contact.GroupApplicationHandledResult
import io.trtc.tuikit.atomicxcore.api.contact.GroupApplicationHandledStatus
import io.trtc.tuikit.atomicxcore.api.contact.GroupApplicationInfo
import io.trtc.tuikit.atomicxcore.api.contact.GroupApplicationType
import io.trtc.tuikit.atomicxcore.api.login.UserProfile

val ContactInfo.displayName
    get() = title ?: contactID

val FriendApplicationInfo.displayName
    get() = title ?: applicationID

val UserProfile.displayName
    get() = nickname?.takeIf { it.isNotEmpty() }
        ?: userID

val GroupApplicationInfo.fromUserDisplayName
    get() = when {
        !fromUserNickname.isNullOrEmpty() -> fromUserNickname
        else -> fromUser
    } ?: ""

val GroupApplicationInfo.toUserDisplayName
    get() = toUser ?: ""

val GroupApplicationInfo.groupDisplayName
    get() = groupID

val GroupApplicationInfo.isJoinRequest
    get() = type == GroupApplicationType.JOIN_APPROVED_BY_ADMIN

val GroupApplicationInfo.isInviteRequest
    get() = type == GroupApplicationType.INVITE_APPROVED_BY_INVITEE ||
            type == GroupApplicationType.INVITE_APPROVED_BY_ADMIN

val GroupApplicationInfo.applicationTypeText
    get() = when {
        isJoinRequest -> ContextProvider.appContext.getString(R.string.contact_list_apply_to_join_group)
        isInviteRequest -> ContextProvider.appContext.getString(R.string.contact_list_invite_to_join_group)
        else -> ContextProvider.appContext.getString(R.string.contact_list_unknown)
    }

val GroupApplicationInfo.statusText
    get() = when (handledStatus) {
        GroupApplicationHandledStatus.UNHANDLED -> ContextProvider.appContext.getString(R.string.contact_list_handle_status_unhandle)
        GroupApplicationHandledStatus.BY_OTHER -> ContextProvider.appContext.getString(R.string.contact_list_handle_status_handled_by_ohter)
        GroupApplicationHandledStatus.BY_MYSELF -> when (handledResult) {
            GroupApplicationHandledResult.REFUSED -> ContextProvider.appContext.getString(R.string.contact_list_refused)
            GroupApplicationHandledResult.AGREED -> ContextProvider.appContext.getString(R.string.contact_list_agreed)
            else -> ContextProvider.appContext.getString(R.string.contact_list_unknown)
        }

        else -> ContextProvider.appContext.getString(R.string.contact_list_unknown)
    }

val GroupApplicationInfo.canHandle
    get() = handledStatus == GroupApplicationHandledStatus.UNHANDLED
