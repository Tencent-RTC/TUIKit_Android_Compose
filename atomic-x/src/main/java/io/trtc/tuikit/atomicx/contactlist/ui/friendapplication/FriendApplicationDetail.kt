package io.trtc.tuikit.atomicx.contactlist.ui.friendapplication

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicxcore.api.FriendApplicationInfo

@Composable
fun FriendApplicationDetail(
    application: FriendApplicationInfo,
    onBackClick: () -> Unit = {},
    onAccept: () -> Unit = {},
    onRefuse: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors

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
                text = stringResource(R.string.contact_list_friend_application_info),
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                color = colors.textColorPrimary
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.strokeColorPrimary
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                Avatar(
                    url = application.avatarURL,
                    name = application.displayName,
                    size = AvatarSize.L,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = application.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600,
                        color = colors.textColorPrimary
                    )

                    Text(
                        text = "ID：${application.applicationID}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W400,
                        color = colors.textColorSecondary
                    )
                }
            }

            if (!application.addWording.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.contact_list_friend_application_validation_message),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W400,
                            color = colors.textColorSecondary
                        )
                    }

                    Text(
                        text = application.addWording ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W400,
                        color = colors.textColorPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onAccept()
                        }
                        .background(color = colors.textColorLink),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = stringResource(R.string.contact_list_agree),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W400,
                        color = colors.textColorButton
                    )
                }

                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onAccept()
                        }
                        .background(color = colors.bgColorInput),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.contact_list_refuse),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W400,
                        color = colors.textColorError
                    )
                }
            }
        }
    }
}