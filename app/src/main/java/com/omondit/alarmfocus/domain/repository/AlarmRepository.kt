package com.omondit.alarmfocus.domain.repository

import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.domain.model.AlarmTime
import com.omondit.alarmfocus.domain.model.RepeatSchedule
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    // Basic CRUD operations
    fun getAllAlarms(): Flow<List<AlarmEntity>>
    fun getEnabledAlarms(): Flow<List<AlarmEntity>>
    suspend fun getAlarmById(id: Long): AlarmEntity?
    suspend fun insertAlarm(alarm: AlarmEntity): Long
    suspend fun updateAlarm(alarm: AlarmEntity)
    suspend fun deleteAlarm(alarm: AlarmEntity)
    suspend fun deleteAlarmById(id: Long)

    // Alarm state management
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)
    suspend fun markAlarmTriggered(id: Long)
    suspend fun markAlarmDismissed(id: Long)
    suspend fun setSkipNextAlarm(id: Long, skip: Boolean)
    suspend fun updateNextScheduledTime(id: Long, nextTime: Long?)

    // Bulk operations
    suspend fun getEnabledAlarmsOnce(): List<AlarmEntity>
    suspend fun getActiveAlarms(): List<AlarmEntity>
    suspend fun getAlarmsReadyToTrigger(currentTime: Long = System.currentTimeMillis()): List<AlarmEntity>
    suspend fun dismissAllAlarms()

    // Analytics and insights
    suspend fun getEnabledAlarmCount(): Int
    suspend fun getRecentlyTriggeredAlarms(since: Long): List<AlarmEntity>
    suspend fun getOverallSuccessRate(): Float?
    suspend fun getMostSuccessfulAlarms(limit: Int): List<AlarmEntity>

    // Helper methods for alarm creation
    suspend fun createQuickAlarm(
        time: AlarmTime,
        label: String = "Quick Alarm",
        schedule: RepeatSchedule = RepeatSchedule.once()
    ): Long

    suspend fun createRepeatingAlarm(
        time: AlarmTime,
        label: String,
        schedule: RepeatSchedule
    ): Long
}
