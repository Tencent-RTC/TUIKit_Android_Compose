package io.trtc.tuikit.atomicx.messageinput.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun Modifier.audioRecordGesture(
    cancelThreshold: Dp = 100.dp,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onDragToCancel: (Boolean) -> Unit
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current

    var isRecording by remember { mutableStateOf(false) }
    var isDragToCancel by remember { mutableStateOf(false) }
    var startPosition by remember { mutableStateOf(Offset.Zero) }

    val cancelThresholdPx = with(LocalDensity.current) { cancelThreshold.toPx().toInt() }

    this.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                startPosition = offset
                isRecording = true
                isDragToCancel = false
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onStart()
            },
            onDrag = { change, a ->
                if (isRecording) {
                    val currentOffset = change.position
                    val dragDistance = abs(currentOffset.x - startPosition.x)
                    val shouldCancel = dragDistance > cancelThresholdPx

                    if (shouldCancel != isDragToCancel) {
                        isDragToCancel = shouldCancel
                        onDragToCancel(shouldCancel)

                        if (shouldCancel) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }
            },
            onDragEnd = {
                if (isRecording) {
                    if (isDragToCancel) {
                        onCancel()
                    } else {
                        onStop()
                    }
                }

                isRecording = false
                isDragToCancel = false
                onDragToCancel(false)
            }
        )
    }
} 