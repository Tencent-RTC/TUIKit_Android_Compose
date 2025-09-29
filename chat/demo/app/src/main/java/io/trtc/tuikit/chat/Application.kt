package io.trtc.tuikit.chat

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV

class Application : Application() {

    override fun onCreate() {

        super.onCreate()

        MMKV.initialize(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}
