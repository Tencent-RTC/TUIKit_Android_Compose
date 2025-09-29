package io.trtc.tuikit.atomicx.messageinput.utils

import android.view.View
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

sealed class KeyboardActionType {
    data object Showing : KeyboardActionType()
    data object Hiding : KeyboardActionType()
    data object Showed : KeyboardActionType()
    data object Hided : KeyboardActionType()
}

class KeyboardState() {
    internal val keyboardActionState = mutableStateOf<KeyboardActionType>(KeyboardActionType.Hided)

    internal val keyboardMaxHeight = mutableStateOf<Dp>(0.dp)

    internal val keyboardHeight = mutableStateOf<Dp>(0.dp)
}

@Composable
fun rememberKeyboardState(): KeyboardState {
    val activity = LocalActivity.current
    val window = activity?.window
    val density = LocalDensity.current

    val view = remember(window) { window?.decorView }

    val keyboardState = remember(activity) { KeyboardState() }

    LaunchedEffect(activity) {
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    val imeInsets = WindowInsets.ime
    val keyboardHeight = imeInsets.asPaddingValues().calculateBottomPadding()
    keyboardState.keyboardHeight.value = keyboardHeight

    val listener = remember(keyboardState, density) {
        object : KeyBoardAdjustListener {
            override fun onStart(isShow: Boolean, keyBoardHeight: Float) {
                keyboardState.keyboardMaxHeight.value = with(density) { keyBoardHeight.toDp() }
                if (isShow) {
                    keyboardState.keyboardActionState.value = KeyboardActionType.Showing
                } else {
                    keyboardState.keyboardActionState.value = KeyboardActionType.Hiding
                }
            }

            override fun onEnd(isShow: Boolean, keyBoardHeight: Float) {
                if (isShow) {
                    keyboardState.keyboardActionState.value = KeyboardActionType.Showed
                } else {
                    keyboardState.keyboardActionState.value = KeyboardActionType.Hided
                }
            }

            override fun onProgress(isShow: Boolean, currentKeyBoardHeight: Float) {
//                keyboardState.keyboardHeight.value = with(density) { currentKeyBoardHeight.toDp() }
            }
        }
    }

    DisposableEffect(view, listener) {
        KeyboardAdjustHelper.attachKeyboardAdjustments(view, listener)

        onDispose {
            KeyboardAdjustHelper.detachKeyboardAdjustments(view)
        }
    }

    return keyboardState
}

object KeyboardAdjustHelper {
    fun attachKeyboardAdjustments(view: View?, listener: KeyBoardAdjustListener?) {
        if (view == null) {
            return
        }

        val rootView = view.rootView
        ViewCompat.setWindowInsetsAnimationCallback(
            rootView, object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                var softInputHeight: Float = 0F

                val isSoftInputVisible: Boolean
                    get() {
                        val insets = ViewCompat.getRootWindowInsets(rootView) ?: return false
                        return insets.isVisible(WindowInsetsCompat.Type.ime())
                    }

                override fun onStart(
                    animation: WindowInsetsAnimationCompat, bounds: WindowInsetsAnimationCompat.BoundsCompat
                ): WindowInsetsAnimationCompat.BoundsCompat {
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        softInputHeight = bounds.upperBound.bottom.toFloat()
                        val isSoftInputVisible = isSoftInputVisible
                        listener?.onStart(isSoftInputVisible, softInputHeight)
                    }
                    return super.onStart(animation, bounds)
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        val isSoftInputVisible = isSoftInputVisible
                        listener?.onEnd(isSoftInputVisible, softInputHeight)
                    }
                    super.onEnd(animation)
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: List<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    if (softInputHeight == 0F) {
                        return insets
                    }
                    val isSoftInputVisible = isSoftInputVisible
                    for (runningAnimation in runningAnimations) {
                        if ((runningAnimation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                            val progress = runningAnimation.interpolatedFraction
                            val currentHeight = if (isSoftInputVisible) {
                                progress * softInputHeight
                            } else {
                                (1 - progress) * softInputHeight
                            }
                            listener?.onProgress(isSoftInputVisible, currentHeight)
                            break
                        }
                    }

                    return insets
                }
            })
    }

    fun detachKeyboardAdjustments(view: View?) {
        if (view == null) {
            return
        }

        val rootView = view.rootView
        ViewCompat.setWindowInsetsAnimationCallback(rootView, null)
    }

}

interface KeyBoardAdjustListener {
    fun onStart(isShow: Boolean, keyBoardHeight: Float) {}
    fun onEnd(isShow: Boolean, keyBoardHeight: Float) {}
    fun onProgress(isShow: Boolean, currentKeyBoardHeight: Float) {}
}