package com.omondit.alarmfocus.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.domain.model.AlarmTime
import com.omondit.alarmfocus.domain.model.RepeatSchedule
import com.omondit.alarmfocus.services.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class AlarmScheduler(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    companion object {
        private const val TAG = "AlarmScheduler"
        private const val REQUEST_CODE_BASE = 10000
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule a single alarm with the Android system
     */
    fun scheduleAlarm(alarm: AlarmEntity): ScheduleResult {
        return try {
            if (!alarm.isEnabled) {
                return ScheduleResult.Disabled("Alarm is disabled")
            }

            if (alarm.skipNextAlarm) {
                return ScheduleResult.Skipped("Alarm is set to skip next occurrence")
            }

            val alarmTime = alarm.toAlarmTime()
            if (!alarmTime.isValid()) {
                return ScheduleResult.Error("Invalid alarm time: ${alarmTime.hour}:${alarmTime.minute}")
            }

            val repeatSchedule = alarm.getRepeatSchedule()
            val nextTriggerTime = calculateNextTriggerTime(alarmTime, repeatSchedule)
                ?: return ScheduleResult.Error("Unable to calculate next trigger time")

            val intent = createAlarmIntent(alarm)
            val pendingIntent = createPendingIntent(alarm.id, intent)

            // Schedule with system AlarmManager
            scheduleWithAlarmManager(nextTriggerTime, pendingIntent)

            // Update next scheduled time in database
            coroutineScope.launch {
                // This would require repository injection - simplified for now
                Log.d(TAG, "Next scheduled time for alarm ${alarm.id}: ${Date(nextTriggerTime)}")
            }

            ScheduleResult.Success(nextTriggerTime)

        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception scheduling alarm ${alarm.id}", e)
            ScheduleResult.Error("Permission denied. Please grant exact alarm permission.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm ${alarm.id}", e)
            ScheduleResult.Error("Failed to schedule alarm: ${e.message}")
        }
    }

    /**
     * Cancel an alarm
     */
    fun cancelAlarm(alarmId: Long): Boolean {
        return try {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(alarmId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            Log.d(TAG, "Cancelled alarm $alarmId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel alarm $alarmId", e)
            false
        }
    }

    /**
     * Reschedule multiple alarms efficiently
     */
    fun rescheduleAlarms(alarms: List<AlarmEntity>): RescheduleResult {
        val results = mutableMapOf<Long, ScheduleResult>()
        var successCount = 0
        var failureCount = 0

        alarms.forEach { alarm ->
            val result = scheduleAlarm(alarm)
            results[alarm.id] = result

            when (result) {
                is ScheduleResult.Success -> successCount++
                is ScheduleResult.Error -> failureCount++
                else -> { /* No-op for disabled/skipped */ }
            }
        }

        Log.i(TAG, "Rescheduled $successCount alarms successfully, $failureCount failed")
        return RescheduleResult(successCount, failureCount, results)
    }

    /**
     * Check if exact alarm permission is available
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun calculateNextTriggerTime(alarmTime: AlarmTime, schedule: RepeatSchedule): Long? {
        val baseTime = alarmTime.toMillisNextOccurrence()
        return schedule.getNextOccurrence(baseTime)
    }

    private fun createAlarmIntent(alarm: AlarmEntity): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_SOUND_URI, alarm.soundUri)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_VIBRATION_ENABLED, alarm.vibrationEnabled)
            putExtra(AlarmReceiver.EXTRA_VOLUME, alarm.volume)
        }
    }

    private fun createPendingIntent(alarmId: Long, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            getRequestCode(alarmId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleWithAlarmManager(triggerTime: Long, pendingIntent: PendingIntent) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            else -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }

    private fun getRequestCode(alarmId: Long): Int {
        return (REQUEST_CODE_BASE + (alarmId % Int.MAX_VALUE)).toInt()
    }

    // Result classes for better error handling
    sealed class ScheduleResult {
        data class Success(val nextTriggerTime: Long) : ScheduleResult()
        data class Error(val message: String) : ScheduleResult()
        data class Disabled(val reason: String) : ScheduleResult()
        data class Skipped(val reason: String) : ScheduleResult()
    }

    data class RescheduleResult(
        val successCount: Int,
        val failureCount: Int,
        val individualResults: Map<Long, ScheduleResult>
    )
}
