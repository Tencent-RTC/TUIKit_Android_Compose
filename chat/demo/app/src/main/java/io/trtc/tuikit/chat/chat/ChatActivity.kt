package io.trtc.tuikit.chat.chat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.trtc.tuikit.atomicx.basecomponent.utils.SetActivitySystemBarAppearance
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.ContactInfo
import io.trtc.tuikit.atomicxcore.api.ContactListStore
import io.trtc.tuikit.atomicxcore.api.MessageInfo
import io.trtc.tuikit.chat.BaseActivity
import io.trtc.tuikit.chat.Event
import io.trtc.tuikit.chat.EventBus
import io.trtc.tuikit.chat.chatsetting.ChatSettingActivity
import io.trtc.tuikit.chat.collectOn
import io.trtc.tuikit.chat.pages.ChatPage
import io.trtc.tuikit.chat.widgets.AddFriendDialog

class ChatActivity : BaseActivity() {

    val contactListStore = ContactListStore.create()
    val contactState = contactListStore.contactListState
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val conversationID = intent?.getStringExtra("conversationID")
        val message = intent?.getParcelableExtra<MessageInfo>("message")
        if (conversationID == null) {
            finish()
            return
        }

        EventBus.collectOn<Event.GroupDeleted>(this) {
            if ("group_${it.groupID}" == conversationID) {
                finish()
            }
        }
        EventBus.collectOn<Event.ContactDeleted>(this) {
            if ("c2c_${it.contactID}" == conversationID) {
                finish()
            }
        }

        setContent {
            SetActivitySystemBarAppearance()

            var showAddFriendDialog by remember { mutableStateOf(false) }
            var contactInfo by remember { mutableStateOf<ContactInfo?>(null) }

            fun handleChatSettingNavigation(userID: String? = null, groupID: String? = null) {
                if (userID.isNullOrEmpty() == false) {
                    contactListStore.fetchUserInfo(userID, object : CompletionHandler {
                        override fun onSuccess() {
                            contactState.addFriendInfo.value?.let {
                                if (it.isContact == true) {
                                    ChatSettingActivity.start(context = this@ChatActivity, userID = userID)
                                } else {
                                    showAddFriendDialog = true
                                    contactInfo = it
                                }
                            }
                        }

                        override fun onFailure(code: Int, desc: String) {
                        }
                    })
                } else if (groupID.isNullOrEmpty() == false) {
                    ChatSettingActivity.start(context = this@ChatActivity, groupID = groupID)
                }
            }

            ChatPage(
                conversationID = conversationID,
                locateMessage = message,
                onUserClick = {
                    handleChatSettingNavigation(it)
                }, onChatHeaderClick = {
                    val userID = getUserID(conversationID)
                    val groupID = getGroupID(conversationID)
                    handleChatSettingNavigation(userID, groupID)
                }, onBackClick = { finish() })


            if (showAddFriendDialog && contactInfo != null) {
                AddFriendDialog(
                    contactInfo = contactInfo!!,
                    onDismiss = { showAddFriendDialog = false })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}

fun getUserID(conversationID: String): String? {
    val c2cConversationIDPrefix = "c2c_"
    return if (conversationID.startsWith(c2cConversationIDPrefix)) {
        conversationID.replaceFirst(c2cConversationIDPrefix, "")
    } else {
        null
    }
}

fun getGroupID(conversationID: String): String? {
    val groupConversationIDPrefix = "group_"
    return if (conversationID.startsWith(groupConversationIDPrefix)) {
        conversationID.replaceFirst(groupConversationIDPrefix, "")
    } else {
        null
    }
}