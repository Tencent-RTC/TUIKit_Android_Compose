package io.trtc.tuikit.atomicx.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.search.utils.displayName
import io.trtc.tuikit.atomicx.search.viewmodels.SearchGroupViewModel
import io.trtc.tuikit.atomicxcore.api.search.GroupSearchInfo
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SearchGroupScreen(
    keywords: String,
    onBack: () -> Unit,
    onGroupClick: (GroupSearchInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors
    val groupViewModel: SearchGroupViewModel = viewModel()

    var searchQuery by remember { mutableStateOf(keywords) }
    val isSearching by groupViewModel.isSearching.collectAsState()
    val groupResults by groupViewModel.groupSearchResults.collectAsState()
    val listState = rememberLazyListState()
    var isLoadingMoreRequested by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        groupViewModel.updateSearchQuery(searchQuery)
    }

    LaunchedEffect(listState, isSearching, groupResults.size) {
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            Pair(total > 0 && lastVisible >= total - 1, total)
        }.distinctUntilChanged().collect { (endReached, _) ->
            if (endReached && !isSearching && !isLoadingMoreRequested && groupResults.isNotEmpty()) {
                isLoadingMoreRequested = true
                groupViewModel.searchMore()
            }
        }
    }

    LaunchedEffect(groupResults.size) {
        isLoadingMoreRequested = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
    ) {
        SubScreenSearchHeader(
            query = searchQuery,
            onQueryChange = { searchQuery = it; groupViewModel.updateSearchQuery(it) },
            onBack = onBack,
        )

        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        } else if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {

            }
        } else if (groupResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.search_cannot_found_group),
                    color = colors.textColorSecondary,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                state = listState
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(R.string.search_category_group),
                        fontSize = 24.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColorPrimary,
                    )
                }
                items(groupResults) { group ->
                    GroupResultItem(
                        result = group,
                        keywords = searchQuery,
                        onClick = { onGroupClick(group) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupResultItem(
    result: GroupSearchInfo,
    keywords: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bgColorOperate)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            url = result.groupAvatarURL,
            name = result.displayName,
            size = AvatarSize.L
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            HighlightTitle(
                text = result.displayName,
                keywords = keywords
            )

            Spacer(modifier = Modifier.height(4.dp))

            HighlightSecondary(
                textList = listOf(
                    HighlightTextItem(text = "${stringResource(R.string.search_group_id)}: "),
                    HighlightTextItem(text = result.groupID, keywords = keywords)
                )
            )

        }
    }
}