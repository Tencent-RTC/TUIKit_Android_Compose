package io.trtc.tuikit.atomicx.search.ui

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.rememberEmojiKeyToName
import io.trtc.tuikit.atomicx.messagelist.utils.MessageUtils
import io.trtc.tuikit.atomicx.search.utils.displayName
import io.trtc.tuikit.atomicx.search.utils.userAvatarURL
import io.trtc.tuikit.atomicx.search.viewmodels.SearchAllViewModel
import io.trtc.tuikit.atomicx.search.viewmodels.SearchCategory
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.atomicxcore.api.search.FriendSearchInfo
import io.trtc.tuikit.atomicxcore.api.search.GroupSearchInfo
import io.trtc.tuikit.atomicxcore.api.search.MessageSearchResultItem
import io.trtc.tuikit.atomicxcore.api.search.SearchType


enum class SearchFlowStep {
    GLOBAL_SEARCH,
    CONTACT_DETAIL,
    GROUP_DETAIL,
    MESSAGE_DETAIL,
    MESSAGE_IN_CONVERSATION_DETAIL
}

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onContactSelect: (FriendSearchInfo) -> Unit = {},
    onGroupSelect: (GroupSearchInfo) -> Unit = {},
    onConversationSelect: (MessageSearchResultItem) -> Unit = {},
    onMessageSelect: (MessageInfo) -> Unit = {}
) {
    val colors = LocalTheme.current.colors
    val globalViewModel: SearchAllViewModel = viewModel()

    var searchAllQuery by remember { mutableStateOf("") }
    val isSearching by globalViewModel.isSearching.collectAsState()
    val searchCategories by globalViewModel.searchCategories.collectAsState()

    var currentFlowStep by remember { mutableStateOf(SearchFlowStep.GLOBAL_SEARCH) }
    var prevFlowStep by remember { mutableStateOf(SearchFlowStep.GLOBAL_SEARCH) }
    var selectedConversation by remember { mutableStateOf<MessageSearchResultItem?>(null) }

    Column(
        modifier = modifier
            .background(colors.bgColorOperate)
            .fillMaxSize()
    ) {
        Crossfade(
            targetState = currentFlowStep,
        ) { targetStep ->
            when (targetStep) {
                SearchFlowStep.GLOBAL_SEARCH -> {
                    GlobalSearchPage(
                        searchAllQuery = searchAllQuery,
                        onSearchQueryChange = {
                            searchAllQuery = it
                            globalViewModel.updateSearchQuery(it)
                        },
                        onBack = onBack,
                        isSearching = isSearching,
                        searchCategories = searchCategories,
                        onResultClick = { resultInfo ->
                            when (resultInfo) {
                                is FriendSearchInfo -> {
                                    onContactSelect(resultInfo)
                                }

                                is GroupSearchInfo -> {
                                    onGroupSelect(resultInfo)
                                }

                                is MessageSearchResultItem -> {
                                    selectedConversation = resultInfo
                                    prevFlowStep = currentFlowStep
                                    currentFlowStep = SearchFlowStep.MESSAGE_IN_CONVERSATION_DETAIL
                                }
                            }
                        },
                        onShowMore = { searchType ->
                            when (searchType) {
                                SearchType.Friend -> {
                                    prevFlowStep = currentFlowStep
                                    currentFlowStep = SearchFlowStep.CONTACT_DETAIL
                                }

                                SearchType.Group -> {
                                    prevFlowStep = currentFlowStep
                                    currentFlowStep = SearchFlowStep.GROUP_DETAIL
                                }

                                SearchType.Message -> {
                                    prevFlowStep = currentFlowStep
                                    currentFlowStep = SearchFlowStep.MESSAGE_DETAIL
                                }

                                else -> {
                                }
                            }
                        }
                    )
                }

                SearchFlowStep.CONTACT_DETAIL -> {
                    SearchContactScreen(
                        keywords = searchAllQuery,
                        onBack = {
                            prevFlowStep = currentFlowStep
                            currentFlowStep = SearchFlowStep.GLOBAL_SEARCH
                            selectedConversation = null
                        },
                        onContactClick = { contact ->
                            onContactSelect(contact)
                        }
                    )
                }

                SearchFlowStep.GROUP_DETAIL -> {
                    SearchGroupScreen(
                        keywords = searchAllQuery,
                        onBack = {
                            prevFlowStep = currentFlowStep
                            currentFlowStep = SearchFlowStep.GLOBAL_SEARCH
                            selectedConversation = null
                        },
                        onGroupClick = { group ->
                            onGroupSelect(group)
                        }
                    )
                }

                SearchFlowStep.MESSAGE_DETAIL -> {
                    SearchMessageScreen(
                        keywords = searchAllQuery,
                        onBack = {
                            prevFlowStep = currentFlowStep
                            currentFlowStep = SearchFlowStep.GLOBAL_SEARCH
                            selectedConversation = null
                        },
                        onMessageInConversationClick = { conversation ->
                            selectedConversation = conversation
                            prevFlowStep = currentFlowStep
                            currentFlowStep = SearchFlowStep.MESSAGE_IN_CONVERSATION_DETAIL
                        }
                    )
                }

                SearchFlowStep.MESSAGE_IN_CONVERSATION_DETAIL -> {
                    selectedConversation?.let { conversation ->
                        SearchMessageInConversationScreen(
                            conversation = conversation,
                            keyword = searchAllQuery,
                            onBack = {
                                if (prevFlowStep == SearchFlowStep.GLOBAL_SEARCH) {
                                    prevFlowStep = currentFlowStep
                                    currentFlowStep = SearchFlowStep.GLOBAL_SEARCH
                                } else if (prevFlowStep == SearchFlowStep.MESSAGE_DETAIL) {
                                    prevFlowStep = currentFlowStep
                                    currentFlowStep = SearchFlowStep.MESSAGE_DETAIL
                                }
                            },
                            onMessageClick = { message ->
                                onMessageSelect(message)
                            },
                            onConversationClick = { conversation ->
                                onConversationSelect(conversation)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalSearchPage(
    searchAllQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit = {},
    isSearching: Boolean,
    searchCategories: List<SearchCategory>,
    onResultClick: (Any) -> Unit,
    onShowMore: (SearchType) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchHeader(
            query = searchAllQuery,
            onQueryChange = onSearchQueryChange,
            onBack = onBack,
            onClear = onClear
        )

        when {
            searchAllQuery.isBlank() -> {

            }

            else -> {
                GlobalSearchResults(
                    keywords = searchAllQuery,
                    categories = searchCategories,
                    isLoading = isSearching,
                    onResultClick = onResultClick,
                    onShowMore = onShowMore
                )
            }
        }
    }
}


@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit
) {
    val colors = LocalTheme.current.colors
    val fonts = LocalTheme.current.fonts
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val initialValue = remember { TextFieldValue(query, selection = TextRange(query.length)) }
        var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(initialValue) }
        LaunchedEffect(query) {
            if (query != textFieldValue.text) {
                textFieldValue = TextFieldValue(query, selection = TextRange(query.length))
            }
        }
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                if (it.text != query) onQueryChange(it.text)
            },
            textStyle = TextStyle(
                color = colors.textColorPrimary,
                fontSize = fonts.caption1Medium.size,
                fontWeight = fonts.caption1Medium.weight,
            ),
            cursorBrush = SolidColor(colors.textColorLink),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color = colors.bgColorInput, shape = RoundedCornerShape(10.dp))
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        keyboardController?.show()
                    }
                },
            enabled = true,
            readOnly = false,
            decorationBox = { innerTextField ->
                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textColorTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_search_text),
                                color = colors.textColorTertiary,
                                fontSize = fonts.caption1Medium.size,
                                fontWeight = fonts.caption1Medium.weight,
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = stringResource(R.string.base_component_cancel),
            color = colors.textColorLink,
            fontSize = fonts.caption1Medium.size,
            modifier = Modifier.clickable {
                onClear()
                onBack()
            }
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Composable
private fun GlobalSearchResults(
    keywords: String,
    categories: List<SearchCategory>,
    isLoading: Boolean,
    onResultClick: (Any) -> Unit,
    onShowMore: (SearchType) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            item {
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
            }
        } else {
            items(categories) { category ->
                SearchCategorySection(
                    keywords = keywords,
                    category = category,
                    onResultClick = onResultClick,
                    onMoreClick = { onShowMore(category.type) }
                )
            }
        }
    }
}

