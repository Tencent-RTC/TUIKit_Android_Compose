package io.trtc.tuikit.atomicx.messagelist.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messagelist.viewmodels.HighlightManager
import kotlinx.coroutines.delay


@Composable
fun Modifier.highlightBackground(
    color: Color,
    highlightKey: String,
    shape: Shape = RectangleShape,
): Modifier {
    val highlights by HighlightManager.highlights.collectAsState()
    val highlightState = highlights[highlightKey]
    val colors = LocalTheme.current.colors

    return if (highlightState != null) {
        val config = highlightState.config
        var animationPhase by remember { mutableStateOf(0) }

        LaunchedEffect(highlightState) {
            for (i in 1..config.flashCount) {
                animationPhase = i * 2 - 1
                delay(config.flashDuration)
                animationPhase = i * 2
                delay(config.flashDuration)
            }
            animationPhase = 0
        }

        val targetAlpha = when (animationPhase) {
            0 -> 0f
            in 1..6 step 2 -> 0.8f
            in 2..6 step 2 -> 0.1f
            else -> 0f
        }

        val alpha by animateFloatAsState(
            targetValue = targetAlpha,
            animationSpec = tween(durationMillis = config.flashDuration.toInt()),
            label = "HighlightAlpha"
        )

        val highlightColor = if (alpha > 0f) {
            colors.textColorWarning.copy(alpha = alpha)
        } else {
            color
        }

        this.background(
            color = highlightColor,
            shape = shape
        )
    } else {
        return this.background(
            color = color,
            shape = shape
        )
    }
}
