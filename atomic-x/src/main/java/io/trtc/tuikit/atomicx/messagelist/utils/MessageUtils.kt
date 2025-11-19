package io.trtc.tuikit.atomicx.messagelist.utils

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.imsdk.v2.V2TIMMessage
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.utils.ContextProvider
import io.trtc.tuikit.atomicxcore.api.contact.GroupJoinOption
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.message.MessageType
import io.trtc.tuikit.atomicxcore.api.message.SystemMessageInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object MessageUtils {

    fun getSystemInfoDisplayString(systemMessages: List<SystemMessageInfo>?): String {
        val context = ContextProvider.appContext

        if (systemMessages.isNullOrEmpty()) return ""
        val parts = systemMessages.mapNotNull { info ->
            when (info) {
                is SystemMessageInfo.RecallMessage -> getRecallDisplayString(info)
                else -> getGroupTipsDisplayString(info)
            }.takeIf { it.isNotEmpty() }
        }
        return if (parts.isEmpty()) context.getString(R.string.message_list_unknown) else parts.joinToString("")
    }

    fun getRecallDisplayString(systemInfo: SystemMessageInfo.RecallMessage): String {
        val context = ContextProvider.appContext

        val reason = systemInfo.recallReason ?: ""
        var content = if (systemInfo.isRecalledBySelf == true) {
            context.getString(R.string.message_list_message_tips_you_recall_message)
        } else {
            if (systemInfo.isInGroup == true) {
                val operator = systemInfo.recallMessageOperator ?: ""
                context.getString(R.string.message_list_message_tips_recall_message_format, operator)
            } else {
                context.getString(R.string.message_list_message_tips_others_recall_message)
            }
        }
        if (reason.isNotEmpty()) {
            content = "$content: $reason"
        }
        return content
    }

    fun getGroupTipsDisplayString(systemInfo: SystemMessageInfo): String {
        val context = ContextProvider.appContext
        return when (systemInfo) {
            is SystemMessageInfo.Unknown -> {
                context.getString(R.string.message_list_unknown)
            }

            is SystemMessageInfo.JoinGroup -> {
                context.getString(R.string.message_list_message_tips_join_group_format, systemInfo.joinMember ?: "")
            }

            is SystemMessageInfo.InviteToGroup -> {
                context.getString(
                    R.string.message_list_message_tips_invite_join_group_format,
                    systemInfo.inviter ?: "",
                    systemInfo.inviteesShowName ?: ""
                )
            }

            is SystemMessageInfo.QuitGroup -> {
                context.getString(
                    R.string.message_list_message_tips_leave_group_format,
                    systemInfo.quitMember ?: ""
                )
            }

            is SystemMessageInfo.KickedFromGroup -> {
                context.getString(
                    R.string.message_list_message_tips_kickoff_group_format,
                    systemInfo.kickOperator ?: "",
                    systemInfo.kickedMembersShowName ?: ""
                )
            }

            is SystemMessageInfo.SetGroupAdmin -> {
                context.getString(
                    R.string.message_list_message_tips_set_admin_format,
                    systemInfo.setAdminMembersShowName ?: ""
                )
            }

            is SystemMessageInfo.CancelGroupAdmin -> {
                context.getString(
                    R.string.message_list_message_tips_cancel_admin_format,
                    systemInfo.cancelAdminMembersShowName ?: ""
                )
            }

            is SystemMessageInfo.MuteGroupMember -> {
                val muteTime = systemInfo.muteTime ?: 0
                val memberShowName = systemInfo.mutedGroupMembersShowName ?: ""
                val isSelfMuted = systemInfo.isSelfMuted ?: false
                val actualShowName = if (isSelfMuted) context.getString(R.string.message_list_you) else memberShowName
                if (muteTime == 0L) {
                    "$actualShowName ${context.getString(R.string.message_list_message_tips_unmute)}"
                } else {
                    val duration = formatMuteTime(muteTime)
                    "$actualShowName ${context.getString(R.string.message_list_message_tips_mute)} $duration"
                }
            }

            is SystemMessageInfo.PinGroupMessage -> {
                context.getString(
                    R.string.message_list_message_tips_group_pin_message,
                    systemInfo.pinGroupMessageOperator ?: ""
                )
            }

            is SystemMessageInfo.UnpinGroupMessage -> {
                context.getString(
                    R.string.message_list_message_tips_group_unpin_message,
                    systemInfo.unpinGroupMessageOperator ?: ""
                )
            }

            is SystemMessageInfo.ChangeGroupName -> {
                context.getString(
                    R.string.message_list_message_tips_edit_group_name_format,
                    systemInfo.groupNameOperator ?: "",
                    systemInfo.groupName ?: ""
                )
            }

            is SystemMessageInfo.ChangeGroupIntroduction -> {
                context.getString(
                    R.string.message_list_message_tips_edit_group_intro_format,
                    systemInfo.groupIntroductionOperator ?: "",
                    systemInfo.groupIntroduction ?: ""
                )
            }

            is SystemMessageInfo.ChangeGroupNotification -> {
                context.getString(
                    R.string.message_list_message_tips_edit_group_announce_format,
                    systemInfo.groupNotificationOperator ?: "",
                    systemInfo.groupNotification ?: ""
                )
            }

            is SystemMessageInfo.ChangeGroupAvatar -> {
                context.getString(
                    R.string.message_list_message_tips_edit_group_avatar_format,
                    systemInfo.groupAvatarOperator ?: ""
                )
            }

            is SystemMessageInfo.ChangeGroupOwner -> {
                context.getString(
                    R.string.message_list_message_tips_edit_group_owner_format,
                    systemInfo.groupOwnerOperator ?: "",
                    systemInfo.groupOwner ?: ""
                )
            }

            is SystemMessageInfo.ChangeGroupMuteAll -> {
                context.getString(
                    if (systemInfo.isMuteAll == true) R.string.message_list_set_mute_all_format else R.string.message_list_unmute_all_format,
                    systemInfo.groupMuteAllOperator ?: ""
                )
            }

            is SystemMessageInfo.ChangeJoinGroupApproval -> {
                val approvalDesc = when (systemInfo.groupJoinOption) {
                    GroupJoinOption.FORBID -> context.getString(R.string.message_list_group_profile_join_disable)
                    GroupJoinOption.AUTH -> context.getString(R.string.message_list_group_profile_admin_approve)
                    GroupJoinOption.ANY -> context.getString(R.string.message_list_group_profile_auto_approval)
                    else -> context.getString(R.string.message_list_unknown)
                }
                context.getString(
                    R.string.message_list_message_tips_edit_group_add_opt_format,
                    systemInfo.groupJoinApprovalOperator ?: "",
                    approvalDesc
                )
            }

            is SystemMessageInfo.ChangeInviteToGroupApproval -> {
                val approvalDesc = when (systemInfo.groupInviteOption) {
                    GroupJoinOption.FORBID -> context.getString(R.string.message_list_group_profile_invite_disable)
                    GroupJoinOption.AUTH -> context.getString(R.string.message_list_group_profile_admin_approve)
                    GroupJoinOption.ANY -> context.getString(R.string.message_list_group_profile_auto_approval)
                    else -> context.getString(R.string.message_list_unknown)
                }
                context.getString(
                    R.string.message_list_message_tips_edit_group_invite_opt_format,
                    systemInfo.groupInviteApprovalOperator ?: "",
                    approvalDesc
                )
            }

            is SystemMessageInfo.RecallMessage -> "" // handled in getRecallDisplayString
        }
    }

    fun getMessageAbstract(messageInfo: MessageInfo?): String {
        val context = ContextProvider.appContext
        if (messageInfo == null) return ""
        return when (messageInfo.messageType) {
            MessageType.TEXT -> messageInfo.messageBody?.text ?: ""
            MessageType.IMAGE -> context.getString(R.string.message_list_message_type_image)
            MessageType.SOUND -> context.getString(R.string.message_list_message_type_voice)
            MessageType.FILE -> context.getString(R.string.message_list_message_type_file)
            MessageType.VIDEO -> context.getString(R.string.message_list_message_type_video)
            MessageType.FACE -> context.getString(R.string.message_list_message_type_animate_emoji)
            MessageType.CUSTOM -> {
                val customInfo = jsonData2Dictionary(messageInfo.messageBody?.customMessage?.data)
                if (customInfo != null && customInfo["businessID"] == "group_create") {
                    val sender = customInfo["opUser"] as? String ?: ""
                    val cmd = (customInfo["cmd"] as? Double) ?: 0.0
                    if (cmd == 1.0) {
                        "$sender ${context.getString(R.string.message_list_community_create_tips_message)}"
                    } else {
                        "$sender ${context.getString(R.string.message_list_group_create_tips_message)}"
                    }
                } else {
                    context.getString(R.string.message_list_message_tips_unsupport_custom_message)
                }
            }

            MessageType.SYSTEM -> getSystemInfoDisplayString(messageInfo.messageBody?.systemMessage)
            else -> ""
        }
    }

    fun formatMuteTime(seconds: Long): String {
        val context = ContextProvider.appContext
        if (seconds <= 0) return ""
        var timeStr =
            context.resources.getQuantityString(R.plurals.message_list_second, seconds.toInt(), seconds.toInt())
        if (seconds > 60) {
            val second = seconds % 60
            var min = seconds / 60
            timeStr = context.getString(R.string.message_list_min_second, min, second)
            if (min > 60) {
                min = (seconds / 60) % 60
                val hour = (seconds / 60) / 60
                timeStr = context.getString(R.string.message_list_hour_min_second, hour, min, second)
                if (hour % 24 == 0L) {
                    val day = ((seconds / 60) / 60) / 24
                    timeStr = context.resources.getQuantityString(R.plurals.message_list_day, day.toInt(), day.toInt())
                } else if (hour > 24) {
                    val newHour = ((seconds / 60) / 60) % 24
                    val day = ((seconds / 60) / 60) / 24
                    timeStr = context.getString(R.string.message_list_day_hour_min_second, day, newHour, min, second)
                }
            }
        }
        return timeStr
    }

    fun convertDateToYMDStr(timeStamp: Long?): String {
        if (timeStamp == null || timeStamp <= 0) {
            return ""
        }
        val date = Date(timeStamp)
        if (date == Date(Long.MIN_VALUE)) {
            return ""
        }

        val dateFmt = SimpleDateFormat().apply {
            timeZone = TimeZone.getDefault()
        }

        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SUNDAY
        }

        val now = Date()
        val nowComponent = calendar.run {
            time = now
            mapOf(
                Calendar.DAY_OF_MONTH to get(Calendar.DAY_OF_MONTH),
                Calendar.MONTH to get(Calendar.MONTH),
                Calendar.YEAR to get(Calendar.YEAR),
                Calendar.WEEK_OF_MONTH to get(Calendar.WEEK_OF_MONTH)
            )
        }

        val dateComponent = calendar.run {
            time = date
            mapOf(
                Calendar.DAY_OF_MONTH to get(Calendar.DAY_OF_MONTH),
                Calendar.MONTH to get(Calendar.MONTH),
                Calendar.YEAR to get(Calendar.YEAR),
                Calendar.WEEK_OF_MONTH to get(Calendar.WEEK_OF_MONTH)
            )
        }

        return when {
            nowComponent[Calendar.YEAR] != dateComponent[Calendar.YEAR] -> {
                dateFmt.apply { applyPattern("yyyy/MM/dd") }.format(date)
            }

            nowComponent[Calendar.MONTH] != dateComponent[Calendar.MONTH] -> {
                dateFmt.apply { applyPattern("MM/dd") }.format(date)
            }

            nowComponent[Calendar.WEEK_OF_MONTH] != dateComponent[Calendar.WEEK_OF_MONTH] -> {
                dateFmt.apply { applyPattern("MM/dd") }.format(date)
            }

            nowComponent[Calendar.DAY_OF_MONTH] != dateComponent[Calendar.DAY_OF_MONTH] -> {
                dateFmt.apply {
                    applyPattern("EEEE")
                }.format(date)
            }

            else -> {
                dateFmt.apply { applyPattern("HH:mm") }.format(date)
            }
        }
    }
}

val V2TIMMessage?.messageSenderDisplayName: String
    get() = run {
        if (this == null) {
            return ""
        }
        var displayName: String = ""
        if (!TextUtils.isEmpty(nameCard)) {
            displayName = nameCard
        } else if (!TextUtils.isEmpty(friendRemark)) {
            displayName = friendRemark
        } else if (!TextUtils.isEmpty(nickName)) {
            displayName = nickName
        } else {
            displayName = sender
        }
        return displayName
    }

fun jsonData2Dictionary(jsonData: ByteArray?): Map<String, Any>? {
    if (jsonData == null) return null

    return try {
        val jsonString = String(jsonData, Charsets.UTF_8)
        val type = TypeToken.getParameterized(Map::class.java, String::class.java, Any::class.java).type
        Gson().fromJson(jsonString, type)
    } catch (e: Exception) {
        Log.e("Util", "Gson conversion failed", e)
        null
    }
}