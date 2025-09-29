package io.trtc.tuikit.atomicx.basecomponent.basiccontrols

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme


enum class SwitchSize(
    val width: Dp,
    val height: Dp,
    val thumbSize: Dp,
    val padding: Dp,
    val textSize: TextUnit
) {
    S(width = 26.dp, height = 16.dp, thumbSize = 12.dp, padding = 2.dp, textSize = 10.sp),
    M(width = 32.dp, height = 20.dp, thumbSize = 15.dp, padding = 2.5.dp, textSize = 12.sp),
    L(width = 40.dp, height = 24.dp, thumbSize = 18.dp, padding = 3.dp, textSize = 14.sp)
}

sealed class SwitchType {
    data object Basic : SwitchType()
    data object WithText : SwitchType()
    data object WithIcon : SwitchType()
}

@Composable
fun Switch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    size: SwitchSize = SwitchSize.L,
    type: SwitchType = SwitchType.Basic,
) {
    val colors = LocalTheme.current.colors
    val checkedIcon: ImageVector = Icons.Default.Check
    val uncheckedIcon: ImageVector = Icons.Default.Close
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "thumbOffset"
    )

    var trackColor = if (checked) colors.switchColorOn else colors.switchColorOff
    var thumbColor = colors.switchColorButton
    if (!enabled) {
        trackColor = trackColor.copy(alpha = 0.6f)
        thumbColor = thumbColor.copy(alpha = 0.6f)
    }

    val width = when (type) {
        SwitchType.Basic -> size.width
        SwitchType.WithIcon -> size.height * 2
        SwitchType.WithText -> size.height * 2
    }

    val maxOffset = width - size.thumbSize - size.padding * 2

    Box(
        modifier = modifier
            .width(width)
            .height(size.height)
            .clip(CircleShape)
            .background(color = trackColor)
            .clickable(enabled = enabled) {
                onCheckedChange?.invoke(!checked)
            }
            .padding(horizontal = size.padding),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(size.thumbSize)
                .offset(x = (maxOffset * thumbOffset))
                .shadow(
                    elevation = when (size) {
                        SwitchSize.S -> 1.6.dp
                        SwitchSize.M -> 2.dp
                        SwitchSize.L -> 2.4.dp
                    },
                    shape = CircleShape,
                    clip = false
                )
                .background(
                    color = thumbColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size.thumbSize * 0.6f),
                    strokeWidth = 1.dp,
                    color = colors.switchColorOn
                )
            }

        }
        if (type == SwitchType.WithText || type == SwitchType.WithIcon) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = if (checked) Arrangement.Start else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val openText = stringResource(R.string.base_component_switch_open)
                val closeText = stringResource(R.string.base_component_switch_close)

                when (type) {
                    SwitchType.WithIcon -> {
                        val icon = if (checked) checkedIcon else uncheckedIcon
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(
                                    start = if (checked) 0.dp else size.padding,
                                    end = if (checked) size.padding else 0.dp
                                )
                                .width(width - size.thumbSize - size.padding * 2),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(size.thumbSize),
                                tint = colors.textColorButton
                            )
                        }
                    }

                    SwitchType.WithText -> {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(
                                    start = if (checked) 0.dp else size.padding,
                                    end = if (checked) size.padding else 0.dp
                                )
                                .width(width - size.thumbSize - size.padding * 2),
                            contentAlignment = Alignment.Center
                        ) {

                            BasicText(
                                text = if (checked) openText else closeText,
                                style = TextStyle(
                                    color = colors.textColorButton,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.W500,
                                    lineHeight = with(LocalDensity.current) { size.height.toSp() }
                                ),
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = size.textSize * 0.7,
                                    maxFontSize = size.textSize,
                                    stepSize = 0.25.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }

                    else -> {}
                }

            }
        }
    }
}
