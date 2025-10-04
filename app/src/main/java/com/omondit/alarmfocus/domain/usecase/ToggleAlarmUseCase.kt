package com.omondit.alarmfocus.domain.usecase

import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.utils.AlarmScheduler

class ToggleAlarmUseCase(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) {

    suspend operator fun invoke(alarmId: Long): Result<Boolean> {
        return try {
            val alarm = repository.getAlarmById(alarmId)
                ?: return Result.failure(Exception("Alarm not found"))

            // Block disabling if alarm is currently active/ringing
            if (alarm.isActive) {
                return Result.failure(Exception("Cannot disable active alarm. Complete the mission first."))
            }
            val newEnabledState = !alarm.isEnabled

            // Update database
            repository.setAlarmEnabled(alarmId, newEnabledState)

            if (newEnabledState) {
                // Enable: Schedule the alarm
                val updatedAlarm = repository.getAlarmById(alarmId)!!
                val scheduleResult = scheduler.scheduleAlarm(updatedAlarm)

                when (scheduleResult) {
                    is AlarmScheduler.ScheduleResult.Success -> {
                        repository.updateNextScheduledTime(alarmId, scheduleResult.nextTriggerTime)
                        Result.success(true)
                    }
                    is AlarmScheduler.ScheduleResult.Error -> {
                        // Rollback - disable the alarm again
                        repository.setAlarmEnabled(alarmId, false)
                        Result.failure(Exception("Failed to schedule: ${scheduleResult.message}"))
                    }
                    else -> Result.success(true)
                }
            } else {
                // Disable: Cancel the alarm
                scheduler.cancelAlarm(alarmId)
                repository.updateNextScheduledTime(alarmId, null)
                Result.success(false)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
