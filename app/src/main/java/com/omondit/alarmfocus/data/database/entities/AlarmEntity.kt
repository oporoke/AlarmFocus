package com.omondit.alarmfocus.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omondit.alarmfocus.domain.model.AlarmTime
import com.omondit.alarmfocus.domain.model.RepeatSchedule

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val repeatDays: String = RepeatSchedule.once().toJson(), // JSON serialized RepeatSchedule
    val soundUri: String? = null, // null = default ultra-loud sound
    val soundName: String = "Ultra Loud Default", // Display name for sound
    val missionType: String = "NONE", // For D6 integration
    val missionConfig: String = "{}", // JSON config for missions
    val vibrationEnabled: Boolean = true,
    val volume: Float = 1.0f, // 0.7 to 1.0 (70% to 100%)
    val skipNextAlarm: Boolean = false, // One-time skip
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggered: Long? = null,
    val isActive: Boolean = false, // Currently ringing
    val nextScheduledTime: Long? = null, // Cache for next occurrence
    val snoozeCount: Int = 0, // Track snooze attempts (for analytics)
    val successfulWakeups: Int = 0, // Track successful dismissals
    val notes: String = "" // User notes for this alarm
) {
    fun toAlarmTime(): AlarmTime = AlarmTime(hour, minute)

    fun getRepeatSchedule(): RepeatSchedule = RepeatSchedule.fromJson(repeatDays)

    fun getDisplayTime(is24Hour: Boolean = true): String = toAlarmTime().toFormattedString(is24Hour)

    fun isRepeating(): Boolean = getRepeatSchedule().type != RepeatSchedule.RepeatType.ONCE

    fun shouldTriggerToday(): Boolean {
        val calendar = java.util.Calendar.getInstance()
        return getRepeatSchedule().shouldTriggerOn(calendar)
    }

    fun getSuccessRate(): Float {
        val totalTriggers = snoozeCount + successfulWakeups
        return if (totalTriggers > 0) successfulWakeups.toFloat() / totalTriggers else 0f
    }
}
