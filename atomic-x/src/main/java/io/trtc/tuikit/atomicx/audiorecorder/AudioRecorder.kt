package io.trtc.tuikit.atomicx.audiorecorder

import io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore.AudioRecorderImpl
import io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore.ResultCode


interface AudioRecorderListener {
    fun onCompleted(resultCode: ResultCode, path: String?, duration: Int)
}

object AudioRecorder {
    private val instance = AudioRecorderImpl()

    val currentPower = instance._currentPower

    val currentTime = instance._recordTimeSecond

    // To use AI noise reduction, the app must depend on LiteAVSDK_Professional v12.7+ and have the feature enabled.
    // Dependency: Add to app module's build.gradle dependencies: implementation("com.tencent.liteav:LiteAVSDK_Professional:12.7.0.xxxxx")
    // For enabling permissions, see documentation: https://cloud.tencent.com/document/product/269/113290
    fun startRecord(
        filepath: String? = null,
        enableAIDeNoise: Boolean = false,
        listener: AudioRecorderListener
    ) {
        instance.startRecord(filepath, enableAIDeNoise, listener)
    }

    fun stopRecord() {
        instance.stopRecord()
    }

    fun cancelRecord() {
        instance.cancelRecord()
    }

}
