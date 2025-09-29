package io.trtc.tuikit.atomicx.messagelist.utils

import android.content.Context
import android.widget.Toast

fun Context.toast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    if (message.isNullOrEmpty()) {
        return
    }
    Toast.makeText(this, message, duration).show()
}

fun Context.toast(duration: Int = Toast.LENGTH_SHORT, message: () -> String?) {
    toast(message(), duration)
}
