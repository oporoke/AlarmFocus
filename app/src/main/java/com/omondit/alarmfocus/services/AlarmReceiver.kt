package com.omondit.alarmfocus.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * BroadcastReceiver for handling alarm triggers from AlarmManager
 * Ensures reliable alarm delivery even when app is killed
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_SOUND_URI = "sound_uri"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        val soundUri = intent.getStringExtra(EXTRA_SOUND_URI)

        if (alarmId != -1L) {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = AlarmService.ACTION_START_ALARM
                putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmService.EXTRA_SOUND_URI, soundUri)
            }

            // Start foreground service for API 26+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}