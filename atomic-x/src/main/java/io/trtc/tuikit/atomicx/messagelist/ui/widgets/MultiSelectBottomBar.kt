package io.trtc.tuikit.atomicx.messagelist.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

@Composable
fun MultiSelectBottomBar(
    selectedCount: Int,
    onCancel: () -> Unit = {},
    onDelete: () -> Unit = {},
    onForward: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = colors.bgColorOperate)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(modifier = Modifier.align(Alignment.CenterStart)) {

            Box(
                modifier = Modifier
                    .clickable(onClick = onForward)
                    .padding(4.dp)
            ) {

                Icon(
                    modifier = Modifier
                        .size(16.dp),
                    painter = painterResource(R.drawable.message_list_forward_button_icon),
                    tint = colors.buttonColorPrimaryDefault,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(24.dp))

            Box(
                modifier = Modifier
                    .clickable(onClick = onDelete)
                    .padding(4.dp)
            ) {

                Icon(
                    modifier = Modifier
                        .size(16.dp),
                    painter = painterResource(R.drawable.message_list_delete_button_icon),
                    tint = colors.buttonColorPrimaryDefault,
                    contentDescription = null
                )
            }
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.message_list_multi_select_count, selectedCount),
            fontSize = 14.sp,
            color = colors.textColorSecondary
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable {
                    onCancel()
                },
            text = stringResource(R.string.base_component_cancel),
            fontSize = 14.sp
        )

    }
}