package io.trtc.tuikit.atomicx.messagelist.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

@Composable
fun AuxiliaryTextBubble(
    modifier: Modifier = Modifier,
    isSelf: Boolean,
    isLoading: Boolean,
    showMenu: Boolean = false,
    topSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    onLongPress: () -> Unit = {},
    onDismissMenu: () -> Unit = {},
    onHide: () -> Unit = {},
    onForward: () -> Unit = {},
    onCopy: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colors = LocalTheme.current.colors
    val density = LocalDensity.current
    val bubbleColor = if (isSelf) colors.bgColorBubbleOwn else colors.bgColorBubbleReciprocal

    Spacer(modifier = Modifier.height(topSpacing))

    Box(
        modifier = modifier
            .widthIn(max = 240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bubbleColor)
            .then(
                if (!isLoading) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = { onLongPress() })
                    }
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = colors.textColorSecondary
            )
        } else {
            content()
        }
    }

    // Popup Menu
    if (showMenu) {
        Popup(
            alignment = if (isSelf) Alignment.TopEnd else Alignment.TopStart,
            offset = IntOffset(0, with(density) { (-8).dp.roundToPx() }),
            onDismissRequest = { onDismissMenu() },
            properties = PopupProperties(focusable = true)
        ) {
            AuxiliaryTextPopupMenu(
                onHide = {
                    onDismissMenu()
                    onHide()
                },
                onForward = {
                    onDismissMenu()
                    onForward()
                },
                onCopy = {
                    onDismissMenu()
                    onCopy()
                }
            )
        }
    }
}
