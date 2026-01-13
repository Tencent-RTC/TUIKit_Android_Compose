package io.trtc.tuikit.atomicx.messagelist.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object DateTimeUtils {

    /**
     * Calculate the interval seconds between two timestamps
     * @param timeStamp1 First timestamp in seconds
     * @param timeStamp2 Second timestamp in seconds
     * @return Absolute difference in seconds
     */
    fun getIntervalSeconds(timeStamp1: Long?, timeStamp2: Long?): Long {
        if (timeStamp1 == null || timeStamp2 == null) return 0L
        if (timeStamp1 == 0L || timeStamp2 == 0L) return 0L
        return abs(timeStamp2 - timeStamp1)
    }

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

    fun getTimeString(timeStamp: Long?): String? {
        val date = timeStamp?.let { Date(it) }
        if (date == null) return null
        if (date.time == 0L) return null

        val calendar = Calendar.getInstance()
        val customCalendar = Calendar.getInstance()
        customCalendar.firstDayOfWeek = Calendar.SATURDAY

        val now = Date()
        customCalendar.time = now
        val nowYear = customCalendar.get(Calendar.YEAR)
        val nowMonth = customCalendar.get(Calendar.MONTH)
        val nowWeekOfMonth = customCalendar.get(Calendar.WEEK_OF_MONTH)
        val nowDay = customCalendar.get(Calendar.DAY_OF_MONTH)

        customCalendar.time = date
        val dateYear = customCalendar.get(Calendar.YEAR)
        val dateMonth = customCalendar.get(Calendar.MONTH)
        val dateWeekOfMonth = customCalendar.get(Calendar.WEEK_OF_MONTH)
        val dateDay = customCalendar.get(Calendar.DAY_OF_MONTH)

        val dateFmt: DateFormat

        if (nowYear == dateYear) {
            if (nowMonth == dateMonth) {
                if (nowWeekOfMonth == dateWeekOfMonth) {
                    if (nowDay == dateDay) {
                        dateFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    } else {
                        val locale = Locale.getDefault()
                        dateFmt = SimpleDateFormat("EEEE", locale)
                    }
                } else {
                    dateFmt = SimpleDateFormat("MM/dd", Locale.getDefault())
                }
            } else {
                dateFmt = SimpleDateFormat("MM/dd", Locale.getDefault())
            }
        } else {
            dateFmt = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        }

        return dateFmt.format(date)
    }
}