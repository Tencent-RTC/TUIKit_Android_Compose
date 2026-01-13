package io.trtc.tuikit.chat.chatsetting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.basecomponent.utils.EventBus
import io.trtc.tuikit.atomicx.basecomponent.utils.SetActivitySystemBarAppearance
import io.trtc.tuikit.atomicx.chatsetting.ui.C2CChatSetting
import io.trtc.tuikit.atomicx.chatsetting.ui.GroupChatSetting
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore
import io.trtc.tuikit.chat.BaseActivity
import io.trtc.tuikit.chat.Event
import io.trtc.tuikit.chat.chat.ChatActivity
import io.trtc.tuikit.chat.widgets.AddFriendDialog

class ChatSettingActivity : BaseActivity() {

    companion object {
        private const val USER_ID = "user_id"
        private const val GROUP_ID = "group_id"
        private const val NEED_NAVIGATE_TO_CHAT = "needNavigateToChat"

        fun start(
            context: Context,
            userID: String? = null,
            groupID: String? = null,
            needNavigateToChat: Boolean = false
        ) {
            val intent = Intent(context, ChatSettingActivity::class.java).apply {
                putExtra(USER_ID, userID)
                putExtra(GROUP_ID, groupID)
                putExtra(NEED_NAVIGATE_TO_CHAT, needNavigateToChat)
            }
            context.startActivity(intent)
        }

    }

    val contactListStore = ContactListStore.create()
    val contactState = contactListStore.contactListState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userID = intent.getStringExtra(USER_ID)
        val groupID = intent.getStringExtra(GROUP_ID)
        val needNavigateToChat = intent.getBooleanExtra(NEED_NAVIGATE_TO_CHAT, false)

        setContent {
            SetActivitySystemBarAppearance()

            var showAddFriendDialog by remember { mutableStateOf(false) }
            var contactInfo by remember { mutableStateOf<ContactInfo?>(null) }

            val colors = LocalTheme.current.colors
            if (!userID.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.bgColorOperate)
                        .systemBarsPadding()
                ) {
                    ChatSettingHeader(
                        title = stringResource(R.string.chat_setting_contact_info),
                        onBackClick = { finish() },
                    )
                    C2CChatSetting(
                        userID = userID,
                        onSendMessageClick = {
                            if (needNavigateToChat) {
                                startActivity(Intent(this@ChatSettingActivity, ChatActivity::class.java).apply {
                                    putExtra("conversationID", "c2c_${userID}")
                                })
                            }
                            finish()
                        },
                        onContactDelete = {
                            EventBus.post(Event.ContactDeleted(userID))
                            finish()
                        },
                    )
                }
            } else if (!groupID.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.bgColorOperate)
                        .systemBarsPadding()
                ) {
                    ChatSettingHeader(
                        title = stringResource(R.string.chat_setting_group_info),
                        onBackClick = { finish() },
                    )
                    GroupChatSetting(
                        groupID = groupID,
                        onGroupMemberClick = { groupMember ->
                            contactListStore.fetchUserInfo(groupMember.userID, object : CompletionHandler {
                                override fun onSuccess() {
                                    contactState.addFriendInfo.value?.let {
                                        if (it.isContact == true) {
                                            start(this@ChatSettingActivity, userID = it.contactID, groupID = "", true)
                                        } else {
                                            showAddFriendDialog = true
                                            contactInfo = it
                                        }
                                    }
                                }

                                override fun onFailure(code: Int, desc: String) {
                                }
                            })
                        },
                        onSendMessageClick = {
                            if (needNavigateToChat) {
                                startActivity(Intent(this@ChatSettingActivity, ChatActivity::class.java).apply {
                                    putExtra("conversationID", "group_${groupID}")
                                })
                            }
                            finish()
                        },
                        onGroupDelete = {
                            EventBus.post(Event.GroupDeleted(groupID))
                            finish()
                        },
                    )
                }
            }
            if (showAddFriendDialog && contactInfo != null) {
                AddFriendDialog(
                    contactInfo = contactInfo!!,
                    onDismiss = { showAddFriendDialog = false })
            }
        }
    }
}

@Composable
fun ChatSettingHeader(
    title: String,
    onBackClick: () -> Unit,
) {
    val colors = LocalTheme.current.colors
    Column(modifier = Modifier) {
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
                fontWeight = FontWeight.W600,
                color = colors.textColorPrimary
            )

        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.strokeColorSecondary
        )
    }
}