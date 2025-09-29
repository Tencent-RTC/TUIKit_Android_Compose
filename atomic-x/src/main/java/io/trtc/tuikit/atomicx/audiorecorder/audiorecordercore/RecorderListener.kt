package io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore


interface RecorderListener {
    fun onRecordTime(time: Int)
    fun onAmplitudeChanged(db: Int)
    fun onCompleted(resultCode: ResultCode, path: String?)
}

