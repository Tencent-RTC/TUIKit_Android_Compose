package io.trtc.tuikit.atomicx.contactlist.ui.groupapplication

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.DisposableEffect
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
import io.trtc.tuikit.atomicx.contactlist.utils.canHandle
import io.trtc.tuikit.atomicx.contactlist.utils.fromUserDisplayName
import io.trtc.tuikit.atomicx.contactlist.utils.groupDisplayName
import io.trtc.tuikit.atomicx.contactlist.utils.isJoinRequest
import io.trtc.tuikit.atomicx.contactlist.utils.statusText
import io.trtc.tuikit.atomicx.contactlist.utils.toUserDisplayName
import io.trtc.tuikit.atomicx.contactlist.viewmodels.GroupApplicationViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.GroupApplicationViewModelFactory
import io.trtc.tuikit.atomicxcore.api.ContactListStore
import io.trtc.tuikit.atomicxcore.api.GroupApplicationInfo

@Composable
fun GroupApplication(
    onBackClick: () -> Unit = {},
    onApplicationClick: (GroupApplicationInfo) -> Unit = {},
    groupApplicationViewModelFactory: GroupApplicationViewModelFactory = GroupApplicationViewModelFactory(
        ContactListStore.create()
    )
) {
    val colors = LocalTheme.current.colors
    val groupApplicationViewModel = viewModel(
        GroupApplicationViewModel::class,
        factory = groupApplicationViewModelFactory
    )

    DisposableEffect(Unit) {
        groupApplicationViewModel.fetchGroupApplicationList()
        groupApplicationViewModel.clearGroupApplicationUnreadCount()
        onDispose {
            groupApplicationViewModel.clearGroupApplicationUnreadCount()
        }
    }

    val groupApplications by groupApplicationViewModel.groupApplications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.bgColorOperate)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
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
                text = stringResource(R.string.contact_list_group_application),
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                color = colors.textColorPrimary
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.strokeColorPrimary
        )

        if (groupApplications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.contact_list_no_group_application),
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
                items(groupApplications, key = { it.applicationID }) { application ->
                    GroupApplicationItem(
                        application = application,
                        onItemClick = { onApplicationClick(application) },
                        onAccept = { groupApplicationViewModel.acceptGroupApplication(application) },
                        onRefuse = { groupApplicationViewModel.refuseGroupApplication(application) }
                    )
                }
            }
        }
    }
}

@Composable
fun GroupApplicationItem(
    application: GroupApplicationInfo,
    onItemClick: () -> Unit = {},
    onAccept: () -> Unit = {},
    onRefuse: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Avatar(
                url = application.fromUserAvatarURL,
                name = application.fromUserDisplayName,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (application.isJoinRequest) {
                            stringResource(R.string.contact_list_apply_to_join_group)
                        } else {
                            stringResource(R.string.contact_list_invite_to_join_group)
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500,
                        color = colors.textColorPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = if (application.isJoinRequest) {
                        "${stringResource(R.string.contact_list_applicant)}：${application.fromUserDisplayName}"
                    } else {
                        "${stringResource(R.string.contact_list_invitee)}：${application.toUserDisplayName}"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${stringResource(R.string.contact_list_group_name)}：${application.groupDisplayName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!application.requestMsg.isNullOrEmpty()) {
                    Text(
                        text = application.requestMsg ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W400,
                        color = colors.textColorSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (application.canHandle) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAccept() }
                            .background(color = colors.textColorLink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.contact_list_agree),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W400,
                            color = colors.bgColorOperate
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRefuse() }
                            .border(
                                width = 1.dp,
                                color = colors.strokeColorPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.contact_list_refuse),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W400,
                            color = colors.textColorError
                        )
                    }
                }
            } else {
                Text(
                    text = application.statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = colors.textColorSecondary
                )
            }
        }
    }
}