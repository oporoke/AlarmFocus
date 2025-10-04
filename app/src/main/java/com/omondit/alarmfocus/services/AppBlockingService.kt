package com.omondit.alarmfocus.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.database.entities.BlockedAppEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppBlockingService : AccessibilityService() {

    companion object {
        private const val TAG = "AppBlockingService"
        const val ACTION_UPDATE_BLOCKED_APPS = "com.omondit.alarmfocus.UPDATE_BLOCKED_APPS"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase
    private var blockedApps: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "AppBlockingService connected")

        database = AppDatabase.getDatabase(this)

        // Monitor blocked apps changes
        serviceScope.launch {
            database.blockedAppDao().getActivelyBlocked().collect { apps ->
                blockedApps = apps.map { it.packageName }.toSet()
                Log.d(TAG, "Updated blocked apps: ${blockedApps.size} apps")
            }
        }

        // Periodically expire timed blocks
        serviceScope.launch {
            while (true) {
                database.blockedAppDao().expireTimedBlocks()
                kotlinx.coroutines.delay(60_000) // Check every minute
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Check if app is blocked
        if (blockedApps.contains(packageName)) {
            Log.d(TAG, "Blocked app detected: $packageName")
            showBlockingOverlay(packageName)
            returnToHome()
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_BLOCKED_APPS -> {
                serviceScope.launch {
                    val apps = database.blockedAppDao().getActivelyBlocked().first()
                    blockedApps = apps.map { it.packageName }.toSet()
                }
            }
        }
        return START_STICKY
    }

    private fun showBlockingOverlay(packageName: String) {
        serviceScope.launch {
            val blockedApp = database.blockedAppDao().getByPackageName(packageName)

            val message = when (blockedApp?.blockType) {
                BlockedAppEntity.BlockType.POST_ALARM -> {
                    val remainingTime = getRemainingBlockTime(blockedApp.blockUntil)
                    "This app is blocked for $remainingTime to help you wake up properly."
                }
                BlockedAppEntity.BlockType.FOCUS_SESSION -> {
                    val remainingTime = getRemainingBlockTime(blockedApp.blockUntil)
                    "You're in Focus Mode. This app will be available in $remainingTime."
                }
                else -> {
                    "This app is currently blocked. You can unblock it in Settings."
                }
            }

            // Show overlay dialog
            val overlayIntent = Intent(this@AppBlockingService, BlockOverlayActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("app_name", blockedApp?.appName ?: packageName)
                putExtra("message", message)
                putExtra("block_type", blockedApp?.blockType?.name)
            }

            startActivity(overlayIntent)
        }
    }

    private fun returnToHome() {
        // Navigate back to home screen
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun getRemainingBlockTime(blockUntil: Long?): String {
        if (blockUntil == null) return "indefinitely"

        val remainingMillis = blockUntil - System.currentTimeMillis()
        if (remainingMillis <= 0) return "0 minutes"

        val minutes = remainingMillis / (60 * 1000)
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when {
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ${remainingMinutes}min"
            else -> "$minutes minute${if (minutes > 1) "s" else ""}"
        }
    }
}
