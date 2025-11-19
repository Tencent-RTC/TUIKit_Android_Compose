package io.trtc.tuikit.atomicx.contactlist.ui.addfriendandgroup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Toast
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.contactlist.utils.displayName
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddContactAndGroupUiState
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddContactAndGroupViewModel
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddContactAndGroupViewModelFactory
import io.trtc.tuikit.atomicx.contactlist.viewmodels.AddType
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo
import io.trtc.tuikit.atomicxcore.api.contact.ContactListStore
import io.trtc.tuikit.atomicxcore.api.contact.ContactType

enum class FlowStep {
    SEARCH,
    CONTACT_DETAIL,
    ADD_FRIEND_FORM,
    GROUP_JOIN_FORM
}

@Composable
fun AddContactAndGroupBottomSheet(
    addType: AddType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModelFactory: AddContactAndGroupViewModelFactory = AddContactAndGroupViewModelFactory(
        ContactListStore.create()
    )
) {
    val viewModel: AddContactAndGroupViewModel = viewModel(
        factory = viewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val colors = LocalTheme.current.colors
    var currentStep by remember { mutableStateOf(FlowStep.SEARCH) }
    var selectedResult by remember { mutableStateOf<ContactInfo?>(null) }

    LaunchedEffect(Unit) {
        viewModel.clearSearchResults()
    }

    LaunchedEffect(uiState.requestResult) {
        uiState.requestResult?.let { result ->
            if (result.isSuccess) {
                Toast.success(context, result.message)
            } else {
                Toast.error(context, result.message)
            }

            viewModel.clearRequestResult()
            onDismiss()
        }
    }

    FullScreenDialog(
        onDismissRequest = { viewModel.clearSearchResults();onDismiss() },
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { viewModel.clearSearchResults();onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.bgColorOperate)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    FlowHeader(
                        currentStep = currentStep,
                        addType = addType,
                        onDismiss = { viewModel.clearSearchResults();onDismiss() },
                        onBack = {
                            when (currentStep) {
                                FlowStep.CONTACT_DETAIL -> currentStep = FlowStep.SEARCH
                                FlowStep.ADD_FRIEND_FORM -> currentStep = FlowStep.CONTACT_DETAIL
                                FlowStep.GROUP_JOIN_FORM -> currentStep = FlowStep.SEARCH
                                else -> {
                                    viewModel.clearSearchResults();onDismiss()
                                }
                            }
                        }
                    )

                    // Content based on current step
                    when (currentStep) {
                        FlowStep.SEARCH -> {
                            SearchInterface(
                                addType = addType,
                                uiState = uiState,
                                viewModel = viewModel,
                                keyboardController = keyboardController,
                                context = context,
                                onResultFound = { result ->
                                    if (result.isContact == true || result.isInGroup == true) {
                                        return@SearchInterface
                                    }
                                    selectedResult = result
                                    currentStep = if (addType == AddType.CONTACT) {
                                        FlowStep.CONTACT_DETAIL
                                    } else {
                                        FlowStep.GROUP_JOIN_FORM
                                    }
                                }
                            )
                        }

                        FlowStep.CONTACT_DETAIL -> {
                            selectedResult?.let { result ->
                                ContactDetail(
                                    result = result,
                                    onAddContact = {
                                        currentStep = FlowStep.ADD_FRIEND_FORM
                                    }
                                )
                            }
                        }

                        FlowStep.ADD_FRIEND_FORM -> {
                            selectedResult?.let { result ->
                                AddFriendForm(
                                    contactInfo = result,
                                    onCompleted = { onDismiss() }
                                )
                            }
                        }

                        FlowStep.GROUP_JOIN_FORM -> {
                            selectedResult?.let { result ->
                                GroupJoinForm(
                                    result = result,
                                    onSendRequest = { result, verificationMessage ->
                                        viewModel.joinGroup(result, verificationMessage)
                                    },
                                    isLoading = uiState.isAddingContact
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowHeader(
    currentStep: FlowStep,
    addType: AddType,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors

    Column(modifier = modifier.fillMaxWidth()) {
        // Top Navigation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgColorOperate)
                .padding(16.dp),
        ) {
            Text(
                text = if (currentStep == FlowStep.SEARCH) stringResource(R.string.base_component_cancel)
                else stringResource(R.string.contact_list_back),
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = colors.textColorLink,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (currentStep == FlowStep.SEARCH) onDismiss() else onBack()
                    }
            )

            Text(
                modifier = Modifier.align(Alignment.Center),
                text = when (currentStep) {
                    FlowStep.SEARCH -> if (addType == AddType.CONTACT) stringResource(R.string.contact_list_add_contact) else stringResource(
                        R.string.contact_list_join_group
                    )

                    FlowStep.CONTACT_DETAIL -> stringResource(R.string.contact_list_contact_info)
                    FlowStep.ADD_FRIEND_FORM -> stringResource(R.string.contact_list_add_contact)
                    FlowStep.GROUP_JOIN_FORM -> stringResource(R.string.contact_list_group_info)
                },
                color = colors.textColorPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center
            )
        }

    }
}

@Composable
private fun SearchInterface(
    addType: AddType,
    uiState: AddContactAndGroupUiState,
    viewModel: AddContactAndGroupViewModel,
    keyboardController: SoftwareKeyboardController?,
    onResultFound: (ContactInfo) -> Unit,
    context: android.content.Context
) {
    var hasSearched by remember { mutableStateOf(false) }
    val colors = LocalTheme.current.colors
    if (uiState.searchKeyword.isEmpty()) {
        hasSearched = false
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SearchField(
            addType = addType,
            searchKeyword = uiState.searchKeyword,
            onSearchKeywordChange = viewModel::updateSearchKeyword,
            onSearch = {
                hasSearched = true
                if (addType == AddType.CONTACT) {
                    viewModel.searchContact()
                } else {
                    viewModel.searchGroup()
                }
                keyboardController?.hide()
            },
            modifier = Modifier.padding(16.dp)
        )

        if (addType == AddType.CONTACT && uiState.currentUserId.isNotEmpty() && uiState.searchKeyword.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgColorOperate)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.contact_list_my_user_id)}：${uiState.currentUserId}",
                    color = colors.textColorPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if ((addType == AddType.CONTACT && uiState.addFriendInfo != null) || (addType == AddType.GROUP && uiState.joinGroupInfo != null)) {

            SearchResult(
                result = (if (addType == AddType.GROUP) uiState.joinGroupInfo else uiState.addFriendInfo)!!,
                onClick = { result ->
                    onResultFound(result)
                }
            )
        } else if (!uiState.isSearching && uiState.searchKeyword.isNotEmpty() && hasSearched) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Image(
                    modifier = Modifier.size(42.dp),
                    painter = painterResource(R.drawable.contact_list_add_more_no_information_icon),
                    contentDescription = "no information"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No Information", fontSize = 16.sp, color = colors.textColorSecondary)
            }
        }
    }
}

@Composable
private fun SearchField(
    addType: AddType,
    searchKeyword: String,
    onSearchKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTheme.current.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.bgColorInput,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "search",
            tint = colors.textColorSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        BasicTextField(
            value = searchKeyword,
            onValueChange = onSearchKeywordChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = colors.textColorPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.W400,
                letterSpacing = (-0.024).sp
            ),
            cursorBrush = SolidColor(colors.textColorLink),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (searchKeyword.isEmpty()) {
                    Text(
                        text = when (addType) {
                            AddType.CONTACT -> stringResource(R.string.contact_list_user_id)
                            AddType.GROUP -> stringResource(R.string.contact_list_group_id)
                        },
                        color = colors.textColorSecondary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W400,
                        letterSpacing = (-0.024).sp
                    )
                }
                innerTextField()
            }
        )

        if (searchKeyword.isNotEmpty()) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.contact_list_search),
                color = colors.textColorLink,
                fontSize = 17.sp,
                fontWeight = FontWeight.W500,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSearch() }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun SearchResult(
    result: ContactInfo,
    onClick: (ContactInfo) -> Unit
) {
    val colors = LocalTheme.current.colors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(result) },
        colors = CardDefaults.cardColors(containerColor = colors.bgColorOperate),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (result.type == ContactType.USER) {
                Avatar(
                    url = result.avatarURL,
                    name = result.displayName,
                )
            } else {
                Avatar(
                    url = result.avatarURL,
                    name = result.displayName,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.displayName,
                    color = colors.textColorPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: ${result.contactID}",
                    color = colors.textColorSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400
                )
            }

            var alreadyTips: String? = null
            if (result.type == ContactType.USER) {
                if (result.isContact == true) {
                    alreadyTips = stringResource(R.string.contact_list_already_is_friend)
                }
            } else {
                if (result.isInGroup == true) {
                    alreadyTips = stringResource(R.string.contact_list_already_in_group)
                }
            }
            alreadyTips?.let {
                Text(
                    text = alreadyTips,
                    color = colors.textColorSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
    }
} 