package io.trtc.tuikit.atomicx.messagelist.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.EmojiManager
import io.trtc.tuikit.atomicxcore.api.login.UserProfile
import io.trtc.tuikit.atomicxcore.api.message.MessageReaction

@Composable
fun ReactionDetailSheet(
    isVisible: Boolean,
    reactionList: List<MessageReaction>,
    currentUserID: String?,
    onDismiss: () -> Unit,
    onFetchUsers: (String) -> Unit,
    onRemoveReaction: (String) -> Unit
) {
    if (!isVisible || reactionList.isEmpty()) return

    val colors = LocalTheme.current.colors
    val context = LocalContext.current

    EmojiManager.initialize(context)

    var selectedReactionID by remember { mutableStateOf(reactionList.firstOrNull()?.reactionID ?: "") }

    LaunchedEffect(selectedReactionID) {
        if (selectedReactionID.isNotEmpty()) {
            onFetchUsers(selectedReactionID)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            verticalArrangement = Arrangement.Bottom
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = colors.bgColorOperate
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.strokeColorPrimary)
                        )
                    }

                    ReactionTabRow(
                        reactionList = reactionList,
                        selectedReactionID = selectedReactionID,
                        onTabSelected = { selectedReactionID = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val selectedReaction = reactionList.find { it.reactionID == selectedReactionID }
                    if (selectedReaction != null) {
                        ReactionUserList(
                            modifier = Modifier.weight(1f),
                            reaction = selectedReaction,
                            currentUserID = currentUserID,
                            onRemoveReaction = { onRemoveReaction(selectedReactionID) }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ReactionTabRow(
    reactionList: List<MessageReaction>,
    selectedReactionID: String,
    onTabSelected: (String) -> Unit
) {
    val colors = LocalTheme.current.colors

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reactionList) { reaction ->
            val isSelected = reaction.reactionID == selectedReactionID
            val emoji = EmojiManager.findEmojiByKey(reaction.reactionID)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        color = if (isSelected) colors.buttonColorPrimaryDefault.copy(alpha = 0.1f)
                        else colors.bgColorInput
                    )
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) colors.buttonColorPrimaryDefault else colors.bgColorInput,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onTabSelected(reaction.reactionID) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (emoji != null) {
                    AsyncImage(
                        model = emoji.emojiUrl,
                        contentDescription = emoji.emojiName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = reaction.totalUserCount.toString(),
                    fontSize = 14.sp,
                    color = if (isSelected) colors.buttonColorPrimaryDefault else colors.textColorSecondary
                )
            }
        }
    }
}

@Composable
private fun ReactionUserList(
    modifier: Modifier = Modifier,
    reaction: MessageReaction,
    currentUserID: String?,
    onRemoveReaction: () -> Unit
) {
    val colors = LocalTheme.current.colors

    val sortedUsers = remember(reaction.partialUserList, currentUserID) {
        val users = reaction.partialUserList.toMutableList()
        if (reaction.reactedByMyself && currentUserID != null) {
            val selfIndex = users.indexOfFirst { it.userID == currentUserID }
            if (selfIndex > 0) {
                val selfUser = users.removeAt(selfIndex)
                users.add(0, selfUser)
            }
        }
        users
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sortedUsers) { user ->
            val isSelf = user.userID == currentUserID && reaction.reactedByMyself
            ReactionUserItem(
                user = user,
                isSelf = isSelf,
                onClick = {
                    if (isSelf) {
                        onRemoveReaction()
                    }
                }
            )
        }
    }
}

@Composable
private fun ReactionUserItem(
    user: UserProfile,
    isSelf: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isSelf) { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            url = user.avatarURL,
            name = user.nickname?.firstOrNull()?.toString() ?: user.userID?.firstOrNull()?.toString() ?: "",
            size = AvatarSize.S
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = user.nickname ?: user.userID ?: "",
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                color = colors.textColorPrimary
            )
            if (isSelf) {
                Text(
                    text = stringResource(R.string.message_list_reaction_tap_to_delete),
                    fontSize = 12.sp,
                    color = colors.textColorTertiary
                )
            }
        }
    }
}
