package com.omondit.alarmfocus.domain.usecase

import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.utils.AlarmScheduler

class DeleteAlarmUseCase(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) {

    suspend operator fun invoke(alarmId: Long): Result<Unit> {
        return try {
            val alarm = repository.getAlarmById(alarmId)
                ?: return Result.failure(Exception("Alarm not found"))

            // Cancel system alarm first
            scheduler.cancelAlarm(alarmId)

            // Delete from database
            repository.deleteAlarmById(alarmId)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
