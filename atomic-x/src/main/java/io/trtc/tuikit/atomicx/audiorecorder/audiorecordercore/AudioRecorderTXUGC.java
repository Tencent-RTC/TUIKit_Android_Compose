package io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore.TXUGCAudioRecorderReflector.AudioConfig;
import io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore.TXUGCAudioRecorderReflector.RecordResult;
import io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore.TXUGCAudioRecorderReflector.TXUGCAudioRecorderReflectorListener;

public class AudioRecorderTXUGC implements AudioRecorderInternalInterface, TXUGCAudioRecorderReflectorListener {
    private final static String TAG = "TXUGCAudioRecorder";
    private final static int ERROR_LESS_THAN_MIN_DURATION = 1;
    private final static int START_RECORD_ERR_LICENCE_VERIFICATION_FAILED = -5;

    static {
        AudioRecorderSignatureChecker.getInstance().startUpdateSignature();
    }

    private final TXUGCAudioRecorderReflector mTxUgcAudioRecorderReflector;
    private final Context mContext;
    private RecorderListener mListener;
    private boolean mEnableAiDeNoise = false;

    public AudioRecorderTXUGC(Context context) {
        Log.i(TAG, "TXUGCAudioRecorder construct");
        mContext = context;
        mTxUgcAudioRecorderReflector = new TXUGCAudioRecorderReflector();
    }

    public void init() throws Exception {
        Log.i(TAG, "TXUGCAudioRecorder init");
        mTxUgcAudioRecorderReflector.init(mContext);
        mTxUgcAudioRecorderReflector.setListener(this);
    }

    @Override
    public void setListener(@Nullable RecorderListener listener) {
        mListener = listener;
    }

    @Override
    public void startRecord(@Nullable String filePath) {
        Log.i(TAG, "start record");
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.enableAIDeNoise = mEnableAiDeNoise;
        int result = mTxUgcAudioRecorderReflector.startRecord(filePath, audioConfig);
        Log.i(TAG, "start record. result : " + result);
        if (result == START_RECORD_ERR_LICENCE_VERIFICATION_FAILED) {
            handleLicenceVerificationFailed();
        } else if (result < 0) {
            mListener.onCompleted(ResultCode.ERROR_RECORD_INNER_FAIL, "");
        }
    }

    @Override
    public void stopRecord() {
        Log.i(TAG, "stop record");
        mTxUgcAudioRecorderReflector.stopRecord();
    }

    @Override
    public void enableAIDeNoise(boolean enable) {
        Log.i(TAG,
            enable ? "enable"
                   : "disable"
                    + "ai de noise");
        mEnableAiDeNoise = enable;
    }

    @Override
    public void onProgress(long milliSecond) {
        Log.i(TAG, "onProgress = " + milliSecond);
        if (mListener != null) {
            mListener.onRecordTime((int) milliSecond);
        }
    }

    @Override
    public void onComplete(RecordResult result) {
        if (mListener == null) {
            return;
        }

        Log.i(TAG, "On record complete. retCode: " + result.retCode + " videoPath:" + result.videoPath);
        if (result.retCode == ERROR_LESS_THAN_MIN_DURATION) {
            mListener.onCompleted(ResultCode.ERROR_LESS_THAN_MIN_DURATION, result.videoPath);
        } else if (result.retCode >= 0) {
            mListener.onCompleted(ResultCode.SUCCESS, result.videoPath);
        } else {
            mListener.onCompleted(ResultCode.ERROR_RECORD_INNER_FAIL, result.videoPath);
        }
    }

    private void handleLicenceVerificationFailed() {
        Log.i(TAG, "handle licence verification failed.");
        AudioRecorderSignatureChecker.ResultCode signatureResultCode =
            AudioRecorderSignatureChecker.getInstance().getSetSignatureResult();

        if (signatureResultCode == AudioRecorderSignatureChecker.ResultCode.SUCCESS) {
            mListener.onCompleted(ResultCode.ERROR_USE_AI_DENOISE_WRONG_SIGNATURE, "");
        } else {
            mListener.onCompleted(ResultCode.Companion.fromCode(signatureResultCode.getValue()), "");
        }
    }
}
