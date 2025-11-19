package io.trtc.tuikit.atomicx.contactlist.ui.friendapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicx.contactlist.viewmodels.FriendApplicationViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.FriendApplicationViewModelFactory
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore
import io.trtc.tuikit.atomicxcore.api.contact.FriendApplicationInfo

@Composable
fun FriendApplication(
    onBackClick: () -> Unit = {},
    onApplicationClick: (FriendApplicationInfo) -> Unit = {},
    friendApplicationViewModelFactory: FriendApplicationViewModelFactory = FriendApplicationViewModelFactory(
        ContactListStore.create()
    )
) {
    val colors = LocalTheme.current.colors
    val friendApplicationViewModel = viewModel(
        FriendApplicationViewModel::class,
        factory = friendApplicationViewModelFactory
    )
    LaunchedEffect(Unit) {
        friendApplicationViewModel.clearFriendApplicationUnreadCount()
    }
    val friendApplications by friendApplicationViewModel.friendApplications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.bgColorOperate)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Back button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
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
                text = stringResource(R.string.contact_list_friend_application),
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                color = colors.textColorPrimary
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.strokeColorPrimary
        )

        if (friendApplications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.contact_list_no_friend_application),
                    fontSize = 17.sp,
                    color = colors.textColorSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp)
            ) {
                items(friendApplications) { application ->
                    FriendApplicationItem(
                        application = application,
                        onItemClick = { onApplicationClick(application) },
                        onAccept = { friendApplicationViewModel.acceptFriendApplication(application) },
                        onRefuse = { friendApplicationViewModel.refuseFriendApplication(application) }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendApplicationItem(
    application: FriendApplicationInfo,
    onItemClick: () -> Unit = {},
    onAccept: () -> Unit = {},
    onRefuse: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onItemClick() }
            .padding(start = 16.dp, end = 0.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {

        Avatar(
            url = application.avatarURL,
            name = application.displayName,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = application.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                color = colors.textColorPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = application.addWording ?: "",
                fontSize = 12.sp,
                fontWeight = FontWeight.W400,
                color = colors.textColorSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier.padding(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onAccept()
                    }
                    .background(color = colors.textColorLink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.contact_list_agree),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.bgColorOperate
                )
            }

            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onRefuse()
                    }
                    .border(
                        width = 1.dp,
                        color = colors.strokeColorPrimary,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.contact_list_refuse),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorError
                )
            }
        }
    }
}