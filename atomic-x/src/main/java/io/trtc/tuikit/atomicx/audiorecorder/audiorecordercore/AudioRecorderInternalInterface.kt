package io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore

interface AudioRecorderInternalInterface {
    fun setListener(listener: RecorderListener?)
    fun startRecord(filePath: String? = null)
    fun stopRecord()
    fun enableAIDeNoise(enable: Boolean)
}