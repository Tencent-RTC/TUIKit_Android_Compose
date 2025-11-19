package io.trtc.tuikit.atomicx.audiorecorder.audiorecorderimpl

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import io.trtc.tuikit.atomicx.audiorecorder.AudioRecorderViewConfig
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.audiorecorder.AudioRecorder
import io.trtc.tuikit.atomicx.audiorecorder.AudioRecorderListener
import io.trtc.tuikit.atomicx.audiorecorder.ResultCode
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.basecomponent.utils.ContextProvider
import io.trtc.tuikit.atomicx.messageinput.ui.AudioRecordingState
import kotlin.math.abs

@Composable
internal fun AudioRecorderViewImpl(
    modifier: Modifier = Modifier,
    config: AudioRecorderViewConfig,
    onCompleted: (path: String?, duration: Int) -> Unit
) {
    val colors = LocalTheme.current.colors

    val resolvedPrimary: Color = config.primaryColor?.takeIf { it.isNotBlank() }?.let { parseColorOrNull(it) } ?: colors.buttonColorPrimaryDefault
    val resolvedBackground: Color = config.backgroundColor?.takeIf { it.isNotBlank() }?.let { parseColorOrNull(it) } ?: colors.bgColorOperate
    val resolvedHeight = 120.dp

    var recordingState by remember { mutableStateOf(AudioRecordingState.IDLE) }
    var isVisible by remember { mutableStateOf(false) }

    val currentTimeMs by AudioRecorder.currentTimeMs.collectAsState(initial = 0)
    val currentPower by AudioRecorder.currentPower.collectAsState(initial = 0)

    fun startRecording() {
        isVisible = true
        recordingState = AudioRecordingState.RECORDING
        AudioRecorder.startRecord(
            enableAIDeNoise = config.enableAIDeNoise,
            minDurationMs = config.minDurationMs,
            maxDurationMs = config.maxDurationMs,
            listener = object : AudioRecorderListener {
            override fun onCompleted(resultCode: ResultCode, path: String?, durationMs: Int) {
                recordingState = AudioRecordingState.IDLE
                isVisible = false
                var finalPath = path
                if (resultCode == ResultCode.ERROR_LESS_THAN_MIN_DURATION) {
                    Toast.makeText(ContextProvider.appContext, R.string.audio_recorder_less_than_min_time, Toast.LENGTH_SHORT).show()
                    finalPath = null
                }

                if (resultCode == ResultCode.SUCCESS_EXCEED_MAX_DURATION) {
                    Toast.makeText(ContextProvider.appContext, R.string.audio_record_time_limit_reached, Toast.LENGTH_SHORT).show()
                }

                if (resultCode.code < 0) {
                    finalPath = null;
                }

                onCompleted(finalPath, durationMs)
            }
        })
    }

    fun stopRecording() {
        isVisible = false
        AudioRecorder.stopRecord()
    }

    fun cancelRecording() {
        isVisible = false
        recordingState = AudioRecordingState.IDLE
        AudioRecorder.cancelRecord()
    }

    fun updateDragToCancel(shouldCancel: Boolean) {
        if (recordingState == AudioRecordingState.IDLE) return
        recordingState = if (shouldCancel) AudioRecordingState.READY_TO_CANCEL else AudioRecordingState.RECORDING
    }

    Box(
        modifier = modifier
            .background(Color.Transparent)
            .audioRecordGesture(
                onStart = { startRecording() },
                onStop = { stopRecording() },
                onCancel = { cancelRecording() },
                onDragToCancel = { updateDragToCancel(it) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isVisible) {
            val painter = config.iconResId?.let { painterResource(it) }
                ?: painterResource(R.drawable.message_input_microphone_icon)
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painter,
                tint = resolvedPrimary,
                contentDescription = "microphone"
            )
        }
    }

    if (isVisible) {
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(focusable = true, dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(resolvedBackground)
                    .height(resolvedHeight)
                    .audioRecordGesture(
                        onStart = {},
                        onStop = { stopRecording() },
                        onCancel = { cancelRecording() },
                        onDragToCancel = { updateDragToCancel(it) }
                    )
            ) {
                AudioRecorderLayout(
                    isVisible = true,
                    recordingState = recordingState,
                    recordingTimeSecond = currentTimeMs / 1000,
                    amplitude = currentPower,
                    primaryColor = resolvedPrimary
                )
            }
        }
    }
}

@Composable
internal fun AudioRecorderLayout(
    isVisible: Boolean,
    recordingState: AudioRecordingState,
    recordingTimeSecond: Int,
    amplitude: Int,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors

    val amplitudeList = remember { mutableStateListOf<Float>() }
    val listState = rememberLazyListState()

    LaunchedEffect(recordingState) {
        if (recordingState == AudioRecordingState.IDLE) {
            amplitudeList.clear()
        }
    }

    LaunchedEffect(recordingTimeSecond) {
        if (recordingState != AudioRecordingState.IDLE && recordingTimeSecond > 0) {
            val normalizedHeight = (6 + amplitude * 18 / 100f).coerceIn(6f, 24f)
            amplitudeList.add(0, normalizedHeight)
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
        modifier = modifier.navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = when (recordingState) {
                        AudioRecordingState.RECORDING -> stringResource(R.string.message_input_swipe_cancel_hint)
                        AudioRecordingState.READY_TO_CANCEL -> stringResource(R.string.message_input_release_cancel_hint)
                        AudioRecordingState.TOO_SHORT -> stringResource(R.string.message_input_recording_too_short)
                        else -> stringResource(R.string.message_input_recording_status)
                    },
                    color = colors.textColorSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    val iconColor = when (recordingState) {
                        AudioRecordingState.READY_TO_CANCEL -> colors.textColorError
                        else -> primaryColor
                    }

                    val offsetX by animateDpAsState(
                        targetValue = if (recordingState == AudioRecordingState.READY_TO_CANCEL) (-16).dp else 0.dp,
                        animationSpec = tween(durationMillis = 200)
                    )

                    val rotation by animateFloatAsState(
                        targetValue = if (recordingState == AudioRecordingState.READY_TO_CANCEL) -45f else 0f,
                        animationSpec = tween(durationMillis = 200)
                    )

                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .offset(offsetX)
                            .rtlRotate(rotation),
                        painter = painterResource(R.drawable.message_input_audio_remove_icon),
                        tint = iconColor,
                        contentDescription = ""
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(color = iconColor, shape = CircleShape)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 14.sp,
                            color = colors.textColorButton,
                            fontWeight = FontWeight.W600,
                            text = getFormatTime(recordingTimeSecond)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            LazyRow(
                                state = listState,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                reverseLayout = false
                            ) {
                                itemsIndexed(amplitudeList) { _, height ->
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(height.dp)
                                            .background(
                                                color = colors.textColorButton,
                                                shape = RoundedCornerShape(1.5.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseColorOrNull(hex: String): Color? = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { null }

private fun getFormatTime(recordingDuration: Int): String {
    val minutes = recordingDuration / 60
    val seconds = recordingDuration % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
private fun Modifier.rtlRotate(degrees: Float) = this.rotate(
    degrees * if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
)

@Composable
internal fun Modifier.audioRecordGesture(
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
            onDrag = { change, _ ->
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
                    if (isDragToCancel) onCancel() else onStop()
                }
                isRecording = false
                isDragToCancel = false
                onDragToCancel(false)
            }
        )
    }
}
