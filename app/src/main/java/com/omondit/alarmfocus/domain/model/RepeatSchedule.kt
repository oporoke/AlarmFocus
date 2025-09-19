package com.omondit.alarmfocus.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import java.util.Calendar

@Parcelize
data class RepeatSchedule(
    val days: Set<DayOfWeek> = emptySet(),
    val type: RepeatType = RepeatType.ONCE
) : Parcelable {

    enum class RepeatType {
        ONCE, DAILY, WEEKDAYS, WEEKENDS, CUSTOM
    }

    enum class DayOfWeek(val calendarValue: Int, val displayName: String, val shortName: String) {
        SUNDAY(Calendar.SUNDAY, "Sunday", "Sun"),
        MONDAY(Calendar.MONDAY, "Monday", "Mon"),
        TUESDAY(Calendar.TUESDAY, "Tuesday", "Tue"),
        WEDNESDAY(Calendar.WEDNESDAY, "Wednesday", "Wed"),
        THURSDAY(Calendar.THURSDAY, "Thursday", "Thu"),
        FRIDAY(Calendar.FRIDAY, "Friday", "Fri"),
        SATURDAY(Calendar.SATURDAY, "Saturday", "Sat");

        companion object {
            fun fromCalendarValue(value: Int): DayOfWeek? {
                return values().find { it.calendarValue == value }
            }

            fun weekdays(): Set<DayOfWeek> {
                return setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
            }

            fun weekends(): Set<DayOfWeek> {
                return setOf(SATURDAY, SUNDAY)
            }

            fun all(): Set<DayOfWeek> {
                return values().toSet()
            }
        }
    }

    companion object {
        fun once(): RepeatSchedule = RepeatSchedule(type = RepeatType.ONCE)

        fun daily(): RepeatSchedule = RepeatSchedule(
            days = DayOfWeek.all(),
            type = RepeatType.DAILY
        )

        fun weekdays(): RepeatSchedule = RepeatSchedule(
            days = DayOfWeek.weekdays(),
            type = RepeatType.WEEKDAYS
        )

        fun weekends(): RepeatSchedule = RepeatSchedule(
            days = DayOfWeek.weekends(),
            type = RepeatType.WEEKENDS
        )

        fun custom(days: Set<DayOfWeek>): RepeatSchedule = RepeatSchedule(
            days = days,
            type = RepeatType.CUSTOM
        )

        fun fromJson(json: String): RepeatSchedule {
            return try {
                val jsonArray = JSONArray(json)
                val dayNames = mutableSetOf<DayOfWeek>()

                for (i in 0 until jsonArray.length()) {
                    val dayName = jsonArray.getString(i)
                    DayOfWeek.values().find { it.name == dayName }?.let {
                        dayNames.add(it)
                    }
                }

                val type = when {
                    dayNames.isEmpty() -> RepeatType.ONCE
                    dayNames.size == 7 -> RepeatType.DAILY
                    dayNames == DayOfWeek.weekdays() -> RepeatType.WEEKDAYS
                    dayNames == DayOfWeek.weekends() -> RepeatType.WEEKENDS
                    else -> RepeatType.CUSTOM
                }

                RepeatSchedule(dayNames, type)
            } catch (e: Exception) {
                once() // Fallback to once if parsing fails
            }
        }
    }

    fun toJson(): String {
        val jsonArray = JSONArray()
        days.forEach { day ->
            jsonArray.put(day.name)
        }
        return jsonArray.toString()
    }

    fun getDisplayText(): String {
        return when (type) {
            RepeatType.ONCE -> "Once"
            RepeatType.DAILY -> "Every day"
            RepeatType.WEEKDAYS -> "Weekdays"
            RepeatType.WEEKENDS -> "Weekends"
            RepeatType.CUSTOM -> {
                if (days.isEmpty()) "Never"
                else days.sortedBy { it.calendarValue }.joinToString(", ") { it.shortName }
            }
        }
    }

    fun shouldTriggerOn(calendar: Calendar): Boolean {
        if (type == RepeatType.ONCE) return true

        val dayOfWeek = DayOfWeek.fromCalendarValue(calendar.get(Calendar.DAY_OF_WEEK))
        return dayOfWeek?.let { days.contains(it) } ?: false
    }

    fun getNextOccurrence(baseTime: Long): Long? {
        val calendar = Calendar.getInstance().apply { timeInMillis = baseTime }

        return when (type) {
            RepeatType.ONCE -> {
                if (baseTime > System.currentTimeMillis()) baseTime else null
            }
            else -> {
                // Find next valid day
                for (i in 0..7) { // Check up to a week ahead
                    if (shouldTriggerOn(calendar)) {
                        return calendar.timeInMillis
                    }
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                null
            }
        }
    }
}
