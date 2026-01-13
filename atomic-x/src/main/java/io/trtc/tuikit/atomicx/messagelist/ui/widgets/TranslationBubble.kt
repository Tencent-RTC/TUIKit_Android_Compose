package io.trtc.tuikit.atomicx.messagelist.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.rememberEmojiText


@Composable
fun TranslationBubble(
    modifier: Modifier = Modifier,
    isSelf: Boolean,
    isLoading: Boolean,
    translatedText: String?,
    showMenu: Boolean = false,
    onLongPress: () -> Unit = {},
    onDismissMenu: () -> Unit = {},
    onHide: () -> Unit = {},
    onForward: () -> Unit = {},
    onCopy: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors
    val textColor = if (isSelf) colors.textColorAntiPrimary else colors.textColorPrimary
    val secondaryTextColor =
        if (isSelf) colors.textColorAntiPrimary.copy(alpha = 0.6f) else colors.textColorSecondary

    AuxiliaryTextBubble(
        modifier = modifier,
        isSelf = isSelf,
        isLoading = isLoading,
        showMenu = showMenu,
        topSpacing = 6.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        onLongPress = onLongPress,
        onDismissMenu = onDismissMenu,
        onHide = onHide,
        onForward = onForward,
        onCopy = onCopy
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val (annotatedString, inlineContent) = rememberEmojiText(translatedText ?: "")
            Text(
                text = annotatedString,
                inlineContent = inlineContent,
                fontSize = 14.sp,
                color = textColor
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .size(13.dp)
                        .background(secondaryTextColor, CircleShape)
                        .padding(2.dp),
                    tint = colors.textColorButton
                )
                Text(
                    text = stringResource(R.string.message_list_translate_default_tips),
                    fontSize = 12.sp,
                    color = secondaryTextColor
                )
            }
        }
    }
}
