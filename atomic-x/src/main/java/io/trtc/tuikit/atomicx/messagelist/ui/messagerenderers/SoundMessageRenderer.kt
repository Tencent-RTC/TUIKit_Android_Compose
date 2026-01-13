package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messagelist.ui.LocalAudioPlayingState
import io.trtc.tuikit.atomicx.messagelist.ui.LocalInteractionHandler
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.isMessagePlaying
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageReadReceiptIndicator
import io.trtc.tuikit.atomicx.messagelist.utils.DateTimeUtils
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import kotlin.math.roundToInt

class SoundMessageRenderer : MessageRenderer {
    @Composable
    override fun Render(message: MessageInfo) {
        val colors = LocalTheme.current.colors
        val messageInteraction = LocalInteractionHandler.current
        val audioPlayingState by LocalAudioPlayingState.current
        val isCurrentMessagePlaying = audioPlayingState.isMessagePlaying(message.msgID ?: "")

        LaunchedEffect(Unit) {
            messageInteraction.onRendered()
        }

        Box(modifier = Modifier.padding(bottom = if (message.needReadReceipt) 6.dp else 0.dp)) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                messageInteraction.onTap()
                            },
                            onLongPress = {
                                messageInteraction.onLongPress()
                            }
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val vector = if (!isCurrentMessagePlaying)
                    Icons.Default.PlayArrow
                else
                    Icons.Default.Pause

                val contentColor = if (!message.isSelf) colors.textColorPrimary else colors.textColorAntiPrimary

                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = vector,
                    contentDescription = "",
                    tint = contentColor
                )

                repeat(3) {
                    Icon(
                        modifier = Modifier.height(21.dp),
                        painter = painterResource(R.drawable.message_list_voice_sound_waves),
                        tint = contentColor,
                        contentDescription = ""
                    )
                }

                val displayTime = if (isCurrentMessagePlaying && audioPlayingState.playPosition > 0) {
                    (audioPlayingState.playPosition / 1000f).roundToInt()
                } else {
                    message.messageBody?.soundDuration ?: 0
                }

                Text(
                    text = DateTimeUtils.formatSmartTime(displayTime),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = contentColor,
                    maxLines = 1
                )
            }

            MessageReadReceiptIndicator(
                message = message,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = 4.dp)
                    .padding(end = 8.dp)
            )
        }
    }
}