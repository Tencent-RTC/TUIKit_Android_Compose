package io.trtc.tuikit.atomicx.messageinput.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.audiorecorder.AudioRecorderView
import io.trtc.tuikit.atomicx.audiorecorder.AudioRecorderViewConfig
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.emojipicker.ui.EmojiPicker
import io.trtc.tuikit.atomicx.messageinput.config.ChatMessageInputConfig
import io.trtc.tuikit.atomicx.messageinput.config.MessageInputConfigProtocol
import io.trtc.tuikit.atomicx.messageinput.utils.KeyboardActionType
import io.trtc.tuikit.atomicx.messageinput.utils.rememberKeyboardState
import io.trtc.tuikit.atomicx.messageinput.viewmodels.AUDIO_MAX_RECORD_TIME
import io.trtc.tuikit.atomicx.messageinput.viewmodels.AUDIO_MIN_RECORD_TIME
import io.trtc.tuikit.atomicx.messageinput.viewmodels.MessageInputViewModel
import io.trtc.tuikit.atomicx.messageinput.viewmodels.MessageInputViewModelFactory
import io.trtc.tuikit.atomicxcore.api.message.MessageInputStore

@Composable
fun MessageInput(
    conversationID: String,
    modifier: Modifier = Modifier,
    config: MessageInputConfigProtocol = ChatMessageInputConfig(),
    messageInputViewModelFactory: MessageInputViewModelFactory =
        MessageInputViewModelFactory(MessageInputStore.create(conversationID)),
) {
    val messageInputViewModel =
        viewModel(MessageInputViewModel::class, factory = messageInputViewModelFactory)
    MessageInput(modifier = modifier, config = config, messageInputViewModel = messageInputViewModel)
}

