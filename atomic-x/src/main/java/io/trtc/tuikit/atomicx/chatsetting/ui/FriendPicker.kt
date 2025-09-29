package io.trtc.tuikit.atomicx.chatsetting.ui

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.chatsetting.viewmodels.FriendPickerViewModel
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicxcore.api.ContactInfo

@Composable
fun FriendPickerDialog(
    isVisible: Boolean,
    title: String = "",
    preSelectedFriends: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (List<ContactInfo>) -> Unit,
    viewModel: FriendPickerViewModel = viewModel()
) {
    val colors = LocalTheme.current.colors
    if (isVisible) {
        var selectedFriends by remember { mutableStateOf(preSelectedFriends.toSet()) }

        LaunchedEffect(isVisible) {
            if (isVisible) {
                viewModel.loadFriends()
                selectedFriends = preSelectedFriends.toSet()
            }
        }

        FullScreenDialog(
            onDismissRequest = onDismiss,
        ) {

            Box(
                modifier = Modifier
                    .background(color = colors.bgColorOperate)
                    .systemBarsPadding()
            ) {
                FriendPickerFullscreenContent(
                    title = title,
                    selectedFriends = selectedFriends,
                    onSelectionChanged = { userID, isSelected ->
                        selectedFriends = if (isSelected) {
                            selectedFriends + userID
                        } else {
                            selectedFriends - userID
                        }
                    },
                    onDismiss = onDismiss,
                    onConfirm = { friends ->
                        onDismiss()
                        val selectedFriendItems = friends.filter {
                            selectedFriends.contains(it.contactID)
                        }
                        onConfirm(selectedFriendItems)
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun FriendPickerFullscreenContent(
    title: String,
    selectedFriends: Set<String>,
    onSelectionChanged: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (List<ContactInfo>) -> Unit,
    viewModel: FriendPickerViewModel
) {
    val colors = LocalTheme.current.colors
    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            FriendPickerTopBar(
                title = title,
                selectedCount = selectedFriends.size,
                onBackClick = onDismiss,
                onConfirmClick = { onConfirm(friends) },
                hasSelection = selectedFriends.isNotEmpty()
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF007AFF),
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF7F7F7))
                ) {
                    items(friends) { friend ->
                        FriendPickerContactItem(
                            friend = friend,
                            isSelected = selectedFriends.contains(friend.contactID),
                            onSelectionChanged = { isSelected ->
                                onSelectionChanged(friend.contactID, isSelected)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendPickerTopBar(
    title: String,
    selectedCount: Int,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    hasSelection: Boolean
) {
    val colors = LocalTheme.current.colors

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colors.bgColorOperate)
                .padding(horizontal = 16.dp)
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
                fontWeight = FontWeight.W400,
                color = colors.textColorPrimary,
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                modifier = Modifier
                    .clickable {
                        onConfirmClick()
                    }
                    .align(Alignment.CenterEnd),
                text = if (selectedCount > 0) "${stringResource(R.string.base_component_confirm)}($selectedCount)"
                else stringResource(R.string.base_component_confirm),
                fontSize = 14.sp,
                color = colors.textColorLink,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.strokeColorSecondary)
        )
    }
}

@Composable
private fun FriendPickerContactItem(
    friend: ContactInfo,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    val colors = LocalTheme.current.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged(!isSelected) }
            .background(colors.bgColorOperate)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        ChatSettingCheckbox(
            checked = isSelected,
            onCheckedChange = onSelectionChanged
        )

        Spacer(modifier = Modifier.width(12.dp))

        Avatar(
            url = friend.avatarURL,
            name = friend.displayName,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = friend.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                color = colors.textColorPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        }

    }
}
