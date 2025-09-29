package io.trtc.tuikit.atomicx.basecomponent.basiccontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.config.AppBuilderConfig
import io.trtc.tuikit.atomicx.basecomponent.config.GlobalAvatarShape
import io.trtc.tuikit.atomicx.basecomponent.theme.Colors
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

sealed class AvatarContent {
    data class Image(val url: Any?, val fallbackName: String = "") : AvatarContent()
    data class Text(val name: String) : AvatarContent()
    data class Icon(val painter: Painter? = null) : AvatarContent()
    data class Default(val isGroup: Boolean = false) : AvatarContent()
}

sealed class AvatarBadge {
    object None : AvatarBadge()
    object Dot : AvatarBadge()
    data class Text(val text: String) : AvatarBadge()
    data class Count(val count: Int) : AvatarBadge()
}

enum class AvatarSize(
    val size: Dp,
    val textSize: TextUnit,
    val borderRadius: Dp
) {
    XS(size = 24.dp, textSize = 12.sp, borderRadius = 4.dp),
    S(size = 32.dp, textSize = 14.sp, borderRadius = 4.dp),
    M(size = 40.dp, textSize = 16.sp, borderRadius = 4.dp),
    L(size = 48.dp, textSize = 18.sp, borderRadius = 8.dp),
    XL(size = 64.dp, textSize = 28.sp, borderRadius = 12.dp),
    XXL(size = 96.dp, textSize = 36.sp, borderRadius = 12.dp)
}

enum class AvatarShape {
    Round,
    RoundRectangle,
    Rectangle
}

enum class AvatarStatus {
    None,
    Online,
    Offline,
}

@Composable
fun Avatar(
    content: AvatarContent,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.M,
    shape: AvatarShape? = null,
    status: AvatarStatus = AvatarStatus.None,
    badge: AvatarBadge = AvatarBadge.None,
    onClick: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors

    Box(
        modifier = modifier.size(size.size),
        contentAlignment = Alignment.Center
    ) {
        BasicAvatar(
            content = content,
            size = size,
            shape = shape,
            onClick = onClick
        )

        if (status == AvatarStatus.Online || status == AvatarStatus.Offline) {
            val dotColor = when (status) {
                AvatarStatus.Offline -> Colors.GrayLight7
                AvatarStatus.Online -> colors.textColorSuccess
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(8.dp)
                    .border(1.dp, colors.bgColorDefault, CircleShape)
                    .background(dotColor, CircleShape)
            )
        }
        if (badge != AvatarBadge.None) {
            val badgeText = when (badge) {
                is AvatarBadge.Text -> badge.text
                is AvatarBadge.Count -> badge.count.toString()
                else -> ""
            }
            var badgeSize by remember { mutableStateOf(IntOffset(0, 0)) }
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .onGloballyPositioned { coordinates ->
                        badgeSize = IntOffset(coordinates.size.width / 2, -coordinates.size.height / 2)
                    }
                    .offset({ badgeSize }),
                text = badgeText
            )
        }
    }
}

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    url: Any? = null,
    name: String = "",
    size: AvatarSize = AvatarSize.M,
    onClick: () -> Unit = {}
) {
    BasicAvatar(
        modifier = modifier,
        content = AvatarContent.Image(url, name),
        size = size,
        onClick = onClick
    )
}

@Composable
private fun BasicAvatar(
    content: AvatarContent,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.M,
    shape: AvatarShape? = null,
    onClick: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors
    val isLoadError = remember { mutableStateOf(false) }

    val avatarShape: Shape = getAvatarShape(shape, size)

    Box(
        modifier = modifier
            .size(size.size)
            .background(color = colors.bgColorAvatar, shape = avatarShape)
            .clip(avatarShape)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (content) {
            is AvatarContent.Image -> {
                if (content.url == null || (content.url is String && content.url.isEmpty()) || isLoadError.value) {
                    val title = if (content.fallbackName.isEmpty()) "" else content.fallbackName.first().uppercase()
                    Text(
                        text = title,
                        color = colors.textColorPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = size.textSize,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        style = TextStyle(fontWeight = FontWeight.Medium)
                    )
                } else {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(avatarShape),
                        model = content.url,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        onError = {
                            isLoadError.value = true
                        }
                    )
                }
            }

            is AvatarContent.Text -> {
                val title = if (content.name.isEmpty()) "" else content.name.first().uppercase()
                Text(
                    text = title,
                    color = colors.textColorPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = size.textSize,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = TextStyle(fontWeight = FontWeight.Medium)
                )
            }

            is AvatarContent.Icon -> {
                val painter = content.painter ?: painterResource(R.drawable.base_component_avatar_default_icon)
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(size.size * 0.5f),
                    tint = colors.textColorPrimary
                )
            }

            is AvatarContent.Default -> {
                val res = when {
                    content.isGroup -> R.drawable.base_component_avatar_group_default_icon
                    else -> R.drawable.base_component_avatar_user_default_icon
                }
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(avatarShape),
                    model = res,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

private fun getAvatarShape(shape: AvatarShape? = null, size: AvatarSize): Shape {
    return shape?.let {
        when (shape) {
            AvatarShape.Round -> CircleShape
            AvatarShape.RoundRectangle -> RoundedCornerShape(size.borderRadius)
            AvatarShape.Rectangle -> RectangleShape
        }
    } ?: when (AppBuilderConfig.avatarShape) {
        GlobalAvatarShape.CIRCULAR -> CircleShape
        GlobalAvatarShape.ROUNDED -> RoundedCornerShape(size.borderRadius)
        GlobalAvatarShape.SQUARE -> RectangleShape
    }
}