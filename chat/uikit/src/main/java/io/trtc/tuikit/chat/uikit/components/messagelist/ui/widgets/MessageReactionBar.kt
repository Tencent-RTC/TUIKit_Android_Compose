package io.trtc.tuikit.chat.uikit.components.messagelist.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import coil3.compose.AsyncImage
import io.trtc.tuikit.chat.uikit.compose.R
import io.trtc.tuikit.chat.uikit.components.theme.LocalTheme
import io.trtc.tuikit.chat.uikit.components.emojipicker.EmojiManager
import io.trtc.tuikit.chat.uikit.components.messagelist.ui.LocalBubbleMaxWidth
import io.trtc.tuikit.atomicxcore.api.message.MessageReaction
import java.util.Locale

private const val MAX_DISPLAY_REACTIONS = 5
private const val SELF_CHIP_BG_ALPHA = 24f / 255f
private const val OTHER_CHIP_BG_ALPHA = 16f / 255f
private const val SELF_CHIP_DIVIDER_ALPHA = 64f / 255f
private const val OTHER_CHIP_DIVIDER_ALPHA = 32f / 255f

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessageReactionBar(
    modifier: Modifier = Modifier,
    reactionList: List<MessageReaction>,
    isSelf: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    EmojiManager.initialize(context)
    if (reactionList.isEmpty()) {
        return
    }
    val displayReactions = reactionList.take(MAX_DISPLAY_REACTIONS)
    val bubbleMaxWidth = LocalBubbleMaxWidth.current
    FlowRow(
        modifier = modifier
            .widthIn(max = bubbleMaxWidth)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        displayReactions.forEach { reaction ->
            ReactionChip(reaction = reaction, isSelf = isSelf)
        }
    }
}

@Composable
private fun ReactionChip(
    reaction: MessageReaction,
    isSelf: Boolean
) {
    val colors = LocalTheme.current.colors
    val context = LocalContext.current
    val baseTextColor = if (isSelf) colors.textColorAntiPrimary else colors.textColorPrimary
    val textColor = if (isSelf) colors.textColorAntiPrimary else colors.textColorSecondary
    val chipBackground = baseTextColor.copy(alpha = if (isSelf) SELF_CHIP_BG_ALPHA else OTHER_CHIP_BG_ALPHA)
    val dividerColor = baseTextColor.copy(alpha = if (isSelf) SELF_CHIP_DIVIDER_ALPHA else OTHER_CHIP_DIVIDER_ALPHA)
    val emoji = EmojiManager.findEmojiByKey(reaction.reactionID)
    val label = buildReactionLabel(reaction, context)

    Row(
        modifier = Modifier
            .widthIn(max = 180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(chipBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (emoji != null) {
            AsyncImage(
                model = emoji.emojiUrl,
                contentDescription = emoji.emojiName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(16.dp)
            )
        }
        if (label.isNotBlank()) {
            if (emoji != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(1.dp)
                        .height(14.dp)
                        .background(dividerColor, RoundedCornerShape(1.dp))
                )
            }
            Text(
                modifier = Modifier.widthIn(max = 120.dp),
                text = label,
                fontSize = 12.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun buildReactionLabel(
    reaction: MessageReaction,
    context: android.content.Context
): String {
    val totalUserCount = reaction.totalUserCount.toInt()
    if (totalUserCount <= 0) {
        return ""
    }
    val firstUser = reaction.partialUserList.firstOrNull()
    val displayName = firstUser?.nickname?.takeIf { it.isNotBlank() }
        ?: firstUser?.userID?.takeIf { it.isNotBlank() }
    if (displayName.isNullOrBlank()) {
        return totalUserCount.toString()
    }
    return if (totalUserCount == 1) {
        displayName
    } else {
        buildMultiUserLabel(context, displayName, totalUserCount)
    }
}

private fun buildMultiUserLabel(
    context: android.content.Context,
    displayName: String,
    totalUserCount: Int
): String {
    val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0] ?: Locale.getDefault()
    val isChineseLocale = locale.language.equals(Locale.CHINESE.language, ignoreCase = true)
    val count = if (isChineseLocale) {
        totalUserCount
    } else {
        (totalUserCount - 1).coerceAtLeast(1)
    }
    val suffix = context.getString(R.string.message_list_user_count_suffix, count)
    return if (isChineseLocale) {
        displayName.trim() + suffix
    } else {
        "${displayName.trim()} $suffix"
    }
}