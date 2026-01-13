package io.trtc.tuikit.atomicx.messagelist.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messagelist.utils.isAllRead
import io.trtc.tuikit.atomicx.messagelist.utils.isShowReadReceipt
import io.trtc.tuikit.atomicx.messagelist.utils.isUnread
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

@Composable
fun MessageReadReceiptIndicator(message: MessageInfo, modifier: Modifier = Modifier) {
    val colors = LocalTheme.current.colors
    if (message.isShowReadReceipt) {
        val resID = if (!message.isUnread) {
            R.drawable.message_list_all_read_icon
        } else {
            R.drawable.message_list_unread_icon
        }
        Box(
            modifier = modifier
        ) {
            Icon(
                modifier = Modifier
                    .size(16.dp),
                painter = painterResource(id = resID),
                tint = if (message.isAllRead) {
                    colors.textColorLink
                } else {
                    colors.textColorSecondary
                },
                contentDescription = null
            )
        }
    }
}