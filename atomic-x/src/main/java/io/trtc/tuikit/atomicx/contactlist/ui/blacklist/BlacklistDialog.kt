package io.trtc.tuikit.atomicx.contactlist.ui.blacklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo

@Composable
fun BlacklistDialog(isVisible: Boolean, onDismiss: () -> Unit, onContactClick: (ContactInfo) -> Unit) {
    val colors = LocalTheme.current.colors
    if (isVisible) {
        FullScreenDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Blacklist(
                onBackClick = { onDismiss() },
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colors.bgColorOperate)
                    .statusBarsPadding()
            ) {
                onContactClick(it)
            }
        }
    }
}
