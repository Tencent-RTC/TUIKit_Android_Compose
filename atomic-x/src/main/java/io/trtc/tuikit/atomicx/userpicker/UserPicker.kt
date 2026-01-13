package io.trtc.tuikit.atomicx.userpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.IndexBar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.pinyinhelper.Pinyin
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class UserPickerData<T>(
    val key: String,
    val label: String,
    val avatarUrl: Any?,
    val extraData: T
)

data class UserPickerState<T>(val selectedItems: StateFlow<List<UserPickerData<T>>> = MutableStateFlow(emptyList()))

@Composable
fun <T> UserPicker(
    modifier: Modifier = Modifier,
    dataSource: List<UserPickerData<T>>,
    state: UserPickerState<T> = UserPickerState(),
    defaultSelectedItems: List<String>? = null,
    lockedItems: List<String>? = null,
    maxCount: Int? = null,
    onMaxCountExceed: (List<UserPickerData<T>>) -> Unit = {},
    onSelectedChanged: (List<UserPickerData<T>>) -> Unit = {},
    onReachEnd: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedKeys = remember { mutableStateListOf<String>() }
    var hasReachedEnd by remember(dataSource) { mutableStateOf(false) }

    val groupedList = remember(dataSource) { groupAndSortItems(dataSource) }
    val indexLetters = remember(groupedList) { groupedList.map { it.letter } }

    val groupRanges = remember(groupedList) {
        var currentIndex = 0
        val ranges = mutableListOf<Pair<String, IntRange>>()
        groupedList.forEach { group ->
            val startIndex = currentIndex
            val endIndex = currentIndex + group.items.size
            ranges.add(group.letter to (startIndex..endIndex))
            currentIndex += 1 + group.items.size
        }
        ranges
    }

    val letterToIndex = remember(groupRanges) {
        groupRanges.associate { (letter, range) -> letter to range.first }
    }

    var showCenterLetter by remember { mutableStateOf<String?>(null) }
    var isIndexBarDragging by remember { mutableStateOf(false) }
    var currentIndexBarLetter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(groupedList, defaultSelectedItems, lockedItems) {
        selectedKeys.clear()
        val init = LinkedHashSet<String>()
        defaultSelectedItems?.let { defaults ->
            init.addAll(defaults)
        }
        selectedKeys.addAll(init)
        val allItems = groupedList.flatMap { it.items }
        val selectedItems = selectedKeys.mapNotNull { key -> allItems.firstOrNull { it.key == key } }
        (state.selectedItems as MutableStateFlow<List<UserPickerData<T>>>).value = selectedItems
    }

    LaunchedEffect(groupedList) {
        currentIndexBarLetter = groupedList.firstOrNull()?.letter
    }

    LaunchedEffect(listState, dataSource) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collectLatest { firstVisible ->
                var itemIndex = 0
                var letter: String? = null
                for (group in groupedList) {
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

    LaunchedEffect(listState, dataSource) {
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            Pair(total, lastVisible)
        }
            .distinctUntilChanged()
            .collectLatest { (total, lastVisible) ->
                if (!hasReachedEnd && total > 0 && lastVisible >= total - 1) {
                    hasReachedEnd = true
                    onReachEnd()
                }
            }
    }

    val isSingleSelect = maxCount == 1
    val showCheckbox = !isSingleSelect

    Box(
        modifier = modifier
            .background(color = colors.bgColorOperate)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            groupedList.forEach { group ->
                stickyHeader(key = "header_${group.letter}") {
                    UserPickerGroupHeader(letter = group.letter)
                }

                itemsIndexed(
                    items = group.items,
                    key = { _, item -> "picker_${item.key}" }
                ) { _, item ->
                    val isLocked = lockedItems?.contains(item.key) == true
                    val isSelected = selectedKeys.contains(item.key)

                    val onItemClick = onItemClick@{
                        if (isLocked) return@onItemClick
                        if (isSingleSelect) {
                            selectedKeys.clear()
                            selectedKeys.add(item.key)
                            (state.selectedItems as MutableStateFlow<List<UserPickerData<T>>>).value = listOf(item)
                            coroutineScope.launch {
                                withFrameNanos { }
                                onSelectedChanged(listOf(item))
                            }
                        } else {
                            if (isSelected) {
                                selectedKeys.remove(item.key)
                                pushSelectionState(
                                    scope = coroutineScope,
                                    selectedKeys = selectedKeys,
                                    dataSource = groupedList.flatMap { it.items },
                                    state = state,
                                    onSelectedChanged = onSelectedChanged
                                )
                            } else {
                                val currentCount = selectedKeys.size
                                if (maxCount != null && currentCount >= maxCount) {
                                    val selectedItems = selectedKeys.mapNotNull { key ->
                                        dataSource.firstOrNull { it.key == key }
                                    }
                                    onMaxCountExceed(selectedItems)
                                } else {
                                    selectedKeys.add(item.key)
                                    pushSelectionState(
                                        scope = coroutineScope,
                                        selectedKeys = selectedKeys,
                                        dataSource = groupedList.flatMap { it.items },
                                        state = state,
                                        onSelectedChanged = onSelectedChanged
                                    )
                                }
                            }
                        }
                    }

                    UserPickerItemRow(
                        item = item,
                        isSelected = isSelected,
                        isLocked = isLocked,
                        showCheckbox = showCheckbox,
                        onClick = onItemClick
                    )
                }
            }
        }

        if (indexLetters.isNotEmpty()) {
            IndexBar(
                letters = indexLetters,
                currentLetter = if (isIndexBarDragging) null else currentIndexBarLetter,
                onLetterSelected = { letter ->
                    letterToIndex[letter]?.let { index ->
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    }
                },
                onLetterPressed = { letter ->
                    showCenterLetter = letter
                },
                onLetterReleased = {
                    coroutineScope.launch {
                        delay(150)
                        showCenterLetter = null
                    }
                },
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

        showCenterLetter?.let { letter ->
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = colors.bgColorBubbleReciprocal.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColorPrimary
                )
            }
        }
    }
}

