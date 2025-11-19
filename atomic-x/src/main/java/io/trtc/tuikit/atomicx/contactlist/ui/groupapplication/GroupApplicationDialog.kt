package io.trtc.tuikit.atomicx.contactlist.ui.groupapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.contactlist.viewmodels.GroupApplicationViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.GroupApplicationViewModelFactory
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore
import io.trtc.tuikit.atomicxcore.api.contact.GroupApplicationInfo


@Composable
fun GroupApplicationDialog(isVisible: Boolean, contactListStore: ContactListStore, onDismiss: () -> Unit) {
    if (isVisible) {
        FullScreenDialog(
            onDismissRequest = onDismiss,
        ) {

            var currentApplication by remember { mutableStateOf<GroupApplicationInfo?>(null) }

            GroupApplication(
                onBackClick = { onDismiss() },
                onApplicationClick = { application ->
                    currentApplication = application
                },
                groupApplicationViewModelFactory = GroupApplicationViewModelFactory(contactListStore)
            )
            if (currentApplication != null) {
                FullScreenDialog(
                    onDismissRequest = onDismiss,
                ) {

                    val groupApplicationViewModel = viewModel(
                        GroupApplicationViewModel::class,
                        factory = GroupApplicationViewModelFactory(contactListStore)
                    )

                    GroupApplicationDetail(
                        application = currentApplication!!,
                        onBackClick = { currentApplication = null },
                        onAccept = {
                            groupApplicationViewModel.acceptGroupApplication(currentApplication!!)
                            currentApplication = null
                        },
                        onRefuse = {
                            groupApplicationViewModel.refuseGroupApplication(currentApplication!!)
                            currentApplication = null
                        }
                    )
                }
            }

        }
    }
}