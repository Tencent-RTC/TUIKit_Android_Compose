package io.trtc.tuikit.atomicx.videoplayer.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun VideoPlayer(modifier: Modifier = Modifier, data: Uri, width: Float, height: Float) {
    val context = LocalContext.current
    val player by remember {
        mutableStateOf(ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(data))
            prepare()
        })
    }
    var coverSurface by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        player.listen { events ->
            if (events.contains(Player.EVENT_RENDERED_FIRST_FRAME)) {
                coverSurface = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.stop()
            player.release()
        }
    }
    var srcSizeDp = Size(width = width, height = height)
    Box(modifier) {
        PlayerSurface(
            player = player,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .layout { measurable, constraints ->
                    val srcSizePx =
                        with(density) {
                            Size(
                                Dp(srcSizeDp.width).toPx(),
                                Dp(srcSizeDp.height).toPx()
                            )
                        }
                    val dstSizePx =
                        Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
                    val scaleFactor = ContentScale.Fit.computeScaleFactor(srcSizePx, dstSizePx)
                    val placeable =
                        measurable.measure(
                            constraints.copy(
                                maxWidth = (srcSizePx.width * scaleFactor.scaleX).roundToInt(),
                                maxHeight = (srcSizePx.height * scaleFactor.scaleY).roundToInt(),
                            )
                        )
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                }

        )

        if (coverSurface) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            MinimalControls(player)
        }
    }
}

@Composable
internal fun MinimalControls(player: Player, modifier: Modifier = Modifier) {
    val graySemiTransparentBackground = Color.Gray.copy(alpha = 0.1f)
    val modifierForIconButton = modifier
        .size(48.dp)
        .background(graySemiTransparentBackground, CircleShape)

    var currentTime by remember { mutableLongStateOf(0) }
    var totalTime by remember { mutableLongStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            totalTime = player.duration
            currentTime = player.currentPosition
            delay(100)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayPauseButton(player, modifierForIconButton)
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatTime(currentTime),
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1
        )
        ProgressBar(
            modifier = Modifier.weight(1f),
            player = player
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatTime(totalTime),
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}

@Composable
fun PlayPauseButton(player: Player, modifier: Modifier = Modifier) {
    var isPlaying by remember { mutableStateOf(player.isPlaying) }


    LaunchedEffect(Unit) {
        player.listen { events ->
            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED)
            ) {
                isPlaying = player.isPlaying
            }

            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                if (player.playbackState == Player.STATE_ENDED) {
                    player.seekTo(0)
                    player.pause()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .clickable {
                if (isPlaying) {
                    player.pause()
                } else {
                    if (player.playbackState == Player.STATE_ENDED) {
                        player.seekTo(0)
                    }
                    player.play()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    player: Player
) {

    var dragProgress by remember { mutableStateOf(0f) }

    var isDragging by remember { mutableStateOf(false) }

    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    var wasPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableLongStateOf(0) }
    var duration by remember { mutableLongStateOf(0) }


    LaunchedEffect(Unit) {
        while (true) {
            duration = player.duration
            currentTime = player.currentPosition
            delay(16)
        }
    }


    val progress = if (isDragging) {
        dragProgress
    } else if (duration > 0) {
        (currentTime.toFloat() / duration).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .height(48.dp)
            .padding(horizontal = 8.dp)
            .onSizeChanged {
                boxSize = it
            }
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.Center)
                .background(Color.Gray.copy(alpha = 0.3f))
        )


        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(4.dp)
                .align(Alignment.CenterStart)
                .background(Color.White)
        )


        Box(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(
                        x = (progress * boxSize.width).toInt().coerceIn(0, boxSize.width),
                        y = 0
                    )
                }
                .background(Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true

                            wasPlaying = player.isPlaying

                            player.pause()

                            dragProgress = if (duration > 0) {
                                (currentTime.toFloat() / duration).coerceIn(0f, 1f)
                            } else 0f
                        },
                        onDragEnd = {
                            isDragging = false

                            player.seekTo((dragProgress * duration).toLong())

                            if (wasPlaying) {
                                player.play()
                            }
                        },
                        onDrag = { change, _ ->

                            val newProgress = (change.position.x / boxSize.width).coerceIn(0f, 1f)
                            dragProgress = newProgress

                            player.seekTo((newProgress * duration).toLong())
                        }
                    )
                }
        )


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (!isDragging) {
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            player.seekTo((newProgress * duration).toLong())
                        }
                    }
                }
        )
    }
}

fun formatTime(timeMs: Long): String {
    if (timeMs <= 0) return "00:00"

    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}