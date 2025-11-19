package io.trtc.tuikit.atomicx.contactlist.ui.addnewchat

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tencent.qcloud.tuicore.util.ErrorMessageConverter
import com.tencent.qcloud.tuicore.util.ToastUtil
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarContent
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarShape
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddNewChatViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.getGroupAvatarUrls
import io.trtc.tuikit.atomicxcore.api.contact.GroupType

@Composable
fun GroupSettingsBottomSheet(
    viewModel: AddNewChatViewModel,
    isCreating: Boolean,
    onCreateGroup: () -> Unit,
    onBack: () -> Unit,
    onShowGroupTypeSelection: () -> Unit
) {
    val colors = LocalTheme.current.colors
    val uiState by viewModel.uiState.collectAsState()
    val groupName = uiState.groupName
    val groupID = uiState.groupID
    val groupAvatarUrl = uiState.groupAvatarUrl
    val currentSelectedType by viewModel.currentSelectedGroupType.collectAsState()
    val context = LocalContext.current
    val activity = LocalActivity.current

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            GroupSettingsHeader(
                onBack = onBack,
                onCreateGroup = {
                    if (currentSelectedType.type == GroupType.COMMUNITY) {
                        if (!groupID.isNullOrEmpty() && !groupID.startsWith("@TGS#_")) {
                            ToastUtil.toastShortMessage(context.getString(R.string.contact_list_community_id_edit_format_tips))
                            return@GroupSettingsHeader
                        }
                    } else {
                        if (!groupID.isNullOrEmpty() && groupID.startsWith("@TGS#")) {
                            ToastUtil.toastShortMessage(context.getString(R.string.contact_list_group_id_edit_format_tips))
                            return@GroupSettingsHeader
                        }
                    }

                    viewModel.createGroupChatWithSettings(
                        groupName,
                        groupID,
                        groupAvatarUrl,
                        onSuccess = {
                            onCreateGroup()
                        },
                        onFailure = { code, desc ->
                            ToastUtil.toastShortMessage(ErrorMessageConverter.convertIMError(code, desc))
                        })
                },
                isCreating = isCreating
            )
        }

        item {
            InputSection(
                text = groupName,
                onTextChange = { viewModel.updateGroupName(it) }
            )
        }

        item {
            InputSection(
                text = groupID ?: "",
                hint = stringResource(R.string.contact_list_group_id_option),
                onTextChange = { viewModel.updateGroupID(it) }
            )
        }

        item {
            GroupTypeSection(
                selectedType = currentSelectedType.type.value,
                onTypeChange = { onShowGroupTypeSelection() }
            )
        }

        item {
            GroupDescriptionSection(currentSelectedType.descriptionResID)
        }

        item {
            GroupAvatarSection(groupName = groupName) {
                viewModel.updateGroupAvatarUrl(it)
            }
        }

        item {
            SelectedContactsSection(
                selectedContacts = uiState.selectedContacts.toList(),
                onContactRemove = {
                    viewModel.removeSelectedContact(it)
                }
            )
        }
    }
}

@Composable
private fun GroupSettingsHeader(
    onBack: () -> Unit,
    onCreateGroup: () -> Unit,
    isCreating: Boolean
) {
    val colors = LocalTheme.current.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.contact_list_contact_prev_step),
                fontSize = 16.sp,
                color = colors.textColorLink,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onBack() }
            )

            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.contact_list_create_group),
                color = colors.textColorPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center
            )

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
                    text = stringResource(R.string.contact_list_create),
                    fontSize = 16.sp,
                    color = colors.textColorLink,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onCreateGroup() }
                )
            }
        }
    }
}

@Composable
private fun InputSection(
    text: String,
    hint: String = "",
    onTextChange: (String) -> Unit
) {
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box() {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = colors.textColorPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                cursorBrush = SolidColor(colors.textColorLink)
            )

            if (text.isEmpty()) {
                Text(
                    text = hint,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = colors.textColorSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = colors.strokeColorPrimary
        )
    }
}


@Composable
private fun GroupTypeSection(
    selectedType: String,
    onTypeChange: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTypeChange() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.contact_list_group_type_text),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedType,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = "select group type",
                    tint = colors.textColorSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = colors.strokeColorPrimary
        )
    }
}

@Composable
private fun GroupAvatarSection(groupName: String, onGroupAvatarSelected: (String?) -> Unit) {
    val colors = LocalTheme.current.colors
    var selected by remember { mutableStateOf<String>("DefaultAvatar") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.contact_list_group_avatar_text),
            fontSize = 14.sp,
            fontWeight = FontWeight.W600,
            color = colors.textColorPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item(key = "DefaultAvatar") {
                    Avatar(
                        modifier = Modifier.then(
                            if (selected == "DefaultAvatar") {
                                Modifier.border(
                                    width = 2.dp,
                                    color = colors.textColorLink,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            } else {
                                Modifier
                            }
                        ),
                        content = AvatarContent.Text(groupName),
                        size = AvatarSize.L,
                        shape = AvatarShape.RoundRectangle
                    ) {
                        selected = "DefaultAvatar"
                        onGroupAvatarSelected(null)
                    }
                }
                items(getGroupAvatarUrls(), key = { it }) { url ->
                    Avatar(
                        modifier = Modifier.then(
                            if (selected == url) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = colors.textColorLink,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            } else {
                                Modifier
                            }
                        ),
                        content = AvatarContent.Image(url),
                        size = AvatarSize.L,
                        shape = AvatarShape.RoundRectangle
                    ) {
                        onGroupAvatarSelected(url)
                        selected = url
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupDescriptionSection(
    descriptionResID: Int
) {
    val colors = LocalTheme.current.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.bgColorDialog)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(descriptionResID),
            fontSize = 12.sp,
            fontWeight = FontWeight.W400,
            color = colors.textColorSecondary,
            lineHeight = 16.sp
        )
    }
}