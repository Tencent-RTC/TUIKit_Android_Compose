package io.trtc.tuikit.atomicx.chatsetting.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.chatsetting.viewmodels.C2CChatSettingViewModel
import io.trtc.tuikit.atomicx.chatsetting.viewmodels.C2CChatSettingViewModelFactory

@Composable
fun C2CChatSetting(
    userID: String,
    modifier: Modifier = Modifier,
    onSendMessageClick: () -> Unit = {},
    onContactDelete: () -> Unit = {},
    c2cChatSettingViewModelFactory: C2CChatSettingViewModelFactory = C2CChatSettingViewModelFactory(userID)
) {
    val colors = LocalTheme.current.colors
    val c2cChatSettingViewModel = viewModel(
        C2CChatSettingViewModel::class,
        factory = c2cChatSettingViewModelFactory
    )

    val isLoading by c2cChatSettingViewModel.isLoading.collectAsState()
    val nickname by c2cChatSettingViewModel.nickname.collectAsState()
    val avatar by c2cChatSettingViewModel.avatar.collectAsState()
    val signature by c2cChatSettingViewModel.signature.collectAsState()
    val remark by c2cChatSettingViewModel.remark.collectAsState()
    val isNotDisturb by c2cChatSettingViewModel.isNotDisturb.collectAsState()
    val isPinned by c2cChatSettingViewModel.isPinned.collectAsState()
    val isContact by c2cChatSettingViewModel.isContact.collectAsState()
    val isBlacklisted by c2cChatSettingViewModel.isInBlacklist.collectAsState()

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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ContactInfoSection(
                        userID = userID,
                        displayName = if (nickname.isEmpty()) userID else nickname,
                        avatar = avatar,
                    )
                }

                item {
                    ContactActionButtons(
                        onMessageClick = onSendMessageClick,
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        var showRemarkSelector by remember { mutableStateOf(false) }
                        InfoItem(
                            title = stringResource(R.string.chat_setting_remark),
                            value = remark,
                            hasArrow = true,
                            onClick = {
                                showRemarkSelector = true
                            },
                        )
                        TextInputBottomSheet(
                            isVisible = showRemarkSelector,
                            title = stringResource(R.string.chat_setting_modify_contact_remark),
                            initialText = remark,
                            onDismiss = { showRemarkSelector = false },
                            onConfirm = {
                                c2cChatSettingViewModel.setFriendRemark(it)
                            })

                        SettingItem(
                            title = stringResource(R.string.chat_setting_do_not_disturb),
                            isToggled = isNotDisturb,
                            onToggle = { c2cChatSettingViewModel.toggleC2CDoNotDisturb() }
                        )

                        SettingItem(
                            title = stringResource(R.string.chat_setting_pin),
                            isToggled = isPinned,
                            onToggle = { c2cChatSettingViewModel.toggleC2CTopChat() }
                        )

                        SettingItem(
                            title = stringResource(R.string.chat_setting_add_blacklist),
                            isToggled = isBlacklisted,
                            onToggle = {
                                c2cChatSettingViewModel.toggleC2CBlacklist()
                            }
                        )
                    }

                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {

                        DangerActionItem(
                            title = stringResource(R.string.chat_setting_clear_history_messages),
                            dangerHint = stringResource(R.string.chat_setting_clear_contact_history_messages_tips),
                            onClick = {
                                c2cChatSettingViewModel.clearC2CChatHistory()
                            }
                        )

                        DangerActionItem(
                            title = stringResource(R.string.chat_setting_delete_contact),
                            dangerHint = stringResource(R.string.chat_setting_delete_contact_tips),
                            onClick = {
                                c2cChatSettingViewModel.deleteC2CContact()
                                onContactDelete()
                            }
                        )

                    }
                }
            }
        }
    }
}


@Composable
fun ContactInfoSection(
    displayName: String,
    avatar: String,
    userID: String,
) {
    val colors = LocalTheme.current.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(
            url = avatar,
            name = displayName,
            size = AvatarSize.XXL
        )

        Text(
            text = displayName,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600,
            color = colors.textColorPrimary
        )

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
                    text = userID,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorSecondary
                )
            }
        }
    }
}

@Composable
fun ContactActionButtons(
    onMessageClick: () -> Unit,
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
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

