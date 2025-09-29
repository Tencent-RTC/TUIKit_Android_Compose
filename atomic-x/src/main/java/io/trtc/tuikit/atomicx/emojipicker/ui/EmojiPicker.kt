package io.trtc.tuikit.atomicx.emojipicker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.Colors
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.EmojiManager
import io.trtc.tuikit.atomicx.emojipicker.RecentEmojiManager
import io.trtc.tuikit.atomicx.emojipicker.model.Emoji
import io.trtc.tuikit.atomicx.emojipicker.model.EmojiGroup


@Composable
fun EmojiPicker(
    modifier: Modifier = Modifier,
    onEmojiClick: (EmojiGroup, Emoji) -> Unit,
    onDeleteClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
) {
    val colors = LocalTheme.current.colors
    val context = LocalContext.current
    EmojiManager.initialize(context)
    RecentEmojiManager.initialize(context)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.bgColorOperate)
    ) {
        val emojiGroupList = EmojiManager.emojiGroupList
        val currentEmojiGroup by remember { mutableStateOf(emojiGroupList.get(0)) }
        var selectedIndex by remember { mutableStateOf(emojiGroupList.indexOf(currentEmojiGroup)) }
        EmojiTab(selectedIndex, onSelectedChanged = {
            selectedIndex = it
        })
        HorizontalDivider(thickness = 1.dp, color = colors.bgColorBubbleReciprocal)
        EmojiGrid(
            selectedIndex = selectedIndex,
            onSendClick = onSendClick,
            onDeleteClick = onDeleteClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onEmojiClick = onEmojiClick,
            onCurrentChanged = {
                selectedIndex = it
            }
        )
    }

}

@Composable
fun EmojiTab(selectedIndex: Int = 0, onSelectedChanged: (Int) -> Unit) {
    val emojiGroupList = EmojiManager.emojiGroupList
    val colors = LocalTheme.current.colors
    LazyRow(
        modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(emojiGroupList) { index, emojiGroup ->
            AsyncImage(
                model = emojiGroup.emojiGroupIconUrl,
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        onSelectedChanged(index)
                    }
                    .background(
                        color = if (selectedIndex == index) colors.bgColorBubbleReciprocal else Colors.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    ),

                contentScale = ContentScale.Fit,
                contentDescription = ""
            )
        }
    }
}

@Composable
fun EmojiGrid(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    onDeleteClick: () -> Unit,
    onSendClick: () -> Unit,
    onEmojiClick: (EmojiGroup, Emoji) -> Unit,
    onCurrentChanged: (Int) -> Unit
) {
    val colors = LocalTheme.current.colors
    val emojiGroupList = EmojiManager.emojiGroupList
    val pagerState = rememberPagerState(selectedIndex, pageCount = { emojiGroupList.size })
    var showDetailEmoji by remember { mutableStateOf<Emoji?>(null) }
    val recentKeysState by RecentEmojiManager.recentEmojis.collectAsState(initial = emptyList())

    LaunchedEffect(selectedIndex) {
        pagerState.scrollToPage(selectedIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        onCurrentChanged(pagerState.currentPage)
    }


    HorizontalPager(state = pagerState) {
        val currentGroup = emojiGroupList[it]

        val isLittle = currentGroup.isLittleEmoji
        Box() {
            LazyVerticalGrid(
                columns = if (isLittle) GridCells.Fixed(8) else GridCells.Fixed(5),
                modifier = modifier.padding(8.dp)
            ) {
                if (isLittle && recentKeysState.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = stringResource(R.string.emoji_picker_recent),
                            color = colors.textColorSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }

                    val recentEmojis = recentKeysState.mapNotNull { key -> EmojiManager.findEmojiByKey(key) }.take(8)

                    itemsIndexed(recentEmojis) { _, emoji ->
                        EmojiItem(
                            emoji = emoji,
                            onLongPress = {
                                showDetailEmoji = emoji
                            },
                            onClick = {
                                RecentEmojiManager.updateRecentEmoji(emoji.key)
                                onEmojiClick(currentGroup, emoji)
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (isLittle) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = stringResource(R.string.emoji_picker_all),
                            color = colors.textColorSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }

                itemsIndexed(currentGroup.emojis) { _, emoji ->
                    EmojiItem(
                        emoji = emoji,
                        onLongPress = {
                            showDetailEmoji = emoji
                        },
                        onClick = {
                            if (isLittle) {
                                RecentEmojiManager.updateRecentEmoji(emoji.key)
                            }
                            onEmojiClick(currentGroup, emoji)
                        },
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                    )
                }
                if (isLittle) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
            if (isLittle) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .wrapContentSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .width(40.dp)
                            .height(30.dp)
                            .clip(CircleShape)
                            .clickable {
                                onDeleteClick()
                            }
                            .background(color = colors.buttonColorSecondaryDefault, shape = CircleShape)
                            .padding(6.dp),
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "",
                        tint = colors.textColorPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.emoji_picker_send),
                        fontSize = 14.sp,
                        lineHeight = 28.sp,
                        color = colors.textColorButton,
                        modifier = Modifier
                            .width(50.dp)
                            .height(30.dp)
                            .clip(CircleShape)
                            .clickable {
                                onSendClick()
                            }
                            .background(color = colors.buttonColorPrimaryDefault, shape = CircleShape),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    showDetailEmoji?.let {
        Popup(alignment = Alignment.TopCenter, onDismissRequest = { showDetailEmoji = null }) {
            val imageData = showDetailEmoji!!.emojiUrl

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable(indication = null, interactionSource = null) {
                        showDetailEmoji = null
                    }
                    .background(color = colors.bgColorInput, shape = RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                AsyncImage(
                    model = imageData,
                    contentDescription = showDetailEmoji!!.emojiName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun EmojiItem(
    emoji: Emoji,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageData = emoji.emojiUrl

    Box(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        AsyncImage(
            model = imageData,
            contentDescription = emoji.emojiName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth()
        )
    }

}