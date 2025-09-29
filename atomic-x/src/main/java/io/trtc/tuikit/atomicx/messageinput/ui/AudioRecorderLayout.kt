package io.trtc.tuikit.atomicx.messageinput.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

@Composable
fun AudioRecorderLayout(
    isVisible: Boolean,
    recordingState: AudioRecordingState,
    recordingDuration: Int,
    amplitude: Int,
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

    LaunchedEffect(recordingDuration) {
        if (recordingState != AudioRecordingState.IDLE && recordingDuration > 0) {
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
                        else -> colors.buttonColorPrimaryDefault
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
                            text = getFormatTime(recordingDuration)
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
                                itemsIndexed(amplitudeList) { index, height ->
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

fun getFormatTime(recordingDuration: Int): String {
    val minutes = recordingDuration / 60
    val seconds = recordingDuration % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun Modifier.rtlRotate(degrees: Float) = this.rotate(
    degrees * if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
)