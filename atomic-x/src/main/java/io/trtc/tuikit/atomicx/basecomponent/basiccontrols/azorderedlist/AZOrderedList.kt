package io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.pinyinhelper.Pinyin
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private data class AZGroup<T>(val letter: String, val items: List<AZOrderedListItem<T>>)

data class AZOrderedListItem<T>(
    val key: String,
    val label: String,
    val avatarUrl: Any? = null,
    val extraData: T
)

data class AZOrderedListConfig(
    val showIndexBar: Boolean = true
)

@Composable
fun <T> AZOrderedList(
    modifier: Modifier = Modifier,
    dataSource: List<AZOrderedListItem<T>>,
    config: AZOrderedListConfig = AZOrderedListConfig(),
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    onItemClick: (AZOrderedListItem<T>) -> Unit
) {
    val colors = LocalTheme.current.colors
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val groups = remember(dataSource) { groupAndSort(dataSource) }
    val indexLetters = remember(groups) { groups.map { it.letter } }

    var isIndexBarDragging by remember { mutableStateOf(false) }
    var currentIndexBarLetter by remember { mutableStateOf<String?>(groups.firstOrNull()?.letter) }

    val groupRanges = remember(groups) {
        var currentIndex = 0
        val ranges = mutableListOf<Pair<String, Int>>()
        groups.forEach { group ->
            val headerIndex = currentIndex
            ranges.add(group.letter to headerIndex)
            currentIndex += 1 + group.items.size
        }
        ranges.toMap()
    }

    LaunchedEffect(listState, dataSource) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collectLatest { firstVisible ->
                var itemIndex = 0
                var letter: String? = null
                for (group in groups) {
                    val groupStart = itemIndex
                    val groupEnd = itemIndex + group.items.size
                    if (firstVisible in groupStart..groupEnd) {
                        letter = group.letter
                        break
                    }
                    itemIndex += 1 + group.items.size
                }
                currentIndexBarLetter = letter
            }
    }

    Box(
        modifier = modifier
            .background(color = colors.bgColorOperate)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {

            item {
                header()
            }

            groups.forEach { group ->
                stickyHeader(key = "az_header_${group.letter}") {
                    DefaultAZHeader(letter = group.letter)
                }

                itemsIndexed(
                    items = group.items,
                    key = { _, item -> "az_${item.key}" }
                ) { _, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(color = colors.bgColorOperate)
                            .padding(horizontal = 16.dp)
                            .clickable { onItemClick(item) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(url = item.avatarUrl, name = item.label) {
                            onItemClick(item)
                        }
                        Spacer(modifier = Modifier.size(13.dp))
                        Text(
                            text = item.label,
                            color = colors.textColorPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                footer()
            }
        }

        if (indexLetters.isNotEmpty() && config.showIndexBar) {
            IndexBar(
                letters = indexLetters,
                currentLetter = if (isIndexBarDragging) null else currentIndexBarLetter,
                onLetterSelected = { letter ->
                    groupRanges[letter]?.let { index ->
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    }
                },
                onLetterPressed = { _ -> },
                onLetterReleased = { },
                onDragStart = {
                    isIndexBarDragging = true
                },
                onDragEnd = {
                    isIndexBarDragging = false
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp, top = 16.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun DefaultAZHeader(letter: String) {
    val colors = LocalTheme.current.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.bgColorDialog)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = letter,
            fontSize = 14.sp,
            fontWeight = FontWeight.W600,
            color = colors.textColorPrimary
        )
    }
}


private fun getFirstLetter(name: String): String {
    if (name.isEmpty()) return "#"
    val firstChar = name[0]
    if (firstChar.isLetter() && firstChar.code < 128) {
        return firstChar.uppercaseChar().toString()
    }
    if (firstChar.isDigit()) return "#"
    if (Pinyin.isChinese(firstChar)) {
        val pinyin = Pinyin.toPinyin(firstChar)
        return if (pinyin.isNotEmpty()) pinyin[0].uppercaseChar().toString() else "#"
    }
    return "#"
}

private fun <T> groupAndSort(items: List<AZOrderedListItem<T>>): List<AZGroup<T>> {
    val grouped = items.groupBy { item -> getFirstLetter(item.label) }
    val sortedGroups = grouped.map { (letter, groupItems) ->
        val sorted = groupItems.sortedBy { it.label.lowercase() }
        AZGroup(letter, sorted)
    }
    return sortedGroups.sortedWith { g1, g2 ->
        when {
            g1.letter == "#" && g2.letter != "#" -> 1
            g1.letter != "#" && g2.letter == "#" -> -1
            else -> g1.letter.compareTo(g2.letter)
        }
    }
}
