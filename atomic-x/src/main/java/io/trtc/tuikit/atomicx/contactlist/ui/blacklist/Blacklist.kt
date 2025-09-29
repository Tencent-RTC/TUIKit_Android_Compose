package io.trtc.tuikit.atomicx.contactlist.ui.blacklist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.AZOrderedList
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.AZOrderedListItem
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicx.contactlist.viewmodels.BlacklistViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.BlacklistViewModelFactory
import io.trtc.tuikit.atomicxcore.api.ContactInfo
import io.trtc.tuikit.atomicxcore.api.ContactListStore

@Composable
fun Blacklist(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    blacklistViewModelFactory: BlacklistViewModelFactory = BlacklistViewModelFactory(
        ContactListStore.create()
    ),
    onContactClick: (ContactInfo) -> Unit
) {
    val colors = LocalTheme.current.colors
    val blacklistViewModel =
        viewModel(BlacklistViewModel::class, factory = blacklistViewModelFactory)

    val blacklistUsers by blacklistViewModel.blacklistUsers.collectAsState()

    LaunchedEffect(Unit) {
        blacklistViewModel.fetchBlacklistUsers()
    }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(color = colors.bgColorOperate)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBackClick() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = colors.textColorLink,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = stringResource(R.string.contact_list_back),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W400,
                            color = colors.textColorLink
                        )
                    }

                    Text(
                        text = stringResource(R.string.contact_list_blacklist),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600,
                        color = colors.textColorPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(80.dp))
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = colors.strokeColorSecondary
            )

            if (blacklistUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.contact_list_no_blacklist_users),
                        fontSize = 17.sp,
                        color = colors.textColorSecondary
                    )
                }
            } else {
                AZOrderedList(
                    modifier = Modifier.fillMaxSize(),
                    dataSource = blacklistUsers.map { user ->
                        AZOrderedListItem(
                            key = user.contactID,
                            label = user.displayName,
                            avatarUrl = user.avatarURL,
                            extraData = user
                        )
                    },
                    onItemClick = { item -> onContactClick(item.extraData) }
                )
            }
        }

    }
}
