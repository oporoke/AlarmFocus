package com.omondit.alarmfocus.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class AlarmTime(
    val hour: Int,
    val minute: Int
) : Parcelable {

    companion object {
        fun now(): AlarmTime {
            val calendar = Calendar.getInstance()
            return AlarmTime(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE)
            )
        }

        fun fromMillis(millis: Long): AlarmTime {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = millis
            }
            return AlarmTime(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE)
            )
        }
    }

    fun toFormattedString(is24Hour: Boolean = true): String {
        return if (is24Hour) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val amPm = if (hour < 12) "AM" else "PM"
            String.format("%d:%02d %s", displayHour, minute, amPm)
        }
    }

    fun toMillisToday(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun toMillisNextOccurrence(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis
    }

    fun isValid(): Boolean {
        return hour in 0..23 && minute in 0..59
    }
}
