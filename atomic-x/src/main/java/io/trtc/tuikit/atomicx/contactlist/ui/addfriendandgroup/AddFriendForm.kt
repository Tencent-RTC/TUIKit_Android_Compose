package io.trtc.tuikit.atomicx.contactlist.ui.addfriendandgroup

import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Toast
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddContactAndGroupViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddContactAndGroupViewModelFactory
import io.trtc.tuikit.atomicxcore.api.ContactInfo
import io.trtc.tuikit.atomicxcore.api.ContactListStore
import io.trtc.tuikit.atomicxcore.api.LoginStore

@Composable
fun AddFriendForm(
    contactInfo: ContactInfo,
    onCompleted: ((Boolean) -> Unit)? = null
) {
    val userInfo by LoginStore.shared.loginState.loginUserInfo.collectAsState()
    val defaultAddWording = stringResource(R.string.contact_list_add_wording_i_am, userInfo?.displayName ?: "")
    var addWording by remember { mutableStateOf(defaultAddWording) }
    val colors = LocalTheme.current.colors
    val context = LocalContext.current
    val activity = LocalActivity.current
    val addViewModel: AddContactAndGroupViewModel = viewModel(
        factory = AddContactAndGroupViewModelFactory(ContactListStore.create())
    )
    val uiState by addViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.requestResult) {
        uiState.requestResult?.let { result ->
            if (result.isSuccess) {
                Toast.success(activity ?: context, result.message)
            } else {
                Toast.error(activity ?: context, result.message)
            }
            onCompleted?.invoke(result.isSuccess)
            addViewModel.clearRequestResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                url = contactInfo.avatarURL,
                name = contactInfo.displayName,
                size = AvatarSize.XL
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = contactInfo.displayName,
                    color = colors.textColorPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID：${contactInfo.contactID}",
                    color = colors.textColorSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400
                )
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(123.dp),
            colors = CardDefaults.cardColors(containerColor = colors.bgColorInput)
        ) {
            BasicTextField(
                value = addWording,
                onValueChange = { addWording = it },
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

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            colors = CardDefaults.cardColors(containerColor = colors.bgColorOperate),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.contact_list_remark),
                    color = colors.textColorPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = contactInfo.displayName,
                    color = colors.textColorPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Send Button
        Button(
            onClick = { addViewModel.addFriend(contactInfo, addWording) },
            enabled = !uiState.isAddingContact,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.bgColorInput
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .height(48.dp)
        ) {
            if (uiState.isAddingContact) {
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