@Composable
private fun MessageInput(
    modifier: Modifier,
    config: MessageInputConfigProtocol,
    messageInputViewModel: MessageInputViewModel
) {
    val keyboardState = rememberKeyboardState()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val colors = LocalTheme.current.colors
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardHeight by keyboardState.keyboardHeight
    val keyboardActionState by keyboardState.keyboardActionState
    val keyboardMaxHeight by keyboardState.keyboardMaxHeight

    val navigationBarsHeight =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val defaultPanelHeight = 330.dp
    var savedKeyboardMaxHeight by remember { mutableStateOf(defaultPanelHeight) }

    var currentInputState by remember { mutableStateOf(InputState.NONE) }
    var inputText by remember { mutableStateOf("") }
    val editTextState = rememberAndroidEditTextState()
    var isShowActions by remember { mutableStateOf(false) }


    var targetPanelHeight by remember { mutableStateOf(0.dp) }
    var isUserKeyboardHeight by remember { mutableStateOf(false) }
    var isUserKeyboardMaxHeight by remember { mutableStateOf(false) }

    val animatedPanelHeight by animateDpAsState(
        targetValue = targetPanelHeight,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
    )

    val finalPanelHeight =
        if (isUserKeyboardMaxHeight) keyboardMaxHeight.takeIf { it > 0.dp }
            ?: defaultPanelHeight else if (!isUserKeyboardHeight) animatedPanelHeight else keyboardHeight

    fun transitionToState(newState: InputState) {
        val oldState = currentInputState
        currentInputState = newState

        println("transitionToState: $oldState -> $newState")
        when {
            oldState == InputState.NONE && newState == InputState.SHOW_KEYBOARD -> {
                isUserKeyboardHeight = true
                isUserKeyboardMaxHeight = false
                editTextState.forceRequestFocus()
                keyboardController?.show()
                targetPanelHeight = keyboardMaxHeight
            }

            oldState == InputState.NONE && newState == InputState.SHOW_EMOJI_PANEL -> {
                isUserKeyboardHeight = false
                isUserKeyboardMaxHeight = false

                editTextState.requestFocus()
                keyboardController?.hide()

                targetPanelHeight = keyboardMaxHeight.takeIf { it > 0.dp } ?: savedKeyboardMaxHeight
            }

            oldState == InputState.SHOW_EMOJI_PANEL && newState == InputState.NONE -> {
                isUserKeyboardHeight = false
                isUserKeyboardMaxHeight = false

                targetPanelHeight = 0.dp
            }

            oldState == InputState.SHOW_EMOJI_PANEL && newState == InputState.SHOW_KEYBOARD -> {
                isUserKeyboardHeight = false
                isUserKeyboardMaxHeight = true
                editTextState.forceRequestFocus()
                keyboardController?.show()
            }

            oldState == InputState.SHOW_KEYBOARD && newState == InputState.NONE -> {
                isUserKeyboardHeight = true
                isUserKeyboardMaxHeight = false

                editTextState.clearFocus()
                keyboardController?.hide()
                targetPanelHeight = 0.dp
            }

            oldState == InputState.SHOW_KEYBOARD && newState == InputState.SHOW_EMOJI_PANEL -> {
                isUserKeyboardHeight = false
                isUserKeyboardMaxHeight = true
                editTextState.hideKeyboardKeepFocus()
                targetPanelHeight = keyboardMaxHeight
            }
        }
    }

    LaunchedEffect(keyboardMaxHeight) {
        if (keyboardMaxHeight > 0.dp) {
            savedKeyboardMaxHeight = keyboardMaxHeight
        }
    }

    LaunchedEffect(keyboardActionState) {
        println("Keyboard state changed: $keyboardActionState")
        when (keyboardActionState) {
            KeyboardActionType.Hiding -> {
                if (currentInputState == InputState.SHOW_KEYBOARD) {
                    transitionToState(InputState.NONE)
                }
            }

            KeyboardActionType.Hided -> {}
            KeyboardActionType.Showed -> {}
            KeyboardActionType.Showing -> {
                if (currentInputState == InputState.SHOW_EMOJI_PANEL) {
                    transitionToState(InputState.SHOW_KEYBOARD)
                }
            }
        }
    }

    LaunchedEffect(currentInputState) {
        editTextState.setCursorVisible(
            currentInputState == InputState.SHOW_KEYBOARD ||
                    currentInputState == InputState.SHOW_EMOJI_PANEL
        )
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.bgColorOperate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (config.isShowMore) {
                        InputIconButton(onClick = {
                            isShowActions = true
                        }) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.message_input_more_icon),
                                tint = colors.textColorLink,
                                contentDescription = "more"
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Row(
                        modifier =
                            Modifier
                                .weight(1f)
                                .focusable(false)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }) {
                                    if (currentInputState != InputState.SHOW_KEYBOARD) {
                                        transitionToState(InputState.SHOW_KEYBOARD)
                                    }
                                }
                                .background(
                                    shape = RoundedCornerShape(18.dp),
                                    color = colors.bgColorInput
                                )
                                .heightIn(min = 36.dp, max = 120.dp)
                                .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AndroidEditText(
                            text = inputText,
                            onTextChange = { newText ->
                                inputText = newText
                            },
                            modifier = Modifier.weight(1f),
                            state = editTextState,
                            hint = stringResource(R.string.message_input_edit_text_hint),
                            textColor = colors.textColorPrimary,
                            hintColor = colors.textColorTertiary,
                            textSize = 14.sp,
                            maxLines = 6,
                            onFocusChanged = { isFocused ->
                                if (isFocused && currentInputState == InputState.NONE) {
                                    transitionToState(InputState.SHOW_KEYBOARD)
                                }
                            },
                            onSendMessage = {
                                if (inputText.isNotEmpty()) {
                                    messageInputViewModel.sendTextMessage(activity, inputText)
                                    inputText = ""
                                    editTextState.setText("")
                                }
                            }
                        )

                        Box(
                            modifier = Modifier
                                .clickable(
                                    onClick = {
                                        when (currentInputState) {
                                            InputState.SHOW_KEYBOARD -> {
                                                transitionToState(InputState.SHOW_EMOJI_PANEL)
                                            }

                                            InputState.SHOW_EMOJI_PANEL -> {
                                                transitionToState(InputState.SHOW_KEYBOARD)
                                            }

                                            InputState.NONE -> {
                                                transitionToState(InputState.SHOW_EMOJI_PANEL)
                                            }
                                        }
                                    }, role = Role.Button, indication = LocalIndication.current,
                                    interactionSource = remember { MutableInteractionSource() })
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                painter = painterResource(
                                    when (currentInputState) {
                                        InputState.SHOW_KEYBOARD -> R.drawable.message_input_emoji_icon
                                        InputState.SHOW_EMOJI_PANEL -> R.drawable.message_input_keyboard_icon
                                        InputState.NONE -> R.drawable.message_input_emoji_icon
                                    }
                                ),
                                tint = colors.textColorLink,
                                contentDescription = ""
                            )
                        }
                    }
                    if (config.isShowAudioRecorder) {
                        Spacer(modifier = Modifier.width(8.dp))

                        AudioRecorderView(
                            modifier = Modifier.padding(8.dp),
                            config = AudioRecorderViewConfig(
                                enableAIDeNoise = true,
                                minDurationMs = AUDIO_MIN_RECORD_TIME,
                                maxDurationMs = AUDIO_MAX_RECORD_TIME,
                            )
                        ) { path, durationMs ->
                            if (!path.isNullOrEmpty()) {
                                messageInputViewModel.sendAudioMessage(path, durationMs / 1000)
                            }
                        }
                    }
                    if (config.isShowPhotoTaker) {
                        InputIconButton(onClick = {
                            transitionToState(InputState.NONE)
                            messageInputViewModel.recordVideoAndSend(activity ?: context)
                        }) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.message_input_camera_icon),
                                tint = colors.textColorLink,
                                contentDescription = "camera"
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(finalPanelHeight - navigationBarsHeight)
            ) {
                if (currentInputState == InputState.SHOW_EMOJI_PANEL) {
                    EmojiPicker(
                        modifier = Modifier
                            .fillMaxSize(),
                        onEmojiClick = { emojiGroup, emoji ->
                            editTextState.insertText(emoji.key)
                        }, onSendClick = {
                            if (inputText.isNotEmpty()) {
                                messageInputViewModel.sendTextMessage(activity, inputText)
                                inputText = ""
                                editTextState.setText("")
                            }
                        }, onDeleteClick = {
                            editTextState.deleteAtCursor()
                        }
                    )
                }
            }

        }

        ActionsDialog(
            isVisible = isShowActions,
            actions = messageInputViewModel.getActions(),
            onDismiss = { isShowActions = false },
            onActionClick = { action ->
                action.onClick()
                isShowActions = false
            }
        )
    }
}
