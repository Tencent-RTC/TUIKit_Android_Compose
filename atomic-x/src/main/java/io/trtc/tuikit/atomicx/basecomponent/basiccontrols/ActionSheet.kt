package io.trtc.tuikit.atomicx.basecomponent.basiccontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

data class ActionItem(
    val text: String,
    val isDestructive: Boolean = false,
    val isEnabled: Boolean = true,
    val value: Any?,
)

@Composable
fun ActionSheet(
    isVisible: Boolean,
    options: List<ActionItem>,
    onDismiss: () -> Unit,
    onActionSelected: (ActionItem) -> Unit,
) {
    val colors = LocalTheme.current.colors
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(indication = null, interactionSource = null) { onDismiss() }
                    .padding(horizontal = 8.dp), verticalArrangement = Arrangement.Bottom
            ) {

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.bgColorBubbleReciprocal
                ) {
                    Column {
                        options.forEachIndexed { index, option ->
                            ActionItem(
                                option = option,
                                onClick = {
                                    if (option.isEnabled) {
                                        onDismiss()
                                        onActionSelected(option)
                                    }
                                }
                            )

                            if (index < options.size - 1) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = colors.strokeColorPrimary
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.padding(vertical = 6.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.bgColorDialog
                ) {
                    Text(
                        text = stringResource(R.string.base_component_cancel),
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W600,
                            color = colors.buttonColorPrimaryDefault
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDismiss() }
                            .padding(vertical = 16.dp)
                    )
                }

                Box(modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}

@Composable
private fun ActionItem(
    option: ActionItem,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    val textColor = when {
        option.isDestructive -> colors.textColorError
        !option.isEnabled -> colors.textColorDisable
        else -> colors.buttonColorPrimaryDefault
    }

    Text(
        text = option.text,
        style = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.W400,
            color = textColor
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = option.isEnabled) { onClick() }
            .background(color = colors.bgColorDialog)
            .padding(vertical = 16.dp)
    )
}

