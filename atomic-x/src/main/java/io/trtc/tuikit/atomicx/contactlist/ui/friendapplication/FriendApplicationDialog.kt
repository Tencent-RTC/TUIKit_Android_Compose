package io.trtc.tuikit.atomicx.contactlist.ui.friendapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.contactlist.viewmodels.FriendApplicationViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.FriendApplicationViewModelFactory
import io.trtc.tuikit.atomicxcore.api.ContactListStore
import io.trtc.tuikit.atomicxcore.api.FriendApplicationInfo


@Composable
fun FriendApplicationDialog(isVisible: Boolean, onDismiss: () -> Unit) {
    if (isVisible) {
        FullScreenDialog(
            onDismissRequest = onDismiss,
        ) {

            var currentApplication by remember { mutableStateOf<FriendApplicationInfo?>(null) }

            FriendApplication(
                onBackClick = { onDismiss() },
                onApplicationClick = { application ->
                    currentApplication = application
                },
                friendApplicationViewModelFactory = FriendApplicationViewModelFactory(
                    ContactListStore.create()
                )
            )
            if (currentApplication != null) {
                FullScreenDialog(
                    onDismissRequest = onDismiss,
                ) {

                    val friendApplicationViewModel = viewModel(
                        FriendApplicationViewModel::class,
                        factory = FriendApplicationViewModelFactory(ContactListStore.create())
                    )

                    FriendApplicationDetail(
                        application = currentApplication!!,
                        onBackClick = { currentApplication = null },
                        onAccept = {
                            friendApplicationViewModel.acceptFriendApplication(currentApplication!!)
                            currentApplication = null
                        },
                        onRefuse = {
                            friendApplicationViewModel.refuseFriendApplication(currentApplication!!)
                            currentApplication = null
                        }
                    )
                }
            }

        }
    }
}