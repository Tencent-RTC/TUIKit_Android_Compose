package io.trtc.tuikit.atomicx.contactlist.ui.mygroup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo

@Composable
fun MyGroupDialog(isVisible: Boolean, onDismiss: () -> Unit, onGroupClick: (ContactInfo) -> Unit) {
    val colors = LocalTheme.current.colors
    if (isVisible) {
        FullScreenDialog(
            onDismissRequest = onDismiss
        ) {
            MyGroup(
                onBackClick = { onDismiss() },
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colors.bgColorOperate)
            ) {
                onGroupClick(it)
            }

        }
    }
}