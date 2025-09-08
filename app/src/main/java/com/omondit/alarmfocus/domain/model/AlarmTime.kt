package com.omondit.alarmfocus.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar

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
            "%02d:%02d".format(hour, minute)
        } else {
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val amPm = if (hour < 12) "AM" else "PM"
            "%d:%02d %s".format(displayHour, minute, amPm)
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
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis
    }

    fun isValid(): Boolean = hour in 0..23 && minute in 0..59
}
