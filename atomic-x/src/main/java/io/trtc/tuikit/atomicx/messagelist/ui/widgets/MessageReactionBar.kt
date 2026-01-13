package io.trtc.tuikit.atomicx.messagelist.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.EmojiManager
import io.trtc.tuikit.atomicxcore.api.message.MessageReaction

private const val MAX_DISPLAY_REACTIONS = 5

@Composable
fun MessageReactionBar(
    modifier: Modifier = Modifier,
    reactionList: List<MessageReaction>,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    val context = LocalContext.current

    EmojiManager.initialize(context)

    if (reactionList.isEmpty()) return

    val displayReactions = reactionList.take(MAX_DISPLAY_REACTIONS)
    val hasMore = reactionList.size > MAX_DISPLAY_REACTIONS

    Row(
        modifier = modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(51.dp))
                .background(color = colors.bgColorBubbleReciprocal)
                .border(
                    width = 1.dp,
                    color = colors.strokeColorPrimary,
                    shape = RoundedCornerShape(51.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 9.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayReactions.forEach { reaction ->
                ReactionItem(
                    reaction = reaction,
                    showCount = displayReactions.size == 1
                )
            }

            if (hasMore) {
                Text(
                    text = "...",
                    fontSize = 13.sp,
                    color = colors.textColorTertiary
                )
            }

            if (displayReactions.size > 1 || hasMore) {
                val totalCount = reactionList.sumOf { it.totalUserCount.toInt() }
                Text(
                    text = totalCount.toString(),
                    fontSize = 13.sp,
                    color = colors.textColorTertiary
                )
            }
        }
    }
}

@Composable
private fun ReactionItem(
    reaction: MessageReaction,
    showCount: Boolean = false
) {
    val colors = LocalTheme.current.colors
    val emoji = EmojiManager.findEmojiByKey(reaction.reactionID)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (emoji != null) {
            AsyncImage(
                model = emoji.emojiUrl,
                contentDescription = emoji.emojiName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(16.dp)
            )
        }

        if (showCount && reaction.totalUserCount > 0) {
            Text(
                text = reaction.totalUserCount.toString(),
                fontSize = 13.sp,
                color = colors.textColorTertiary
            )
        }
    }
}
