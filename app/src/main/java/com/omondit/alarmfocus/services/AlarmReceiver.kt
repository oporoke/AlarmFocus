package com.omondit.alarmfocus.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * BroadcastReceiver for handling alarm triggers from AlarmManager
 * Ensures reliable alarm delivery even when app is killed
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val EXTRA_VIBRATION_ENABLED = "vibration_enabled"
        const val EXTRA_VOLUME = "volume"
    }

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        val soundUri = intent.getStringExtra(EXTRA_SOUND_URI)
        val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "Alarm"
        val vibrationEnabled = intent.getBooleanExtra(EXTRA_VIBRATION_ENABLED, true)
        val volume = intent.getFloatExtra(EXTRA_VOLUME, 1.0f)

        Log.i(TAG, "Alarm triggered: ID=$alarmId, Label=$label")

        if (alarmId == -1L) {
            Log.e(TAG, "Invalid alarm ID received")
            return
        }

        try {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = AlarmService.ACTION_START_ALARM
                putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmService.EXTRA_SOUND_URI, soundUri)
                putExtra(EXTRA_ALARM_LABEL, label)
                putExtra(EXTRA_VIBRATION_ENABLED, vibrationEnabled)
                putExtra(EXTRA_VOLUME, volume)
            }

            // Start foreground service for API 26+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            Log.d(TAG, "AlarmService started successfully for alarm $alarmId")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AlarmService for alarm $alarmId", e)

            // TODO: Implement fallback notification or system alarm
            // This is critical for ADHD users who depend on the alarm
        }
    }
}
