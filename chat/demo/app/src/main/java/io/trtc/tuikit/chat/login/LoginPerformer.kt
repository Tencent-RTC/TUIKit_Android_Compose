package io.trtc.tuikit.chat.login

import android.app.Activity
import android.content.Intent
import com.tencent.mmkv.MMKV
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.chat.MainActivity
import io.trtc.tuikit.chat.R
import io.trtc.tuikit.chat.common.AppConstants
import io.trtc.tuikit.chat.common.CallKitInitializer
import io.trtc.tuikit.chat.common.CustomerServiceInitializer
import io.trtc.tuikit.chat.common.WelcomeMessageSender
import io.trtc.tuikit.chat.uikit.components.widgets.Toast

internal fun performLogin(
    activity: Activity,
    sdkAppId: Int,
    userId: String,
    userSig: String,
    loginType: String,
    onSuccess: (() -> Unit)? = null
) {
    LoginStore.shared.login(
        activity, sdkAppId, userId, userSig,
        object : CompletionHandler {
            override fun onSuccess() {
                CallKitInitializer.init(activity, sdkAppId, userId, userSig)
                CustomerServiceInitializer.init(activity, sdkAppId, userId, userSig)
                WelcomeMessageSender.scheduleWelcomeMessage(activity)
                MMKV.defaultMMKV().encode(AppConstants.KEY_LOGIN_USER, userId)
                MMKV.defaultMMKV().encode(AppConstants.KEY_LOGIN_TYPE, loginType)
                onSuccess?.invoke()
                activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
            }

            override fun onFailure(code: Int, desc: String) {
                Toast.error(activity, activity.getString(R.string.compose_demo_login_failed, desc))
            }
        }
    )
}
