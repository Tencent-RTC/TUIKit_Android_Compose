package io.trtc.tuikit.chat

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tencent.mmkv.MMKV
import io.trtc.tuikit.atomicxcore.api.login.LoginListener
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.chat.common.AppConstants
import io.trtc.tuikit.chat.customMessages.CustomLinkMessage
import io.trtc.tuikit.chat.customerservice.CustomerServiceManager
import io.trtc.tuikit.chat.login.LocalLoginActivity
import io.trtc.tuikit.chat.uikit.components.config.AppBuilderConfig
import io.trtc.tuikit.chat.viewmodels.KEY_ENABLE_READ_RECEIPT

class Application : Application() {

    private val loginListener = object : LoginListener() {
        override fun onKickedOffline() {
            redirectToLogin(R.string.compose_demo_force_offline)
        }

        override fun onLoginExpired() {
            redirectToLogin(R.string.compose_demo_login_expired)
        }
    }

    override fun onCreate() {
        super.onCreate()

        MMKV.initialize(this)

        applyLanguageFromSettings()

        MMKV.defaultMMKV().decodeBool(KEY_ENABLE_READ_RECEIPT, false).also {
            AppBuilderConfig.enableReadReceipt = it
        }

        CustomLinkMessage.registerMessageSummary()
        CustomerServiceManager.registerSummary()

        LoginStore.shared.addLoginListener(loginListener)
    }

    private fun redirectToLogin(messageResId: Int) {
        MMKV.defaultMMKV().encode(AppConstants.KEY_LOGIN_USER, "")
        MMKV.defaultMMKV().encode(AppConstants.KEY_LOGIN_TYPE, "")
        Toast.makeText(this, getString(messageResId), Toast.LENGTH_LONG).show()
        startActivity(Intent(this, LocalLoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
    }

    private fun applyLanguageFromSettings() {
        val languageTag = MMKV.defaultMMKV().decodeString(AppConstants.KEY_APP_LANGUAGE, "").orEmpty()
        val targetLocales = if (languageTag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        if (AppCompatDelegate.getApplicationLocales() != targetLocales) {
            AppCompatDelegate.setApplicationLocales(targetLocales)
        }
    }
}
