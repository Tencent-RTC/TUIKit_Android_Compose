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
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.rememberEmojiKeyToName
import io.trtc.tuikit.atomicx.messagelist.utils.MessageUtils
import io.trtc.tuikit.atomicx.search.utils.displayName
import io.trtc.tuikit.atomicx.search.viewmodels.SearchMessageViewModel
import io.trtc.tuikit.atomicxcore.api.search.MessageSearchResultItem
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SearchMessageScreen(
    keywords: String,
    onBack: () -> Unit,
    onMessageInConversationClick: (MessageSearchResultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors
    val messageViewModel: SearchMessageViewModel = viewModel()

    var searchQuery by remember { mutableStateOf(keywords) }
    val isSearching by messageViewModel.isSearching.collectAsState()
    val messageResults by messageViewModel.messageSearchResults.collectAsState()
    val listState = rememberLazyListState()
    var isLoadingMoreRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        messageViewModel.updateSearchQuery(searchQuery)
    }

    LaunchedEffect(listState, isSearching, messageResults.size) {
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            Pair(total > 0 && lastVisible >= total - 1, total)
        }.distinctUntilChanged().collect { (endReached, _) ->
            if (endReached && !isSearching && !isLoadingMoreRequested && messageResults.isNotEmpty()) {
                isLoadingMoreRequested = true
                messageViewModel.searchMore()
            }
        }
    }

    LaunchedEffect(messageResults.size) {
        isLoadingMoreRequested = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
    ) {
        SubScreenSearchHeader(
            query = searchQuery,
            onQueryChange = { searchQuery = it; messageViewModel.updateSearchQuery(it) },
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
        } else if (messageResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.search_cannot_found_chat_record),
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
                        text = stringResource(R.string.search_category_chat_record),
                        fontSize = 24.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColorPrimary,
                    )
                }
                items(messageResults) { conversation ->
                    ConversationResultItem(
                        conversation = conversation,
                        keyword = searchQuery,
                        onClick = { onMessageInConversationClick(conversation) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationResultItem(
    conversation: MessageSearchResultItem,
    keyword: String,
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
            url = conversation.conversationAvatarURL,
            name = conversation.displayName,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            HighlightTitle(
                text = conversation.displayName,
                keywords = keyword
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (conversation.messageCount > 1) {
                HighlightSecondary(
                    textList = listOf(
                        HighlightTextItem(
                            text = stringResource(
                                R.string.search_related_chat_record_count,
                                conversation.messageCount
                            )
                        )
                    )
                )
            } else {
                HighlightSecondary(
                    textList = listOf(
                        HighlightTextItem(text = "${conversation.displayName}: "),
                        HighlightTextItem(
                            text = rememberEmojiKeyToName(MessageUtils.getMessageAbstract(conversation.messageList.first())),
                            keywords = keyword
                        )
                    )
                )
            }

        }

    }
}
