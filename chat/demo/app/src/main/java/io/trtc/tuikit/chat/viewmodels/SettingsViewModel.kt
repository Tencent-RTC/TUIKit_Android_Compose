package io.trtc.tuikit.chat.viewmodels

import androidx.lifecycle.ViewModel
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.atomicxcore.api.login.UserProfile


class SettingsViewModel : ViewModel() {
    val loginUserInfo = LoginStore.shared.loginState.loginUserInfo
}

val UserProfile.displayName: String
    get() = when {
        !nickname.isNullOrEmpty() -> nickname
        else -> userID
    }.toString()