package com.example.util

import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class StudyDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
) : Comparable<StudyDate> {

    fun toIsoString(): String =
        String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

    private fun toCalendar(): Calendar = Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    fun plusDays(days: Int): StudyDate {
        val cal = toCalendar()
        cal.add(Calendar.DAY_OF_MONTH, days)
        return fromCalendar(cal)
    }

    fun minusDays(days: Int): StudyDate = plusDays(-days)

    fun plusMonths(months: Int): StudyDate {
        val cal = toCalendar()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, months)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, day.coerceAtMost(maxDay))
        return fromCalendar(cal)
    }

    /**
     * 0 = Sunday, 1 = Monday, ..., 6 = Saturday
     */
    fun dayOfWeekSundayFirst(): Int {
        val cal = toCalendar()
        return cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    /**
     * 0 = Monday, 1 = Tuesday, ..., 6 = Sunday
     */
    fun dayOfWeekMondayFirst(): Int {
        val sunFirst = dayOfWeekSundayFirst()
        return if (sunFirst == 0) 6 else sunFirst - 1
    }

    fun koreanDayOfWeekShort(): String = when (dayOfWeekSundayFirst()) {
        0 -> "일"
        1 -> "월"
        2 -> "화"
        3 -> "수"
        4 -> "목"
        5 -> "금"
        else -> "토"
    }

    fun koreanFullLabel(): String =
        "${month}월 ${day}일 (${koreanDayOfWeekShort()})"

    fun koreanYearMonthDayLabel(): String =
        "${year}년 ${month}월 ${day}일 (${koreanDayOfWeekShort()})"

    fun koreanShortMonthDay(): String = "${month}/${day}"

    fun lengthOfMonth(): Int {
        val cal = toCalendar()
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Positive if target is in the future (D-N), 0 if today (D-Day), negative if past (D+N).
     */
    fun daysUntil(target: StudyDate): Int {
        val diffMs = target.toCalendar().timeInMillis - this.toCalendar().timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
    }

    override fun compareTo(other: StudyDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    companion object {
        fun today(): StudyDate = fromCalendar(Calendar.getInstance())

        fun fromCalendar(cal: Calendar): StudyDate = StudyDate(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH)
        )

        fun parse(iso: String): StudyDate {
            val parts = iso.split("-")
            if (parts.size != 3) return today()
            return StudyDate(
                year = parts[0].toIntOrNull() ?: today().year,
                month = (parts[1].toIntOrNull() ?: today().month).coerceIn(1, 12),
                day = (parts[2].toIntOrNull() ?: today().day).coerceIn(1, 31)
            )
        }

        fun daysInMonthGrid(year: Int, month: Int): List<StudyDate?> {
            val firstDay = StudyDate(year, month, 1)
            val offset = firstDay.dayOfWeekSundayFirst()
            val totalDays = firstDay.lengthOfMonth()
            val cells = mutableListOf<StudyDate?>()
            repeat(offset) { cells.add(null) }
            for (d in 1..totalDays) {
                cells.add(StudyDate(year, month, d))
            }
            while (cells.size % 7 != 0) {
                cells.add(null)
            }
            return cells
        }

        fun weekDatesAround(center: StudyDate): List<StudyDate> {
            val offsetFromSunday = center.dayOfWeekSundayFirst()
            val sunday = center.minusDays(offsetFromSunday)
            return (0..6).map { sunday.plusDays(it) }
        }
    }
}

object TimeFormatter {
    fun formatStopwatchMillis(millis: Long): String {
        val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatDurationKorean(seconds: Long): String {
        if (seconds <= 0L) return "0분"
        val hours = seconds / 3600L
        val minutes = (seconds % 3600L) / 60L
        val secs = seconds % 60L
        return when {
            hours > 0 && minutes > 0 -> "${hours}시간 ${minutes}분"
            hours > 0 -> "${hours}시간"
            minutes > 0 -> "${minutes}분"
            else -> "${secs}초"
        }
    }

    fun formatDDayBadge(daysRemaining: Int): String = when {
        daysRemaining == 0 -> "D-DAY"
        daysRemaining > 0 -> "D-$daysRemaining"
        else -> "D+${-daysRemaining}"
    }
}
