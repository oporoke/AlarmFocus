package com.omondit.alarmfocus.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.omondit.alarmfocus.domain.model.AlarmTime
import com.omondit.alarmfocus.domain.model.RepeatSchedule
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.utils.AlarmScheduler
import com.omondit.alarmfocus.utils.AlarmValidator

class CreateAlarmUseCase(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val validator: AlarmValidator
) {

    suspend operator fun invoke(
        time: AlarmTime,
        label: String,
        schedule: RepeatSchedule = RepeatSchedule.once(),
        soundUri: String? = null,
        vibrationEnabled: Boolean = true,
        volume: Float = 1.0f
    ): Result<Long> {
        return try {
            // Validate input
            val timeValidation = validator.validateAlarmTime(time.hour, time.minute)
            if (!timeValidation.isValid) {
                return Result.failure(Exception("Invalid time: ${timeValidation.errors.first()}"))
            }

            val labelValidation = validator.validateAlarmLabel(label)
            if (!labelValidation.isValid) {
                return Result.failure(Exception("Invalid label: ${labelValidation.errors.first()}"))
            }

            // Check system state
            val systemValidation = validator.validateSystemState()
            if (!systemValidation.isValid) {
                return Result.failure(Exception("System not ready: ${systemValidation.errors.first()}"))
            }

            // Create alarm
            val alarmId = repository.createRepeatingAlarm(time, label, schedule)

            // Get created alarm to schedule it
            val alarm = repository.getAlarmById(alarmId)
                ?: return Result.failure(Exception("Failed to retrieve created alarm"))

            // Schedule with system
            val scheduleResult = scheduler.scheduleAlarm(alarm)
            when (scheduleResult) {
                is AlarmScheduler.ScheduleResult.Success -> {
                    // Update next scheduled time
                    repository.updateNextScheduledTime(alarmId, scheduleResult.nextTriggerTime)
                    Result.success(alarmId)
                }
                is AlarmScheduler.ScheduleResult.Error -> {
                    // Clean up - delete the alarm since scheduling failed
                    repository.deleteAlarmById(alarmId)
                    Result.failure(Exception("Failed to schedule: ${scheduleResult.message}"))
                }
                else -> {
                    Result.success(alarmId) // Disabled or skipped alarms are still "successful"
                }
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