@Composable
private fun UserPickerGroupHeader(letter: String) {
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

private data class PickerGroup<T>(val letter: String, val items: List<UserPickerData<T>>)

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

private fun <T> groupAndSortItems(items: List<UserPickerData<T>>): List<PickerGroup<T>> {
    val grouped = items.groupBy { item -> getFirstLetter(item.label) }
    val sortedGroups = grouped.map { (letter, inGroup) ->
        val sorted = inGroup.sortedBy { it.label.lowercase() }
        PickerGroup(letter, sorted)
    }
    return sortedGroups.sortedWith { g1, g2 ->
        when {
            g1.letter == "#" && g2.letter != "#" -> 1
            g1.letter != "#" && g2.letter == "#" -> -1
            else -> g1.letter.compareTo(g2.letter)
        }
    }
}

@Composable
private fun <T> UserPickerItemRow(
    item: UserPickerData<T>,
    isSelected: Boolean,
    isLocked: Boolean,
    showCheckbox: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(enabled = !isLocked) { onClick() }
            .background(color = colors.bgColorOperate)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(url = item.avatarUrl, name = item.label) {
            if (!isLocked) onClick()
        }
        Spacer(modifier = Modifier.width(13.dp))
        Text(
            text = item.label,
            color = colors.textColorPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        if (showCheckbox) {
            SelectionCheckBox(
                checked = isSelected,
                enabled = !isLocked,
                onCheckedChange = { _ ->
                    if (!isLocked) onClick()
                }
            )
            Spacer(modifier = Modifier.width(26.dp))
        }
    }
}

private fun <T> pushSelectionState(
    scope: CoroutineScope,
    selectedKeys: List<String>,
    dataSource: List<UserPickerData<T>>,
    state: UserPickerState<T>,
    onSelectedChanged: (List<UserPickerData<T>>) -> Unit
) {
    val selectedItems = selectedKeys.mapNotNull { key ->
        dataSource.firstOrNull { it.key == key }
    }
    scope.launch {
        (state.selectedItems as MutableStateFlow<List<UserPickerData<T>>>).value = selectedItems
        withFrameNanos { }
        onSelectedChanged(selectedItems)
    }
}

@Composable
fun SelectionCheckBox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = LocalTheme.current.colors
    Box(
        modifier = modifier
            .size(16.dp)
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = if (enabled) colors.textColorLink else colors.textColorLinkDisabled,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (enabled) colors.textColorButton else colors.textColorButtonDisabled,
                    modifier = Modifier.size(12.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(
                        width = 1.dp,
                        color = colors.scrollbarColorDefault,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!enabled) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = colors.textColorButtonDisabled,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}