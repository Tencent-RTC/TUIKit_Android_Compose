package io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class AudioRecorderSystem(private val context: Context) : AudioRecorderInternalInterface {
    private var mediaRecorder: MediaRecorder? = null
    private var listener: RecorderListener? = null
    private var filePath: String? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var recordStartTime: Long = 0
    private var isEnableAIDeNoise: Boolean = false

    private var job: Job? = null

    override fun startRecord(filePath: String?) {
        if (isEnableAIDeNoise) {
            listener?.onCompleted(ResultCode.ERROR_USE_AI_DENOISE_NO_LITEAV_SDK, " ")
            return;
        }

        this.filePath = filePath;
        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(filePath)

                try {
                    prepare()
                    start()
                    listener?.onRecordTime(0)

                } catch (e: IOException) {
                    listener?.onCompleted(ResultCode.ERROR_RECORD_INNER_FAIL, " ")
                    reset()
                    release()
                    mediaRecorder = null
                }
            }
        } catch (e: Exception) {
            listener?.onCompleted(ResultCode.ERROR_RECORD_INNER_FAIL, " ")
            releaseResources()
        }
        createJob()
    }

    override fun setListener(listener: RecorderListener?) {
        this.listener = listener
    }

    override fun stopRecord() {
        job?.cancel()
        job = null
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null

            if (System.currentTimeMillis() - recordStartTime < MIN_DURATION) {
                listener?.onCompleted(ResultCode.ERROR_LESS_THAN_MIN_DURATION, filePath)
            } else {
                listener?.onCompleted(ResultCode.SUCCESS, filePath)
            }
        } catch (e: Exception) {
            listener?.onCompleted(ResultCode.ERROR_RECORD_INNER_FAIL, "")
            releaseResources()
        }
    }

    override fun enableAIDeNoise(enable: Boolean) {
        Log.e(TAG, "The system's audio recording does not support setting AI de noise")
        isEnableAIDeNoise = enable
    }

    private fun createJob() {
        job?.cancel()
        job = coroutineScope.launch {
            recordStartTime = System.currentTimeMillis();
            while (mediaRecorder != null) {
                try {
                    val amplitude = mediaRecorder?.maxAmplitude ?: 0
                    val db = if (amplitude > 0) {
                        20 * Math.log10(amplitude.toDouble()).toInt().coerceAtLeast(-90)
                    } else {
                        -90
                    }
                    val recordTime = System.currentTimeMillis() - recordStartTime;
                    listener?.onAmplitudeChanged(db)
                    listener?.onRecordTime(recordTime.toInt())
                } catch (e: Exception) {
                }
                delay(AMPLITUDE_UPDATE_INTERVAL)
            }
        }
    }

    private fun releaseResources() {
        try {
            job?.cancel()
            job = null

            mediaRecorder?.apply {
                reset()
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val AMPLITUDE_UPDATE_INTERVAL = 100L
        private const val MIN_DURATION = 1000
        private var TAG: String? = "AudioRecorderImpl"
    }
} 