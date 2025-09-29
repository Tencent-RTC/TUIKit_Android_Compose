package io.trtc.tuikit.atomicx.contactlist.ui.addnewchat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddNewChatViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.GroupTypeOption
import io.trtc.tuikit.atomicx.contactlist.viewmodels.getGroupTypeOptionList


@Composable
fun GroupTypeSelectionBottomSheet(
    viewModel: AddNewChatViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onTypeSelected: (GroupTypeOption) -> Unit,
) {
    val currentSelectedType by viewModel.currentSelectedGroupType.collectAsState()
    var tempSelectedType by remember { mutableStateOf(currentSelectedType.type) }

    val groupTypes = getGroupTypeOptionList()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            GroupTypeSelectionHeader(
                onCancel = onDismiss,
                onConfirm = {
                    val selectedGroupType = groupTypes.find { it.type == tempSelectedType }
                    if (selectedGroupType != null) {
                        onTypeSelected(selectedGroupType)
                    }
                }
            )
        }

        items(groupTypes) { groupType ->
            GroupTypeOptionItem(
                groupType = groupType,
                isSelected = tempSelectedType == groupType.type,
                onClick = {
                    tempSelectedType = groupType.type
                }
            )
        }

    }
}

@Composable
private fun GroupTypeSelectionHeader(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    ) { onCancel() }
            )

            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.contact_list_group_type_select_text),
                color = colors.textColorPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.base_component_confirm),
                fontSize = 16.sp,
                color = colors.textColorLink,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onConfirm() }
            )
        }

    }
}

@Composable
private fun GroupTypeOptionItem(
    groupType: GroupTypeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgColorDialog),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (isSelected) 1.dp else 1.dp,
            brush = SolidColor(if (isSelected) colors.textColorLink else colors.strokeColorPrimary)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    CheckBox(checked = true) { }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = stringResource(groupType.displayNameResID),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = stringResource(groupType.descriptionResID),
                fontSize = 12.sp,
                fontWeight = FontWeight.W400,
                color = colors.textColorSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun CheckBox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = LocalTheme.current.colors
    Box(
        modifier = modifier
            .size(16.dp)
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = if (enabled) colors.textColorLink else colors.textColorLinkDisabled,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checked",
                    tint = if (enabled) colors.textColorButton else colors.textColorButtonDisabled,
                    modifier = Modifier.size(12.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(
                        width = 1.dp,
                        color = colors.scrollbarColorDefault,
                        shape = CircleShape
                    )
                    .background(
                        color = if (!enabled) colors.scrollbarColorDefault else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!enabled) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "disabled",
                        tint = colors.textColorButtonDisabled,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
