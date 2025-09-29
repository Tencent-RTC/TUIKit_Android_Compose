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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

@Composable
fun TextInputBottomSheet(
    isVisible: Boolean,
    title: String = "",
    initialText: String = "",
    placeholder: String = "",
    maxLength: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = LocalTheme.current.colors
    if (isVisible) {

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() }
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        color = colors.bgColorOperate
                    ) {
                        TextInputBottomSheetContent(
                            title = title,
                            initialText = initialText,
                            placeholder = placeholder,
                            maxLength = maxLength,
                            onClose = onDismiss,
                            onConfirm = { result -> onDismiss();onConfirm(result) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextInputBottomSheetContent(
    title: String,
    initialText: String,
    placeholder: String,
    maxLength: Int,
    onClose: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = LocalTheme.current.colors
    var inputText by remember { mutableStateOf(initialText) }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = inputText,
                selection = TextRange(inputText.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.W600,
                color = colors.textColorPrimary
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = colors.textColorSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClose() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colors.bgColorInput,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newText ->
                    if (maxLength <= 0 || newText.toString().length <= maxLength) {
                        textFieldValue = newText
                        inputText = textFieldValue.text
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = colors.textColorPrimary,
                    fontWeight = FontWeight.W400
                ),
                singleLine = false,
                maxLines = 5,
                cursorBrush = SolidColor(colors.textColorLink),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (inputText.isNotBlank()) {
                            onConfirm(inputText.trim())
                        }
                    }
                ),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = colors.textColorSecondary,
                            fontWeight = FontWeight.W400
                        )
                    }
                    innerTextField()
                }
            )
        }

        if (maxLength > 0) {
            Text(
                text = "${inputText.length}/$maxLength",
                fontSize = 12.sp,
                color = colors.textColorSecondary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                onConfirm(inputText.trim())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.buttonColorPrimaryDefault,
                contentColor = colors.textColorButton,
                disabledContainerColor = colors.buttonColorPrimaryDisabled,
                disabledContentColor = colors.textColorButton
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.base_component_confirm),
                fontSize = 16.sp,
                fontWeight = FontWeight.W500
            )
        }
    }
} 