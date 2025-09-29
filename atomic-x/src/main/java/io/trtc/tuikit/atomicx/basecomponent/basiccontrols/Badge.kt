package io.trtc.tuikit.atomicx.basecomponent.basiccontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

sealed class BadgeType {
    data object Text : BadgeType()
    data object Dot : BadgeType()
}

@Composable
fun Badge(modifier: Modifier = Modifier, text: String? = null, type: BadgeType = BadgeType.Text) {
    val colors = LocalTheme.current.colors
    if (text.isNullOrEmpty() || type == BadgeType.Dot) {
        Box(
            modifier = modifier
                .size(8.dp)
                .background(color = colors.textColorError, shape = CircleShape)
        )
    } else {
        Text(
            modifier = modifier
                .height(16.dp)
                .background(color = colors.textColorError, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 5.dp)
                .clip(CircleShape)
                .wrapContentSize(),
            text = text,
            color = colors.textColorButton,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 12.sp,
            fontWeight = FontWeight.W600
        )
    }
}