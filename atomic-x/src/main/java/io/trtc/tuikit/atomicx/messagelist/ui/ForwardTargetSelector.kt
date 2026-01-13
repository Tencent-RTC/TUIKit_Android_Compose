package io.trtc.tuikit.atomicx.messagelist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarContent
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.conversationlist.viewmodels.ConversationListViewModel
import io.trtc.tuikit.atomicx.conversationlist.viewmodels.ConversationListViewModelFactory
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageCheckBox
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationInfo
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationListStore

@Composable
fun ForwardTargetSelector(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onConfirm: (List<String>) -> Unit = {},
) {
    val colors = LocalTheme.current.colors
    val viewModelFactory = ConversationListViewModelFactory(ConversationListStore.create())
    val viewModel: ConversationListViewModel = viewModel(
        factory = viewModelFactory
    )
    val conversationList by viewModel.conversationList.collectAsState()
    val selectedConversations by viewModel.selectedConversations.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearSelection()
    }

    FullScreenDialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.bgColorMask)
                .statusBarsPadding()
                .navigationBarsPadding()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { },
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                colors = CardDefaults.cardColors(containerColor = colors.bgColorDialog),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = SolidColor(colors.strokeColorPrimary)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SelectorHeader(
                        onDismiss = onDismiss,
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxHeight(),
                            ) {
                                Text(
                                    text = stringResource(R.string.message_list_recent_conversations),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W500,
                                    color = colors.textColorPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        items(conversationList, key = { it.conversationID }) { conversation ->
                            ConversationSelectorItem(
                                conversation = conversation,
                                isSelected = selectedConversations.contains(conversation),
                                onToggle = {
                                    if (selectedConversations.contains(conversation)) {
                                        viewModel.removeSelection(conversation)
                                    } else {
                                        viewModel.addSelection(conversation)
                                    }
                                }
                            )
                        }
                    }

                    if (selectedConversations.isNotEmpty()) {
                        SelectedConversationsFooter(
                            selectedConversations = selectedConversations.toList(),
                            onConfirm = {
                                val selectedIds = selectedConversations.map { it.conversationID }
                                onConfirm(selectedIds)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorHeader(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.base_component_cancel),
                fontSize = 16.sp,
                color = colors.textColorLink,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() }
            )

            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.message_list_select_conversation),
                color = colors.textColorPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center
            )

        }
    }
}

@Composable
private fun ConversationSelectorItem(
    conversation: ConversationInfo,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onToggle() }
            .background(color = colors.bgColorOperate)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            content = AvatarContent.Image(
                url = conversation.avatarURL,
                fallbackName = conversation.title ?: conversation.conversationID
            ),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = conversation.title ?: conversation.conversationID,
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                color = colors.textColorPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        MessageCheckBox(
            checked = isSelected,
        )
    }
}

@Composable
private fun SelectedConversationsFooter(
    selectedConversations: List<ConversationInfo>,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit
) {
    val colors = LocalTheme.current.colors
    val selectedNames = selectedConversations.joinToString(", ") {
        it.title ?: it.conversationID
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bgColorDialog)
    ) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.strokeColorPrimary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = selectedNames,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                modifier = Modifier
                    .clickable(enabled = selectedConversations.isNotEmpty(), onClick = {
                        onConfirm()
                    }),
                fontSize = 14.sp,
                maxLines = 1,
                color = if (selectedConversations.isNotEmpty()) colors.buttonColorPrimaryDefault else colors.textColorDisable,
                text = stringResource(R.string.message_list_forward_button_text, selectedConversations.size)
            )
        }
    }
}
