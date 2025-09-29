package io.trtc.tuikit.atomicx.contactlist.ui.mygroup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.AZOrderedList
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.AZOrderedListItem
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicx.contactlist.viewmodels.MyGroupViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.MyGroupViewModelFactory
import io.trtc.tuikit.atomicxcore.api.ContactInfo
import io.trtc.tuikit.atomicxcore.api.ContactListStore

@Composable
fun MyGroup(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    myGroupViewModelFactory: MyGroupViewModelFactory = MyGroupViewModelFactory(ContactListStore.create()),
    onGroupClick: (ContactInfo) -> Unit
) {
    val colors = LocalTheme.current.colors
    val myGroupViewModel = viewModel(MyGroupViewModel::class, factory = myGroupViewModelFactory)

    val groups by myGroupViewModel.groups.collectAsState()

    LaunchedEffect(Unit) {
        myGroupViewModel.fetchGroups()
    }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(color = colors.bgColorOperate)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable(
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
                            text = stringResource(R.string.contact_list_back),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W400,
                            color = colors.textColorLink
                        )
                    }

                    Text(
                        text = stringResource(R.string.contact_list_my_group),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600,
                        color = colors.textColorPrimary,
                        textAlign = TextAlign.Center
                    )


                    Spacer(modifier = Modifier.width(80.dp))
                }
            }

            // Divider
            HorizontalDivider(
                thickness = 0.5.dp,
                color = colors.strokeColorSecondary
            )
            if (groups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.contact_list_no_group),
                        fontSize = 17.sp,
                        color = colors.textColorSecondary
                    )
                }
            } else {
                AZOrderedList(
                    modifier = Modifier.fillMaxSize(),
                    dataSource = groups.map { groupInfo ->
                        AZOrderedListItem(
                            key = groupInfo.contactID,
                            label = groupInfo.displayName,
                            avatarUrl = groupInfo.avatarURL,
                            extraData = groupInfo
                        )
                    },
                    onItemClick = { item -> onGroupClick(item.extraData) }
                )
            }
        }

    }
}

 