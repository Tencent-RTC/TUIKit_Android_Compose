package io.trtc.tuikit.atomicx.messagelist.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

@Composable
fun AuxiliaryTextPopupMenu(
    onHide: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgColorOperate),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AuxiliaryTextPopupMenuItem(
                text = stringResource(R.string.message_list_menu_hide),
                iconRes = R.drawable.message_list_menu_hide_icon,
                onClick = onHide
            )
            AuxiliaryTextPopupMenuItem(
                text = stringResource(R.string.message_list_menu_forward),
                iconRes = R.drawable.message_list_menu_forward_icon,
                onClick = onForward
            )
            AuxiliaryTextPopupMenuItem(
                text = stringResource(R.string.message_list_menu_copy),
                iconRes = R.drawable.message_list_menu_copy_icon,
                onClick = onCopy
            )
        }
    }
}

@Composable
private fun AuxiliaryTextPopupMenuItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = text,
            modifier = Modifier.size(20.dp),
            tint = colors.textColorLink
        )
        Text(
            text = text,
            fontSize = 10.sp,
            color = colors.textColorPrimary
        )
    }
}
