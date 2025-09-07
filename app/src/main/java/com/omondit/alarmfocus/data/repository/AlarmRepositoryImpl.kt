package com.omondit.alarmfocus.data.repository

import com.omondit.alarmfocus.data.database.dao.AlarmDao
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<AlarmEntity>> {
        return alarmDao.getAllAlarms()
    }

    override suspend fun getAllAlarmsOnce(): List<AlarmEntity> {
        return alarmDao.getAllAlarmsOnce()
    }


    override suspend fun getAlarmById(id: Long): AlarmEntity? {
        return alarmDao.getAlarmById(id)
    }

    override suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return alarmDao.insertAlarm(alarm)
    }

    override suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.updateAlarm(alarm)
    }

    override suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmDao.deleteAlarm(alarm)
    }

    override suspend fun getEnabledAlarms(): List<AlarmEntity> {
        return alarmDao.getEnabledAlarms()
    }

    override suspend fun markAlarmTriggered(id: Long) {
        alarmDao.markAlarmTriggered(id, System.currentTimeMillis())
    }

    override suspend fun markAlarmDismissed(id: Long) {
        alarmDao.markAlarmDismissed(id)
    }
}
