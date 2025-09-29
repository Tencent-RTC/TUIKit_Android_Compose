package io.trtc.tuikit.atomicx.chatsetting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

@Composable
fun AvatarSelector(
    isVisible: Boolean,
    imageUrls: List<String>,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.chat_setting_select_avatar),
    preSelectedImageUrl: String? = null,
    onImageSelected: (index: Int, imageUrl: String) -> Unit
) {
    val colors = LocalTheme.current.colors
    if (isVisible) {
        FullScreenDialog(
            onDismissRequest = onDismiss,
        ) {

            AvatarSelectorContent(
                title = title,
                imageUrls = imageUrls,
                preSelectedImageUrl = preSelectedImageUrl,
                onBack = onDismiss,
                onConfirm = { index, url ->
                    onDismiss()
                    onImageSelected(index, url)
                }
            )
        }
    }
}

@Composable
private fun AvatarSelectorContent(
    title: String,
    imageUrls: List<String>,
    preSelectedImageUrl: String?,
    onBack: () -> Unit,
    onConfirm: (index: Int, imageUrl: String) -> Unit
) {
    val initialSelectedIndex = remember(preSelectedImageUrl, imageUrls) {
        if (preSelectedImageUrl != null) {
            imageUrls.indexOf(preSelectedImageUrl).takeIf { it >= 0 } ?: -1
        } else {
            -1
        }
    }

    var selectedIndex by remember(initialSelectedIndex) { mutableIntStateOf(initialSelectedIndex) }
    val colors = LocalTheme.current.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgColorOperate)
    ) {
        ImagePickerHeader(
            title = title,
            hasSelection = selectedIndex >= 0,
            onBackClick = onBack,
            onConfirmClick = {
                if (selectedIndex >= 0) {
                    onConfirm(selectedIndex, imageUrls[selectedIndex])
                }
            }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(imageUrls) { index, imageUrl ->
                ImageGridItem(
                    imageUrl = imageUrl,
                    isSelected = selectedIndex == index,
                    onClick = {
                        selectedIndex = if (selectedIndex == index) -1 else index
                    }
                )
            }
        }
    }
}

@Composable
private fun ImagePickerHeader(
    title: String,
    hasSelection: Boolean,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Column(modifier = Modifier.statusBarsPadding()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onBackClick() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = colors.textColorLink,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = stringResource(R.string.base_component_cancel),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    color = colors.textColorLink
                )
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                color = colors.textColorPrimary
            )

            Text(
                text = stringResource(R.string.base_component_confirm),
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = if (hasSelection) colors.textColorLink else colors.textColorDisable,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = hasSelection
                    ) {
                        if (hasSelection) {
                            onConfirmClick()
                        }
                    }
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.strokeColorSecondary
        )
    }
}

@Composable
private fun ImageGridItem(
    imageUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalTheme.current.colors
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = colors.textColorLink,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Avatar option",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .background(
                        color = colors.buttonColorPrimaryDefault,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = colors.textColorButton,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
} 