@Composable
private fun SearchCategorySection(
    keywords: String,
    category: SearchCategory,
    onResultClick: (Any) -> Unit,
    onMoreClick: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = when (category.type) {
                    SearchType.Group -> stringResource(R.string.search_category_group)
                    SearchType.Friend -> stringResource(R.string.search_category_contact)
                    SearchType.Message -> stringResource(R.string.search_category_chat_record)
                    else -> ""
                },
                fontSize = 24.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColorPrimary,
            )
            if (category.hasMore) {
                Text(
                    text = stringResource(R.string.search_more),
                    fontSize = 12.sp,
                    color = colors.textColorLink,
                    modifier = Modifier.clickable(indication = null, interactionSource = null, onClick = {
                        onMoreClick()
                    })
                )
            }
        }

        category.results.forEach { result ->
            when (result) {
                is FriendSearchInfo -> {
                    ContactSearchResultItem(
                        keywords = keywords,
                        result = result,
                        onClick = { onResultClick(result) }
                    )
                }

                is GroupSearchInfo -> {
                    GroupSearchResultItem(
                        keywords = keywords,
                        result = result,
                        onClick = { onResultClick(result) }
                    )
                }

                is MessageSearchResultItem -> {
                    ConversationSearchResultItem(
                        keywords = keywords,
                        result = result,
                        onClick = { onResultClick(result) }
                    )
                }
            }
        }

    }
}

