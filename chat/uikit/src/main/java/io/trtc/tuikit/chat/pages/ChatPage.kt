package io.trtc.tuikit.chat.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messageinput.ui.MessageInput
import io.trtc.tuikit.atomicx.messagelist.ui.MessageList
import io.trtc.tuikit.atomicxcore.api.ConversationInfo
import io.trtc.tuikit.atomicxcore.api.ConversationListStore
import io.trtc.tuikit.atomicxcore.api.MessageInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Composable
fun ChatPage(
    conversationID: String,
    locateMessage: MessageInfo? = null,
    onUserClick: (String) -> Unit,
    onChatHeaderClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val conversationListStore = remember { ConversationListStore.create() }
    val conversationListState = conversationListStore.conversationListState
    var conversation: StateFlow<ConversationInfo?> = remember {
        conversationListState.conversationList
            .map {
                it.firstOrNull { it.conversationID == conversationID }
            }.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }
    val conversationState = conversation.collectAsState()
    LaunchedEffect(Unit) {
        conversationListStore.fetchConversationInfo(conversationID)
    }

    val avatarUrl = conversationState.value?.avatarURL
    val displayName = conversationState.value?.title
    val colors = LocalTheme.current.colors
    Scaffold(
        modifier = Modifier
            .background(color = colors.bgColorOperate)
            .fillMaxSize()
            .wrapContentHeight(), topBar = {
            Box(
                modifier = Modifier
                    .background(color = colors.bgColorOperate)
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                ChatHeader(
                    avatarUrl = avatarUrl,
                    name = displayName ?: "",
                    onBackClick = onBackClick,
                    onAvatarClick = {
                        onChatHeaderClick(conversationID)
                    }
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.bgColorOperate)
            ) {
                MessageInput(modifier = Modifier.navigationBarsPadding(), conversationID = conversationID)
            }

        }) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
        ) {
            MessageList(conversationID = conversationID, locateMessage = locateMessage) {
                onUserClick(it)
            }
        }
    }
}


@Composable
fun ChatHeader(
    avatarUrl: Any? = null,
    name: String,
    onBackClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier
            .background(color = colors.bgColorOperate)
            .fillMaxWidth()
            .padding(0.dp)
            .wrapContentHeight(unbounded = true)
            .height(IntrinsicSize.Min)

    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(modifier = Modifier.size(40.dp), onClick = onBackClick) {
                Icon(
                    modifier = Modifier
                        .size(9.dp, 16.dp),
                    painter = painterResource(R.drawable.message_list_back_icon),
                    tint = colors.textColorSecondary,
                    contentDescription = "back"
                )
            }
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onAvatarClick() }),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Avatar(url = avatarUrl, name = name) {
                    onAvatarClick()
                }

                Column(modifier = Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.Center) {
                    Text(
                        modifier = Modifier,
                        color = colors.textColorPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        text = name
                    )

                }
            }

        }

        HorizontalDivider(thickness = 0.5.dp, color = colors.strokeColorPrimary)

    }
}
