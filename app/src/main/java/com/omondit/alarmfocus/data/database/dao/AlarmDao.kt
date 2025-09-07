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

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    suspend fun getAllAlarmsOnce(): List<AlarmEntity>

    @Insert
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("UPDATE alarms SET lastTriggered = :timestamp, isActive = 1 WHERE id = :id")
    suspend fun markAlarmTriggered(id: Long, timestamp: Long)

    @Query("UPDATE alarms SET isActive = 0 WHERE id = :id")
    suspend fun markAlarmDismissed(id: Long)

    @Query("UPDATE alarms SET isActive = 0")
    suspend fun dismissAllAlarms()
}