package io.trtc.tuikit.chat.common

import android.content.Context
import android.util.Log
import com.tencent.cloud.tuikit.engine.call.TUICallEngine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.qcloud.tuikit.tuicallkit.TUICallKit
import com.tencent.qcloud.tuikit.tuicallkit.manager.feature.CallingBellFeature
import com.tencent.qcloud.tuikit.tuicallkit.manager.feature.CallingVibratorFeature
import com.tencent.qcloud.tuikit.tuicallkit.manager.feature.NotificationFeature

object CallKitInitializer {
    private const val TAG = "CallKitInitializer"

    fun init(context: Context, sdkAppId: Int, userId: String, userSig: String?) {
        TUICallEngine.createInstance(context).init(
            sdkAppId, userId, userSig,
            object : TUICommonDefine.Callback {
                override fun onSuccess() {
                    Log.i(TAG, "callEngine init success")
                    NotificationFeature(context).registerNotificationBannerChannel()
                    CallingBellFeature(context)
                    CallingVibratorFeature(context)
                    TUICallEngine.createInstance(context).enableMultiDeviceAbility(true, null)
                    TUICallKit.createInstance(context).enableIncomingBanner(true)
                }

                override fun onError(errCode: Int, errMsg: String) {
                    Log.e(TAG, "callEngine init failed, errCode: $errCode, errMsg: $errMsg")
                }
            })
    }
}
