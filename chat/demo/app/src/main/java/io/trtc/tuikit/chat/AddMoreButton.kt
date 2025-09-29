package io.trtc.tuikit.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddType

data class MenuItem(
    val enabled: Boolean = true,
    val text: String,
    val onClick: () -> Unit,
)

@Composable
fun AddMoreButton(
    modifier: Modifier = Modifier,
    iconResId: Int = R.drawable.app_main_nav_add_icon,
    menuItems: List<MenuItem> = emptyList(),
    popupWidth: androidx.compose.ui.unit.Dp = 140.dp
) {
    val colors = LocalTheme.current.colors
    var showMenu by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf(AddType.CONTACT) }
    var buttonPosition by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    var popupOffset by remember { mutableStateOf(IntOffset.Zero) }
    var indicatorXPosition by remember { mutableStateOf(0f) }

    val finalMenuItems = menuItems

    Box {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = "More",
            modifier = modifier
                .clickable {
                    showMenu = true
                }
                .onGloballyPositioned { coordinates ->
                    buttonPosition = coordinates
                },
            tint = colors.textColorLink
        )

        if (showMenu) {
            LaunchedEffect(buttonPosition) {
                buttonPosition?.let { coords ->
                    val buttonGlobalPosition = coords.positionInRoot()
                    val buttonWidth = coords.size.width
                    val popupWidthPx = with(density) { popupWidth.toPx() }
                    val indicatorHeight = with(density) { 8.dp.toPx() }
                    val yOffset = with(density) { 8.dp.toPx() }

                    val screenWidth = with(density) {
                        configuration.screenWidthDp.dp.toPx()
                    }

                    val anchorWidth = buttonWidth.toFloat()
                    var indicatorX = anchorWidth / 2 + indicatorHeight
                    var x = (buttonGlobalPosition.x - indicatorHeight).toInt()
                    val xOffset = anchorWidth / 2

                    if (buttonGlobalPosition.x * 2 + anchorWidth > screenWidth) {
                        indicatorX = popupWidthPx - anchorWidth / 2 - xOffset
                        x = (buttonGlobalPosition.x + anchorWidth - popupWidthPx + xOffset).toInt()
                    }

                    val y = (buttonGlobalPosition.y + yOffset).toInt()

                    indicatorXPosition = indicatorX
                    popupOffset = IntOffset(x, y)
                }
            }

            MorePopup(
                offset = popupOffset,
                indicatorX = indicatorXPosition,
                menuItems = finalMenuItems,
                popupWidth = popupWidth,
                onDismiss = { showMenu = false }
            )
        }
    }
}

@Composable
private fun MorePopup(
    offset: IntOffset,
    indicatorX: Float,
    menuItems: List<MenuItem>,
    popupWidth: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit
) {
    val colors = LocalTheme.current.colors

    Popup(
        offset = offset,
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .padding(horizontal = 5.dp)
        ) {
            Card(
                modifier = Modifier.wrapContentSize(),
                colors = CardDefaults.cardColors(containerColor = colors.bgColorDialog),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    menuItems.forEachIndexed { index, menuItem ->
                        PopupMenuItem(
                            text = menuItem.text,
                            enabled = menuItem.enabled,
                            onClick = { onDismiss();menuItem.onClick() }
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
        Text(
            text = text,
            color = if (enabled) colors.textColorPrimary else colors.textColorDisable,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
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

