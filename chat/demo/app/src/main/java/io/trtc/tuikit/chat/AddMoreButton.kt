package io.trtc.tuikit.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

data class MenuItem(
    val enabled: Boolean = true,
    val text: String,
    val onClick: () -> Unit,
)

@Composable
fun AddMoreButton(
    modifier: Modifier = Modifier,
    iconResId: Int = R.drawable.app_main_nav_add_icon,
    menuItems: List<MenuItem> = emptyList()
) {
    val colors = LocalTheme.current.colors
    var showMenu by remember { mutableStateOf(false) }
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }
    var popupWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Box {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = "More",
            modifier = modifier
                .clickable {
                    showMenu = true
                }
                .onGloballyPositioned { coordinates ->
                    buttonSize = coordinates.size
                },
            tint = colors.textColorLink
        )

        if (showMenu) {
            // Y offset = button height + spacing
            val yOffset = buttonSize.height + with(density) { 8.dp.roundToPx() }
            // X offset to align popup end with button end
            val xOffset = -(popupWidth - buttonSize.width)

            MorePopup(
                offset = IntOffset(xOffset, yOffset),
                menuItems = menuItems,
                onPopupWidthChanged = { popupWidth = it },
                onDismiss = { showMenu = false }
            )
        }
    }
}

@Composable
private fun MorePopup(
    offset: IntOffset,
    menuItems: List<MenuItem>,
    onPopupWidthChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Popup(
        offset = offset,
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .onGloballyPositioned { coordinates ->
                    onPopupWidthChanged(coordinates.size.width)
                },
            colors = CardDefaults.cardColors(containerColor = colors.bgColorDialog),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.Start
            ) {
                menuItems.forEachIndexed { index, menuItem ->
                    PopupMenuItem(
                        text = menuItem.text,
                        enabled = menuItem.enabled,
                        onClick = { onDismiss(); menuItem.onClick() }
                    )

                    if (index < menuItems.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = colors.strokeColorSecondary,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupMenuItem(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        color = Color.Transparent
    ) {
        Box(modifier = Modifier, contentAlignment = Alignment.CenterStart) {
            Text(
                text = text,
                color = if (enabled) colors.textColorPrimary else colors.textColorDisable,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun createAddContactMenuItems(
    onAddFriendClick: () -> Unit,
    onAddGroupClick: () -> Unit
): List<MenuItem> {
    return listOf(
        MenuItem(text = stringResource(R.string.compose_demo_add_contact), onClick = onAddFriendClick),
        MenuItem(text = stringResource(R.string.compose_demo_join_group), onClick = onAddGroupClick)
    )
}

@Composable
fun createChatMenuItems(
    onAddC2CChatClick: () -> Unit,
    onAddGroupChatClick: () -> Unit,
): List<MenuItem> {
    return listOf(
        MenuItem(text = stringResource(R.string.compose_demo_start_c2c_chat), onClick = onAddC2CChatClick),
        MenuItem(text = stringResource(R.string.compose_demo_create_group_chat), onClick = onAddGroupChatClick),
    )
}

