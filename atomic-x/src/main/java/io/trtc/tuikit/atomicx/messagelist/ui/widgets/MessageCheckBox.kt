package io.trtc.tuikit.atomicx.messagelist.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme


@Composable
fun MessageCheckBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checked: Boolean
) {
    val colors = LocalTheme.current.colors
    Box(
        modifier = modifier.size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!enabled) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color = colors.scrollbarColorDefault, shape = CircleShape)
            )
        } else {
            if (checked) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = colors.textColorLink,
                            shape = CircleShape
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = colors.textColorButton,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(
                            width = 1.5.dp,
                            color = colors.scrollbarColorDefault,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
