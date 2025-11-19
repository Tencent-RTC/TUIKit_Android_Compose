package io.trtc.tuikit.atomicx.contactlist.ui.addnewchat

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddNewChatViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddNewChatViewModelFactory
import io.trtc.tuikit.atomicx.contactlist.viewmodels.ChatType
import io.trtc.tuikit.atomicx.contactlist.viewmodels.GroupFlowStep
import io.trtc.tuikit.atomicx.userpicker.UserPicker
import io.trtc.tuikit.atomicx.userpicker.UserPickerState
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore

@Composable
fun AddNewChatBottomSheet(
    modifier: Modifier = Modifier,
    chatType: ChatType = ChatType.SINGLE,
    onDismiss: () -> Unit,
    onCreateChat: (String) -> Unit = {},
    viewModelFactory: AddNewChatViewModelFactory = AddNewChatViewModelFactory(
        ContactListStore.create()
    )
) {
    val viewModel: AddNewChatViewModel = viewModel(
        factory = viewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colors = LocalTheme.current.colors

    LaunchedEffect(Unit) {
        viewModel.clearState()
    }

    LaunchedEffect(chatType) {
        viewModel.setChatType(chatType)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    FullScreenDialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.bgColorMask)
                .statusBarsPadding()
                .navigationBarsPadding()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { },
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                colors = CardDefaults.cardColors(containerColor = colors.bgColorDialog),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = SolidColor(colors.strokeColorPrimary)
                )
            ) {
                Crossfade(targetState = uiState.groupFlowStep) { state ->
                    when (state) {
                        GroupFlowStep.CONTACT_SELECTION -> {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CreateChatHeader(
                                    chatType = uiState.chatType,
                                    selectedCount = uiState.selectedContacts.size,
                                    onDismiss = onDismiss,
                                    onConfirm = { viewModel.startChat() },
                                    isCreating = uiState.isCreating
                                )

                                if (chatType == ChatType.GROUP && uiState.selectedContacts.isNotEmpty()) {
                                    SelectedContactsSection(
                                        selectedContacts = uiState.selectedContacts.toList(),
                                        onContactRemove = {
                                            viewModel.removeSelectedContact(it)
                                        }
                                    )
                                }

                                val userPickerDataSource by viewModel.contactDataSource.collectAsState()
                                val userPickerState = UserPickerState<ContactInfo>()
                                val selected by userPickerState.selectedItems.collectAsState()
                                UserPicker(
                                    dataSource = userPickerDataSource,
                                    state = userPickerState,
                                    maxCount = if (chatType == ChatType.SINGLE) 1 else 100,
                                    onSelectedChanged = {
                                        if (chatType == ChatType.SINGLE) {
                                            if (selected.isNotEmpty() == true) {
                                                onCreateChat("c2c_${it.first().key}")
                                            }
                                        } else {
                                            viewModel.setSelectedContacts(it)
                                        }
                                    },
                                )

                            }
                        }

                        GroupFlowStep.GROUP_SETTINGS -> {
                            GroupSettingsBottomSheet(
                                viewModel = viewModel,
                                isCreating = uiState.isCreating,
                                onBack = {
                                    viewModel.clearGroupSettingsScreen()
                                },
                                onCreateGroup = {
                                    val createdGroupID = viewModel.getCreatedGroupID()
                                    createdGroupID?.let {
                                        onCreateChat("group_$it")
                                    }
                                },
                                onShowGroupTypeSelection = {
                                    viewModel.showGroupTypeSelectionScreen()
                                }
                            )
                        }

                        GroupFlowStep.GROUP_TYPE_SELECTION -> {
                            GroupTypeSelectionBottomSheet(
                                viewModel = viewModel,
                                onDismiss = {
                                    viewModel.clearGroupTypeSelectionScreen()
                                },
                                onTypeSelected = { groupTypeOption ->
                                    viewModel.updateSelectedGroupType(groupTypeOption)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateChatHeader(
    chatType: ChatType,
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isCreating: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.base_component_cancel),
                fontSize = 16.sp,
                color = colors.textColorLink,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() }
            )

            Text(
                modifier = Modifier.align(Alignment.Center),
                text = when (chatType) {
                    ChatType.SINGLE -> stringResource(R.string.contact_list_create_c2c)
                    ChatType.GROUP -> stringResource(R.string.contact_list_create_group)
                },
                color = colors.textColorPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center
            )

            if (chatType == ChatType.GROUP) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(16.dp),
                        color = colors.textColorLink,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.contact_list_contact_next_step),
                        fontSize = 16.sp,
                        color = if (selectedCount > 0) colors.textColorLink else colors.textColorDisable,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (selectedCount > 0) {
                                    onConfirm()
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedContactsSection(
    selectedContacts: List<ContactInfo>,
    onContactRemove: (ContactInfo) -> Unit
) {
    val colors = LocalTheme.current.colors
    Text(
        text = stringResource(R.string.contact_list_selected_group_member),
        fontSize = 14.sp,
        fontWeight = FontWeight.W600,
        color = colors.textColorPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgColorDialog)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(selectedContacts, key = { it.contactID }) { contact ->
                SelectedContactItem(
                    contact = contact,
                    onRemove = { onContactRemove(contact) }
                )
            }
        }
    }
}

@Composable
private fun SelectedContactItem(
    contact: ContactInfo,
    onRemove: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clickable { onRemove() }
    ) {
        Box {
            Avatar(
                url = contact.avatarURL,
                name = contact.displayName,
                size = AvatarSize.M
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(16.dp)
                    .clickable {
                        onRemove()
                    }
                    .background(
                        color = colors.bgColorTopBar,
                        shape = CircleShape
                    )
                    .border(
                        width = 0.1.dp,
                        color = colors.strokeColorSecondary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "remove",
                    tint = colors.textColorPrimary,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = contact.displayName,
            color = colors.textColorPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
