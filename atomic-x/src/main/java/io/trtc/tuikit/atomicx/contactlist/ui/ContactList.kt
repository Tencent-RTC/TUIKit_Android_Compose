package io.trtc.tuikit.atomicx.contactlist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Badge
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.AZOrderedList
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.AZOrderedListItem
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.ui.blacklist.BlacklistDialog
import io.trtc.tuikit.atomicx.contactlist.ui.friendapplication.FriendApplicationDialog
import io.trtc.tuikit.atomicx.contactlist.ui.groupapplication.GroupApplicationDialog
import io.trtc.tuikit.atomicx.contactlist.ui.mygroup.MyGroupDialog
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicx.contactlist.viewmodels.ContactListViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.ContactListViewModelFactory
import io.trtc.tuikit.atomicx.contactlist.viewmodels.DefaultContactItem
import io.trtc.tuikit.atomicxcore.api.ContactInfo
import io.trtc.tuikit.atomicxcore.api.ContactListStore

val LocalContactViewModel =
    compositionLocalOf<ContactListViewModel> { error("No ViewModel provided") }

@Composable
fun ContactList(
    modifier: Modifier = Modifier,
    contactListViewModelFactory: ContactListViewModelFactory = ContactListViewModelFactory(
        ContactListStore.create()
    ),
    onGroupClick: (ContactInfo) -> Unit = {},
    onContactClick: (ContactInfo) -> Unit = {},
) {
    val colors = LocalTheme.current.colors
    val contactViewModel =
        viewModel(ContactListViewModel::class, factory = contactListViewModelFactory)
    val friendList by contactViewModel.friendList.collectAsState(emptyList())

    var isShowBlacklist by remember { mutableStateOf(false) }
    var isShowMyGroupList by remember { mutableStateOf(false) }
    var isShowFriendApplication by remember { mutableStateOf(false) }
    var isShowGroupApplication by remember { mutableStateOf(false) }

    val defaultItems = contactViewModel.getDefaultItems(
        onNavigateToMyGroup = {
            isShowMyGroupList = true
        },
        onNavigateToBlacklist = {
            isShowBlacklist = true
        },
        onNavigateToFriendApplications = {
            isShowFriendApplication = true
        }, onNavigateToGroupApplications = {
            isShowGroupApplication = true
        }
    )

    MyGroupDialog(isShowMyGroupList, onDismiss = {
        isShowMyGroupList = false
    }) {
        onGroupClick(it)
    }

    BlacklistDialog(isShowBlacklist, onDismiss = {
        isShowBlacklist = false
    }) {
        onContactClick(it)
    }
    FriendApplicationDialog(isShowFriendApplication) {
        isShowFriendApplication = false
    }

    GroupApplicationDialog(isShowGroupApplication, contactListStore = contactViewModel.contactListStore) {
        isShowGroupApplication = false
    }

    CompositionLocalProvider(
        LocalContactViewModel provides contactViewModel,
    ) {
        Box(
            modifier = modifier
                .background(color = colors.bgColorOperate)
        ) {

            var defaultItemsHeightPx by remember { mutableStateOf(0) }
            var defaultOffsetY by remember { mutableStateOf(0f) }
            val headerVisibleHeightDp = with(LocalDensity.current) {
                (defaultItemsHeightPx + defaultOffsetY)
                    .coerceIn(0f, defaultItemsHeightPx.toFloat())
                    .toDp()
            }

            AZOrderedList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = headerVisibleHeightDp),
                header = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        defaultItems.forEach { item ->
                            DefaultContactItem(
                                item = item,
                                onClick = { clickedItem -> clickedItem.onClick() }
                            )
                        }
                    }
                },
                dataSource = friendList.map { c ->
                    AZOrderedListItem(
                        key = c.contactID,
                        label = c.displayName,
                        avatarUrl = c.avatarURL,
                        extraData = c
                    )
                },
                onItemClick = { item -> onContactClick(item.extraData) }
            )
        }
    }
}

@Composable
fun DefaultContactItem(
    item: DefaultContactItem,
    onClick: (DefaultContactItem) -> Unit
) {
    val colors = LocalTheme.current.colors
    val badgeCount by item.badgeCount.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick(item) }
            .background(color = colors.bgColorInput)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(item.titleResID),
            color = colors.textColorPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (badgeCount > 0) {
                Badge(text = badgeCount.toString())
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textColorSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}