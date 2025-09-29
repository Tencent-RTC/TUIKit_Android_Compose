package io.trtc.tuikit.chat.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.trtc.tuikit.atomicx.basecomponent.theme.Colors
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.ui.ContactList
import io.trtc.tuikit.atomicxcore.api.ContactInfo
import io.trtc.tuikit.chat.R
import io.trtc.tuikit.chat.widgets.PageHeader

@Composable
fun ContactsPage(
    onGroupClick: (ContactInfo) -> Unit = {},
    onContactClick: (ContactInfo) -> Unit = {},
    editContent: @Composable () -> Unit = {},
) {

    val colors = LocalTheme.current.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.Transparent)
    ) {
        PageHeader(stringResource(R.string.chat_uikit_contacts)) {
            editContent()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(), contentAlignment = Alignment.Center
        ) {
            ContactList(onGroupClick = onGroupClick, onContactClick = onContactClick)
        }
    }
}