package io.trtc.tuikit.atomicx.albumpicker.util

import android.content.pm.PackageManager
import android.util.Log
import com.tencent.qcloud.tuicore.ServiceInitializer

object AlbumPickerUtil {
    private const val TAG = "AlbumPickerUtil"

    @JvmStatic
    fun getAppName(): String {
        var appName = ""
        val context = ServiceInitializer.getAppContext()
        val packageManager = context.packageManager
        try {
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(applicationInfo)
            val labelCharSequence = applicationInfo.loadLabel(packageManager)
            if (labelCharSequence.isNotEmpty()) {
                appName = labelCharSequence.toString()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "getAppName exception:${e.message}")
        }
        return appName
    }
} 