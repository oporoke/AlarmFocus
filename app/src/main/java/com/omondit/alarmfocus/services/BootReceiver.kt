package com.omondit.alarmfocus.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
        // This will be implemented in Deliverable 3 & 5
        // For now, just a placeholder that logs the event
        android.util.Log.i("BootReceiver", "Device booted - alarms will be restored")
    }
}
