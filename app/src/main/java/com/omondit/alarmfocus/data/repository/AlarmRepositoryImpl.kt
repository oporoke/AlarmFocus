package com.omondit.alarmfocus.data.repository

import com.omondit.alarmfocus.data.database.dao.AlarmDao
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.domain.model.AlarmTime
import com.omondit.alarmfocus.domain.model.RepeatSchedule
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    override fun getEnabledAlarms(): Flow<List<AlarmEntity>> = alarmDao.getEnabledAlarms()

    override suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)

    override suspend fun insertAlarm(alarm: AlarmEntity): Long = alarmDao.insertAlarm(alarm)

    override suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.updateAlarm(alarm)

    override suspend fun deleteAlarm(alarm: AlarmEntity) = alarmDao.deleteAlarm(alarm)

    override suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)

    override suspend fun setAlarmEnabled(id: Long, enabled: Boolean) =
        alarmDao.setAlarmEnabled(id, enabled)

    override suspend fun markAlarmTriggered(id: Long) =
        alarmDao.markAlarmTriggered(id, System.currentTimeMillis())

    override suspend fun markAlarmDismissed(id: Long) = alarmDao.markAlarmDismissed(id)

    override suspend fun setSkipNextAlarm(id: Long, skip: Boolean) =
        alarmDao.setSkipNextAlarm(id, skip)

    override suspend fun updateNextScheduledTime(id: Long, nextTime: Long?) =
        alarmDao.updateNextScheduledTime(id, nextTime)

    override suspend fun getEnabledAlarmsOnce(): List<AlarmEntity> = alarmDao.getEnabledAlarmsOnce()

    override suspend fun getActiveAlarms(): List<AlarmEntity> = alarmDao.getActiveAlarms()

    override suspend fun getAlarmsReadyToTrigger(currentTime: Long): List<AlarmEntity> =
        alarmDao.getAlarmsReadyToTrigger(currentTime)

    override suspend fun dismissAllAlarms() = alarmDao.dismissAllAlarms()

    override suspend fun getEnabledAlarmCount(): Int = alarmDao.getEnabledAlarmCount()

    override suspend fun getRecentlyTriggeredAlarms(since: Long): List<AlarmEntity> =
        alarmDao.getRecentlyTriggeredAlarms(since)

    override suspend fun getOverallSuccessRate(): Float? = alarmDao.getOverallSuccessRate()

    override suspend fun getMostSuccessfulAlarms(limit: Int): List<AlarmEntity> =
        alarmDao.getMostSuccessfulAlarms(limit)

    override suspend fun createQuickAlarm(
        time: AlarmTime,
        label: String,
        schedule: RepeatSchedule
    ): Long {
        val alarm = AlarmEntity(
            hour = time.hour,
            minute = time.minute,
            label = label,
            repeatDays = schedule.toJson(),
            isEnabled = true
        )
        return insertAlarm(alarm)
    }

    override suspend fun createRepeatingAlarm(
        time: AlarmTime,
        label: String,
        schedule: RepeatSchedule
    ): Long {
        val alarm = AlarmEntity(
            hour = time.hour,
            minute = time.minute,
            label = label,
            repeatDays = schedule.toJson(),
            isEnabled = true
        )
        return insertAlarm(alarm)
    }
}
