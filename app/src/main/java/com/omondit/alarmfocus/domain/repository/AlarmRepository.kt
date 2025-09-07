package com.omondit.alarmfocus.domain.repository

import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<AlarmEntity>>
    suspend fun getAllAlarmsOnce(): List<AlarmEntity>
    suspend fun getAlarmById(id: Long): AlarmEntity?
    suspend fun insertAlarm(alarm: AlarmEntity): Long
    suspend fun updateAlarm(alarm: AlarmEntity)
    suspend fun deleteAlarm(alarm: AlarmEntity)
    suspend fun getEnabledAlarms(): List<AlarmEntity>
    suspend fun markAlarmTriggered(id: Long)
    suspend fun markAlarmDismissed(id: Long)
}
