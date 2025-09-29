package io.trtc.tuikit.atomicx.chatsetting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AlertDialog
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Switch
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme


@Composable
fun ActionButton(
    title: String,
    iconResID: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors
    Column(
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = colors.bgColorTopBar,
                shape = RoundedCornerShape(12.dp)
            )
            .width(92.dp)
            .height(98.dp)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(36.dp),
            painter = painterResource(iconResID),
            contentDescription = "",
            tint = colors.textColorLink
        )
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = colors.textColorPrimary
        )
    }
}

@Composable
fun GroupNoticeFullScreenDialog(
    isVisible: Boolean,
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (!isVisible) return
    val colors = LocalTheme.current.colors
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputText by remember { mutableStateOf(initialText) }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = inputText,
                selection = TextRange(inputText.length)
            )
        )
    }
    var isEditing by remember { mutableStateOf(false) }

    FullScreenDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgColorOperate)
                .systemBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgColorOperate)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.base_component_cancel),
                    fontSize = 14.sp,
                    color = colors.textColorLink,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(indication = null, interactionSource = null) { onDismiss() }
                )

                Text(
                    text = stringResource(id = R.string.chat_setting_group_notice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = colors.textColorPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )

                Text(
                    text = if (isEditing) stringResource(R.string.base_component_confirm) else stringResource(
                        R.string.chat_setting_edit
                    ),
                    fontSize = 14.sp,
                    color = colors.textColorLink,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(indication = null, interactionSource = null) {
                            if (isEditing) {
                                keyboardController?.hide()
                                onConfirm(inputText.trim())
                                isEditing = false
                            } else {
                                isEditing = true
                            }
                        }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.bgColorOperate)
                    ) {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newText ->
                                textFieldValue = newText
                                inputText = newText.text
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester)
                                .clickable {
                                    focusRequester.requestFocus()
                                },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = colors.textColorPrimary,
                                fontWeight = FontWeight.W400
                            ),
                            cursorBrush = SolidColor(colors.textColorLink),
                        )
                    }
                } else {
                    Text(
                        text = if (inputText.isBlank()) stringResource(id = R.string.chat_setting_no_group_notice) else inputText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W400,
                        color = colors.textColorSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoItem(
    title: String,
    value: String = "",
    hasArrow: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .background(color = colors.bgColorTopBar)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            color = colors.textColorPrimary,
            maxLines = 1,
            modifier = Modifier
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.textColorPrimary
            )

            if (hasArrow) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Arrow",
                    tint = colors.textColorSecondary,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    isToggled: Boolean,
    onToggle: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.bgColorTopBar)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            color = colors.textColorPrimary,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isToggled,
            onCheckedChange = { onToggle() },
        )
    }
}

@Composable
fun DangerActionItem(
    title: String,
    dangerHint: String = "",
    isAlert: Boolean = true,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    var showAlertDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isAlert) {
                    showAlertDialog = true
                } else {
                    onClick()
                }
            }
            .background(color = colors.bgColorTopBar)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            color = colors.textColorError
        )
    }
    if (isAlert) {
        AlertDialog(
            isVisible = showAlertDialog,
            message = dangerHint,
            onCancel = { showAlertDialog = false },
            onDismiss = { showAlertDialog = false },
            onConfirm = { showAlertDialog = false;onClick() }
        )
    }
}

@Composable
fun ChatSettingCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = LocalTheme.current.colors
    Box(
        modifier = modifier
            .size(16.dp)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = colors.textColorLink,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checked",
                    tint = colors.textColorButton,
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
            )
        }
    }
}

