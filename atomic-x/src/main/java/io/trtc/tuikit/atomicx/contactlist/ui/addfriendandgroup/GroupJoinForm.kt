package io.trtc.tuikit.atomicx.contactlist.ui.addfriendandgroup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo
import io.trtc.tuikit.atomicxcore.api.login.LoginStore

@Composable
fun GroupJoinForm(
    result: ContactInfo,
    onSendRequest: (ContactInfo, String) -> Unit,
    isLoading: Boolean
) {
    val userInfo by LoginStore.shared.loginState.loginUserInfo.collectAsState()
    val defaultAddWording = stringResource(R.string.contact_list_add_wording_i_am, userInfo?.displayName ?: "")
    var verificationMessage by remember { mutableStateOf(defaultAddWording) }
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.bgColorOperate)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(url = result.avatarURL, name = result.displayName, size = AvatarSize.L)

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
//                Text(
//                    text = "${stringResource(R.string.contact_list_group_type_text)}：${result.groupType}",
//                    color = colors.textColorSecondary,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.W400
//                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.contact_list_fill_validation_message),
            color = colors.textColorPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(123.dp),
            colors = CardDefaults.cardColors(containerColor = colors.bgColorInput)
        ) {
            BasicTextField(
                value = verificationMessage,
                onValueChange = { verificationMessage = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textStyle = TextStyle(
                    color = colors.textColorTertiary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onSendRequest(result, verificationMessage) },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.bgColorInput
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .height(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.textColorLink,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.contact_list_send),
                    color = colors.textColorLink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
    }
} 