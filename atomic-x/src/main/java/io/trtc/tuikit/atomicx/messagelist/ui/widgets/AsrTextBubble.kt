package io.trtc.tuikit.atomicx.messagelist.ui.widgets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme


@Composable
fun AsrTextBubble(
    modifier: Modifier = Modifier,
    isSelf: Boolean,
    isLoading: Boolean,
    asrText: String?,
    showMenu: Boolean = false,
    onLongPress: () -> Unit = {},
    onDismissMenu: () -> Unit = {},
    onHide: () -> Unit = {},
    onForward: () -> Unit = {},
    onCopy: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors

    AuxiliaryTextBubble(
        modifier = modifier,
        isSelf = isSelf,
        isLoading = isLoading,
        showMenu = showMenu,
        topSpacing = 10.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        onLongPress = onLongPress,
        onDismissMenu = onDismissMenu,
        onHide = onHide,
        onForward = onForward,
        onCopy = onCopy
    ) {
        Text(
            text = asrText ?: "",
            fontSize = 14.sp,
            color = colors.textColorPrimary
        )
    }
}
