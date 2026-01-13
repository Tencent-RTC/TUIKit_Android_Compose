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
import androidx.compose.material3.CircularProgressIndicator
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
import io.trtc.tuikit.atomicx.search.utils.userAvatarURL
import io.trtc.tuikit.atomicx.search.viewmodels.SearchContactViewModel
import io.trtc.tuikit.atomicxcore.api.search.FriendSearchInfo

@Composable
fun SearchContactScreen(
    keywords: String,
    onBack: () -> Unit,
    onContactClick: (FriendSearchInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors
    val contactViewModel: SearchContactViewModel = viewModel()

    var searchQuery by remember { mutableStateOf(keywords) }
    val isSearching by contactViewModel.isSearching.collectAsState()
    val contactResults by contactViewModel.contactSearchResults.collectAsState()

    LaunchedEffect(Unit) {
        contactViewModel.updateSearchQuery(searchQuery)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
    ) {
        SubScreenSearchHeader(
            query = searchQuery,
            onQueryChange = { searchQuery = it; contactViewModel.updateSearchQuery(it) },
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
        } else if (contactResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.search_cannot_found_contact),
                    color = colors.textColorSecondary,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(R.string.search_category_contact),
                        fontSize = 24.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textColorPrimary,
                    )
                }
                items(contactResults) { contact ->
                    ContactResultItem(
                        contact = contact,
                        keywords = searchQuery,
                        onClick = { onContactClick(contact) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactResultItem(
    contact: FriendSearchInfo,
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
            url = contact.userAvatarURL,
            name = contact.displayName,
            size = AvatarSize.L
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            HighlightTitle(
                text = contact.displayName,
                keywords = keywords
            )

            Spacer(modifier = Modifier.height(4.dp))

            HighlightSecondary(
                textList = listOf(
                    HighlightTextItem(text = "ID: "),
                    HighlightTextItem(text = contact.userID, keywords = keywords)
                ),
            )
        }
    }
}