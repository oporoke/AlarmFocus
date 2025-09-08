package com.omondit.alarmfocus.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour, minute")
    fun getEnabledAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarmsOnce(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE isActive = 1")
    suspend fun getActiveAlarms(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE nextScheduledTime <= :currentTime AND isEnabled = 1")
    suspend fun getAlarmsReadyToTrigger(currentTime: Long): List<AlarmEntity>

    @Insert
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE alarms SET lastTriggered = :timestamp, isActive = 1, snoozeCount = snoozeCount + 1 WHERE id = :id")
    suspend fun markAlarmTriggered(id: Long, timestamp: Long)

    @Query("UPDATE alarms SET isActive = 0, successfulWakeups = successfulWakeups + 1 WHERE id = :id")
    suspend fun markAlarmDismissed(id: Long)

    @Query("UPDATE alarms SET isActive = 0")
    suspend fun dismissAllAlarms()

    @Query("UPDATE alarms SET nextScheduledTime = :nextTime WHERE id = :id")
    suspend fun updateNextScheduledTime(id: Long, nextTime: Long?)

    @Query("UPDATE alarms SET skipNextAlarm = :skip WHERE id = :id")
    suspend fun setSkipNextAlarm(id: Long, skip: Boolean)

    @Query("SELECT COUNT(*) FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarmCount(): Int

    @Query("SELECT * FROM alarms WHERE lastTriggered > :since")
    suspend fun getRecentlyTriggeredAlarms(since: Long): List<AlarmEntity>

    // Analytics queries
    @Query("SELECT AVG(successfulWakeups * 1.0 / (snoozeCount + successfulWakeups)) FROM alarms WHERE snoozeCount + successfulWakeups > 0")
    suspend fun getOverallSuccessRate(): Float?

    @Query("SELECT * FROM alarms ORDER BY successfulWakeups DESC LIMIT :limit")
    suspend fun getMostSuccessfulAlarms(limit: Int): List<AlarmEntity>
}
