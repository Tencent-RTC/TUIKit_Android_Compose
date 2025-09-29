package io.trtc.tuikit.chat.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme


@Composable
fun PageHeader(text: String, editContent: @Composable () -> Unit = {}) {
    val colors = LocalTheme.current.colors
    Column {

        Row(
            modifier = Modifier
                .background(color = colors.bgColorOperate)
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                fontSize = 34.sp,
                color = colors.textColorPrimary,
                fontWeight = FontWeight.W600
            )

            editContent()
        }
    }
}