package io.trtc.tuikit.atomicx.videorecorder.config;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import io.trtc.tuikit.atomicx.videorecorder.RecordMode;
import io.trtc.tuikit.atomicx.videorecorder.TakeVideoConfig;
import io.trtc.tuikit.atomicx.videorecorder.VideoQuality;
import io.trtc.tuikit.atomicx.videorecorder.utils.VideoRecorderFileUtil;
import io.trtc.tuikit.atomicx.videorecorder.utils.VideoRecorderResourceUtils;
import java.lang.reflect.Type;

public class VideoRecorderConfig {

    static final String TAG = "VideoRecorderConfig";
    static final String DEFAULT_JSON_PATH = "file:///asset/video_recorder_config/video_recorder_config.json";
    static final int DEFAULT_MAX_RECORD_DURATION_MS = 15000;
    static final int DEFAULT_MIN_RECORD_DURATION_MS = 2000;
    static final String  DEFAULT_PRIMARY_THEME_COLOR = "#147AFF";

    private static VideoRecorderConfig sInstance;
    private final int mVideoQuality = VideoQuality.MEDIUM.getValue();
    private final int mRecordMode = RecordMode.MIXED.getValue();
    private JsonObject mJsonObject;
    private TakeVideoConfig mConfig;

    public static VideoRecorderConfig getInstance() {
        if (sInstance == null) {
            synchronized (VideoRecorderConfig.class) {
                if (sInstance == null) {
                    sInstance = new VideoRecorderConfig();

                    String json = VideoRecorderFileUtil.readTextFromFile(DEFAULT_JSON_PATH);
                    Log.i(TAG, "initDefaultConfig json = " + json);

                    if (!json.isEmpty()) {
                        sInstance.mJsonObject = new Gson().fromJson(json, JsonObject.class);
                    }
                }
            }
        }
        return sInstance;
    }

    public void setConfig(TakeVideoConfig config) {
        mConfig = config;
    }

    public void setConfig(String configJsonString) {
        if (configJsonString == null || configJsonString.isEmpty()) {
            return;
        }
        Log.i(TAG, "setConfigJsonString json = " + configJsonString);
        mJsonObject = new Gson().fromJson(configJsonString, JsonObject.class);
    }

    public boolean isSupportRecordBeauty() {
        return getBoolFromJsonObject("support_record_beauty", true);
    }

    public boolean isSupportRecordAspect() {
        return getBoolFromJsonObject("support_record_aspect", true);
    }

    public boolean isSupportRecordTorch() {
        return getBoolFromJsonObject("support_record_torch", true);
    }

    public boolean isSupportRecordScrollFilter() {
        return getBoolFromJsonObject("support_record_scroll_filter", true);
    }

    public int getThemeColor() {
        if (mConfig != null && mConfig.getPrimaryColor() != null) {
            return VideoRecorderResourceUtils.parseRGB(mConfig.getPrimaryColor());
        }
        String  primaryColor = getStringFromJsonObject("primary_theme_color", DEFAULT_PRIMARY_THEME_COLOR);
        return VideoRecorderResourceUtils.parseRGB(primaryColor);
    }

    public int getMaxRecordDurationMs() {
        if (mConfig != null && mConfig.getMaxVideoDuration() != null) {
            return Math.max(mConfig.getMaxVideoDuration(), 3000);
        }
        return Math.max(getIntFromJsonObject("max_record_duration_ms", DEFAULT_MAX_RECORD_DURATION_MS), 3000);
    }

    public int getMinRecordDurationMs() {
        if (mConfig != null && mConfig.getMinVideoDuration() != null) {
            return mConfig.getMinVideoDuration();
        }
        return getIntFromJsonObject("min_record_duration_ms", DEFAULT_MIN_RECORD_DURATION_MS);
    }

    public VideoQuality getVideoQuality() {
        if (mConfig != null && mConfig.getVideoQuality() != null) {
            return mConfig.getVideoQuality();
        }

        return VideoQuality.Companion.fromInteger(
                sInstance.getIntFromJsonObject("video_quality", mVideoQuality));
    }

    public RecordMode getRecordMode() {
        if (mConfig != null && mConfig.getRecordMode() != null) {
            return mConfig.getRecordMode();
        }

        return RecordMode.Companion.fromInteger(
                sInstance.getIntFromJsonObject("record_mode", mRecordMode));
    }

    public boolean getIsDefaultFrontCamera() {
        if (mConfig != null && mConfig.getDefaultFrontCamera() != null) {
            return mConfig.getDefaultFrontCamera();
        }

        return sInstance.getBoolFromJsonObject("is_default_front_camera", false);
    }

    private int getIntFromJsonObject(String key, int defaultValue) {
        if (mJsonObject == null || !mJsonObject.has(key)) {
            return defaultValue;
        }

        return mJsonObject.get(key).getAsInt();
    }

    private boolean getBoolFromJsonObject(String key, boolean defaultValue) {
        if (mJsonObject == null || !mJsonObject.has(key)) {
            return defaultValue;
        }

        return mJsonObject.get(key).getAsBoolean();
    }

    private String getStringFromJsonObject(String key, String defaultValue) {
        if (mJsonObject == null || !mJsonObject.has(key)) {
            return defaultValue;
        }

        return mJsonObject.get(key).getAsString();
    }
}
