package io.trtc.tuikit.atomicx.messagelist.utils

object DateTimeUtils {

    /**
     * Convert seconds to smart time format with auto-scaling
     * @param totalSeconds Total seconds to convert
     * @return Formatted time string (MM:SS or HH:MM:SS)
     */
    fun formatSmartTime(totalSeconds: Int?): String {
        if (totalSeconds == null) return "00:00"
        if (totalSeconds <= 0) return "00:00"

        return if (totalSeconds < 3600) {
            // MM:SS format (e.g. 59:59)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d", minutes, seconds)
        } else {
            // HH:MM:SS format (e.g. 01:00:00)
            val hours = totalSeconds / 3600
            val remaining = totalSeconds % 3600
            val minutes = remaining / 60
            val seconds = remaining % 60
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

}