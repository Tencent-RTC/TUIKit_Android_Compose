package io.trtc.tuikit.atomicx.audiorecorder.audiorecordercore

enum class ResultCode(val code: Int) {
    SUCCESS(0),
    ERROR_CANCEL(-1),
    ERROR_RECORDING(-2),
    ERROR_STORAGE_UNAVAILABLE(-3),
    ERROR_LESS_THAN_MIN_DURATION(-4),
    ERROR_RECORD_INNER_FAIL(-5),
    ERROR_RECORD_PERMISSION_DENIED(-6),
    ERROR_USE_AI_DENOISE_NO_LITEAV_SDK(-7),
    ERROR_USE_AI_DENOISE_NO_IM_SDK(-8),
    ERROR_USE_AI_DENOISE_NO_TUI_CORE(-9),
    ERROR_USE_AI_DENOISE_NO_SIGNATURE(-10),
    ERROR_USE_AI_DENOISE_WRONG_SIGNATURE(-11);

    companion object {
        fun fromCode(code: Int): ResultCode {
            return values().firstOrNull { it.code == code } ?: ERROR_RECORD_INNER_FAIL
        }
    }
}