package com.omondit.alarmfocus.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.database.entities.BlockedAppEntity
import com.omondit.alarmfocus.services.AppBlockingService
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class AppBlockManager(private val context: Context) {

    companion object {
        private const val TAG = "AppBlockManager"
        private const val POST_ALARM_BLOCK_DURATION_MS = 60 * 60 * 1000L // 1 hour
        private const val PREFS_NAME = "app_blocking"
        private const val KEY_POST_ALARM_ENABLED = "post_alarm_blocking_enabled"
    }

    private val database = AppDatabase.getDatabase(context)
    private val blockedAppDao = database.blockedAppDao()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Enable post-alarm blocking for social media apps
     */
    suspend fun enablePostAlarmBlocking() {
        val blockUntil = System.currentTimeMillis() + POST_ALARM_BLOCK_DURATION_MS

        // Block default social media apps
        BlockedAppEntity.DEFAULT_SOCIAL_MEDIA_APPS.forEach { packageName ->
            if (isAppInstalled(packageName)) {
                val appName = getAppName(packageName)
                val blockedApp = BlockedAppEntity(
                    packageName = packageName,
                    appName = appName,
                    isBlocked = true,
                    blockType = BlockedAppEntity.BlockType.POST_ALARM,
                    blockUntil = blockUntil
                )
                blockedAppDao.insert(blockedApp)
            }
        }

        // Notify blocking service
        notifyBlockingService()

        prefs.edit().putBoolean(KEY_POST_ALARM_ENABLED, true).apply()

        Log.d(TAG, "Post-alarm blocking enabled for 1 hour")
    }

    /**
     * Disable post-alarm blocking
     */
    suspend fun disablePostAlarmBlocking() {
        blockedAppDao.deleteByType(BlockedAppEntity.BlockType.POST_ALARM)
        prefs.edit().putBoolean(KEY_POST_ALARM_ENABLED, false).apply()
        notifyBlockingService()
        Log.d(TAG, "Post-alarm blocking disabled")
    }

    /**
     * Check if post-alarm blocking is enabled
     */
    fun isPostAlarmBlockingEnabled(): Boolean {
        return prefs.getBoolean(KEY_POST_ALARM_ENABLED, false)
    }

    /**
     * Block a specific app manually
     */
    suspend fun blockApp(packageName: String, duration: Long? = null) {
        val appName = getAppName(packageName)
        val blockUntil = duration?.let { System.currentTimeMillis() + it }

        val blockedApp = BlockedAppEntity(
            packageName = packageName,
            appName = appName,
            isBlocked = true,
            blockType = BlockedAppEntity.BlockType.MANUAL,
            blockUntil = blockUntil
        )

        blockedAppDao.insert(blockedApp)
        notifyBlockingService()

        Log.d(TAG, "Manually blocked app: $appName")
    }

    /**
     * Unblock a specific app
     */
    suspend fun unblockApp(packageName: String) {
        blockedAppDao.setBlockStatus(packageName, false)
        notifyBlockingService()
        Log.d(TAG, "Unblocked app: $packageName")
    }

    /**
     * Get all blocked apps
     */
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>> {
        return blockedAppDao.getAllBlocked()
    }

    /**
     * Get actively blocked apps (not expired)
     */
    fun getActivelyBlockedApps(): Flow<List<BlockedAppEntity>> {
        return blockedAppDao.getActivelyBlocked()
    }

    /**
     * Block apps for a focus session
     */
    suspend fun startFocusSessionBlocking(appPackages: List<String>, durationMinutes: Int) {
        val blockUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(durationMinutes.toLong())

        appPackages.forEach { packageName ->
            if (isAppInstalled(packageName)) {
                val appName = getAppName(packageName)
                val blockedApp = BlockedAppEntity(
                    packageName = packageName,
                    appName = appName,
                    isBlocked = true,
                    blockType = BlockedAppEntity.BlockType.FOCUS_SESSION,
                    blockUntil = blockUntil
                )
                blockedAppDao.insert(blockedApp)
            }
        }

        notifyBlockingService()
        Log.d(TAG, "Focus session blocking started for $durationMinutes minutes")
    }

    /**
     * Stop focus session blocking
     */
    suspend fun stopFocusSessionBlocking() {
        blockedAppDao.deleteByType(BlockedAppEntity.BlockType.FOCUS_SESSION)
        notifyBlockingService()
        Log.d(TAG, "Focus session blocking stopped")
    }

    /**
     * Get installed apps that are social media
     */
    fun getInstalledSocialMediaApps(): List<Pair<String, String>> {
        val packageManager = context.packageManager
        return BlockedAppEntity.DEFAULT_SOCIAL_MEDIA_APPS.mapNotNull { packageName ->
            if (isAppInstalled(packageName)) {
                packageName to getAppName(packageName)
            } else null
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun notifyBlockingService() {
        val intent = Intent(context, AppBlockingService::class.java).apply {
            action = AppBlockingService.ACTION_UPDATE_BLOCKED_APPS
        }
        context.startService(intent)
    }
}
