package io.trtc.tuikit.atomicx.albumpicker.impl

import android.content.Intent
import android.net.Uri
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.interfaces.ITUINotification
import io.trtc.tuikit.atomicx.albumpicker.AbstractAlbumPicker
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerConfig
import io.trtc.tuikit.atomicx.albumpicker.interfaces.AlbumPickerListener
import io.trtc.tuikit.atomicx.albumpicker.ui.picker.AlbumPickerActivity
import io.trtc.tuikit.atomicx.basecomponent.utils.ContextProvider
import java.util.UUID

class MultimediaAlbumPicker : AbstractAlbumPicker {
    companion object {
        const val TAG = "TUIMultimediaAlbumPicker"
    }

    override fun pickMedia(config: AlbumPickerConfig, listener: AlbumPickerListener) {
        val random = UUID.randomUUID().toString()
        val eventKey = "TUIMultimediaAlbumPickerEvent$random"
        val eventSubKey = "SubKeyOnPicked$random"
        lateinit var notificationListener: ITUINotification
        notificationListener = ITUINotification { key, subKey, param ->
            TUICore.unRegisterEvent(notificationListener)
            if (param == null) {
                listener.onCanceled()
                return@ITUINotification
            }
            val dataList: ArrayList<*>? = param.get("data") as? ArrayList<*>
            val transcodeDataList: ArrayList<*>? = param.get("transcodeData") as? ArrayList<*>
            if (dataList.isNullOrEmpty() && transcodeDataList.isNullOrEmpty()) {
                listener.onCanceled()
                return@ITUINotification
            }
            val resultList = mutableListOf<Pair<Uri, Boolean>>()
            if (!transcodeDataList.isNullOrEmpty()) {
                for (uri in transcodeDataList) {
                    resultList.add(uri as Uri to true)
                }
            }
            if (!dataList.isNullOrEmpty()) {
                for (uri in dataList) {
                    resultList.add(uri as Uri to false)
                }
            }
            listener.onPicked(resultList)
        }
        TUICore.registerEvent(eventKey, eventSubKey, notificationListener)
        val context = ContextProvider.appContext
        val intent = Intent(context, AlbumPickerActivity::class.java)
        intent.putExtra("eventKey", eventKey)
        intent.putExtra("eventSubKey", eventSubKey)
        intent.putExtra("config", config)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
