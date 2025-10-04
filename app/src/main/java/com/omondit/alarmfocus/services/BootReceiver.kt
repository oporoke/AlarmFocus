package com.omondit.alarmfocus.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.repository.AlarmRepositoryImpl
import com.omondit.alarmfocus.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // Restore alarms after boot/update
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        restoreAlarms(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private suspend fun restoreAlarms(context: Context) {
        try {
            val database = AppDatabase.getDatabase(context)
            val repository = AlarmRepositoryImpl(database.alarmDao())
            val scheduler = AlarmScheduler(context)

            // Get all enabled alarms
            val enabledAlarms = repository.getEnabledAlarmsOnce()

            Log.i("BootReceiver", "Restoring ${enabledAlarms.size} alarms after boot")

            // Reschedule each alarm
            enabledAlarms.forEach { alarm ->
                val result = scheduler.scheduleAlarm(alarm)
                when (result) {
                    is AlarmScheduler.ScheduleResult.Success -> {
                        repository.updateNextScheduledTime(alarm.id, result.nextTriggerTime)
                        Log.d("BootReceiver", "Restored alarm ${alarm.id}")
                    }
                    is AlarmScheduler.ScheduleResult.Error -> {
                        Log.e("BootReceiver", "Failed to restore alarm ${alarm.id}: ${result.message}")
                    }
                    else -> {}
                }
            }

            // ✅ CHECK IF ALARM WAS RINGING BEFORE REBOOT
            val prefs = context.getSharedPreferences("alarm_service_state", Context.MODE_PRIVATE)
            val activeAlarmId = prefs.getLong("active_alarm_id", -1L)

            if (activeAlarmId != -1L) {
                Log.i("BootReceiver", "Active alarm detected: $activeAlarmId - Restarting")

                val soundUri = prefs.getString("active_alarm_sound", null)

                // Restart the alarm service
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_START_ALARM
                    putExtra(AlarmService.EXTRA_ALARM_ID, activeAlarmId)
                    putExtra(AlarmService.EXTRA_SOUND_URI, soundUri)
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                Log.i("BootReceiver", "Restarted active alarm $activeAlarmId")
            }

            Log.i("BootReceiver", "Alarm restoration complete")
        } catch (e: Exception) {
            Log.e("BootReceiver", "Error restoring alarms", e)
        }
    }
}