@Composable
private fun ContactSearchResultItem(
    keywords: String,
    result: FriendSearchInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            url = result.userAvatarURL,
            name = result.displayName,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            HighlightTitle(
                text = result.displayName,
                keywords = keywords,
            )

            HighlightSecondary(
                textList = listOf(
                    HighlightTextItem(text = "ID: "),
                    HighlightTextItem(text = result.userID, keywords = keywords)
                ),
            )
        }
    }
}

@Composable
private fun GroupSearchResultItem(
    keywords: String,
    result: GroupSearchInfo,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            url = result.groupAvatarURL,
            name = result.displayName,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            HighlightTitle(
                text = result.displayName,
                keywords = keywords,
            )
            HighlightSecondary(
                textList = listOf(
                    HighlightTextItem(text = "${stringResource(R.string.search_group_id)}: "),
                    HighlightTextItem(text = result.groupID, keywords = keywords)
                )
            )
        }
    }
}

@Composable
private fun ConversationSearchResultItem(
    keywords: String,
    result: MessageSearchResultItem,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            url = result.conversationAvatarURL,
            name = result.displayName,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            HighlightTitle(
                text = result.displayName,
                keywords = keywords,
            )

            if (result.messageCount > 1) {
                HighlightSecondary(
                    textList = listOf(
                        HighlightTextItem(
                            text = stringResource(
                                R.string.search_related_chat_record_count,
                                result.messageCount
                            )
                        )
                    )
                )
            } else {
                HighlightSecondary(
                    textList = listOf(
                        HighlightTextItem(
                            text = result.messageList.firstOrNull()
                                ?.let { rememberEmojiKeyToName(MessageUtils.getMessageAbstract(it)) }
                                ?: "", keywords = keywords)
                    )
                )
            }

        }
    }
}

@Composable
fun SubScreenSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors
    val fonts = LocalTheme.current.fonts
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
            contentDescription = "Back",
            tint = colors.textColorSecondary,
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    onBack()
                })

        Spacer(modifier = Modifier.width(10.dp))


        val initialSubValue = remember { TextFieldValue(query, selection = TextRange(query.length)) }
        var subTextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(initialSubValue) }
        LaunchedEffect(query) {
            if (query != subTextFieldValue.text) {
                subTextFieldValue = TextFieldValue(query, selection = TextRange(query.length))
            }
        }
        BasicTextField(
            value = subTextFieldValue,
            onValueChange = {
                subTextFieldValue = it
                if (it.text != query) onQueryChange(it.text)
            },
            textStyle = TextStyle(
                color = colors.textColorPrimary,
                fontSize = fonts.caption1Medium.size,
                fontWeight = fonts.caption1Medium.weight,
            ),
            cursorBrush = SolidColor(colors.textColorLink),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color = colors.bgColorInput, shape = RoundedCornerShape(10.dp))
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        keyboardController?.show()
                    }
                },
            enabled = true,
            readOnly = false,
            decorationBox = { innerTextField ->
                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textColorTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_search_text),
                                color = colors.textColorTertiary,
                                fontSize = fonts.caption1Medium.size,
                                fontWeight = fonts.caption1Medium.weight,
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = stringResource(R.string.base_component_cancel),
            color = colors.textColorLink,
            fontSize = fonts.caption1Medium.size,
            modifier = Modifier.clickable {
                onClear()
                onBack()
            }
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
}
