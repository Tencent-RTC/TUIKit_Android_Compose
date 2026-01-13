package io.trtc.tuikit.chat

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV
import io.trtc.tuikit.atomicx.basecomponent.config.AppBuilderConfig
import io.trtc.tuikit.chat.viewmodels.KEY_ENABLE_READ_RECEIPT

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        MMKV.initialize(this)

        MMKV.defaultMMKV().decodeBool(KEY_ENABLE_READ_RECEIPT, false).also {
            AppBuilderConfig.enableReadReceipt = it
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}
