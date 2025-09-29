package io.trtc.tuikit.atomicx.contactlist.ui.addfriendandgroup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicxcore.api.ContactInfo

@Composable
fun ContactDetail(
    result: ContactInfo,
    onAddContact: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                url = result.avatarURL,
                name = result.displayName,
                size = AvatarSize.XL
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = result.displayName,
                    color = colors.textColorPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID：${result.contactID}",
                    color = colors.textColorSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400
                )
//                Spacer(modifier = Modifier.height(4.dp))
//                val signature =
//                    "${stringResource(R.string.contact_list_signature)}：${result.signature ?: stringResource(R.string.contact_list_no_content)}"
//                Text(
//                    text = signature,
//                    color = colors.textColorSecondary,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.W400
//                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddContact,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.bgColorInput
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.contact_list_add_contact),
                color = colors.textColorLink,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400
            )
        }
    }
} 