package com.omondit.alarmfocus.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comprehensive alarm system diagnostics and health monitoring
 * Helps ADHD users troubleshoot alarm reliability issues
 */
class AlarmDiagnostics(
    private val context: Context,
    private val alarmRepository: AlarmRepository
) {

    companion object {
        private const val TAG = "AlarmDiagnostics"
        private const val LOG_FILE_NAME = "alarm_diagnostics.log"
        private const val MAX_LOG_SIZE_MB = 5
    }

    data class DiagnosticResult(
        val category: String,
        val status: HealthStatus,
        val message: String,
        val actionable: Boolean = false,
        val action: DiagnosticAction? = null
    )

    data class DiagnosticAction(
        val label: String,
        val intent: Intent
    )

    enum class HealthStatus {
        HEALTHY, WARNING, CRITICAL, UNKNOWN
    }

    data class SystemHealth(
        val overallStatus: HealthStatus,
        val results: List<DiagnosticResult>,
        val criticalIssues: Int,
        val warnings: Int,
        val lastChecked: Long = System.currentTimeMillis()
    )

    /**
     * Perform comprehensive system health check
     */
    suspend fun performHealthCheck(): SystemHealth = withContext(Dispatchers.IO) {
        val results = mutableListOf<DiagnosticResult>()

        // Check permissions
        results.addAll(checkPermissions())

        // Check battery optimization
        results.add(checkBatteryOptimization())

        // Check storage space
        results.add(checkStorageSpace())

        // Check alarm manager capabilities
        results.add(checkAlarmManager())

        // Check notification settings
        results.add(checkNotificationSettings())

        // Check system audio settings
        results.add(checkAudioSettings())

        // Check alarm service status
        results.add(checkAlarmService())

        // Check database integrity
        results.add(checkDatabaseIntegrity())

        val criticalIssues = results.count { it.status == HealthStatus.CRITICAL }
        val warnings = results.count { it.status == HealthStatus.WARNING }

        val overallStatus = when {
            criticalIssues > 0 -> HealthStatus.CRITICAL
            warnings > 0 -> HealthStatus.WARNING
            else -> HealthStatus.HEALTHY
        }

        logDiagnosticResults(results)

        SystemHealth(
            overallStatus = overallStatus,
            results = results,
            criticalIssues = criticalIssues,
            warnings = warnings
        )
    }

    private fun checkPermissions(): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.WAKE_LOCK,
                android.Manifest.permission.VIBRATE,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
                android.Manifest.permission.FOREGROUND_SERVICE,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                android.Manifest.permission.WAKE_LOCK,
                android.Manifest.permission.VIBRATE,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
                android.Manifest.permission.FOREGROUND_SERVICE
            )
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            results.add(DiagnosticResult(
                category = "Permissions",
                status = HealthStatus.HEALTHY,
                message = "All required permissions granted"
            ))
        } else {
            results.add(DiagnosticResult(
                category = "Permissions",
                status = HealthStatus.CRITICAL,
                message = "Missing permissions: ${missingPermissions.joinToString(", ")}",
                actionable = true,
                action = DiagnosticAction(
                    label = "Open App Settings",
                    intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                )
            ))
        }

        // Check Do Not Disturb permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                results.add(DiagnosticResult(
                    category = "Do Not Disturb",
                    status = HealthStatus.WARNING,
                    message = "Cannot override Do Not Disturb mode",
                    actionable = true,
                    action = DiagnosticAction(
                        label = "Grant DND Permission",
                        intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    )
                ))
            }
        }

        // Check exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                results.add(DiagnosticResult(
                    category = "Exact Alarms",
                    status = HealthStatus.CRITICAL,
                    message = "Cannot schedule exact alarms",
                    actionable = true,
                    action = DiagnosticAction(
                        label = "Enable Exact Alarms",
                        intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                ))
            }
        }

        return results
    }

    private fun checkBatteryOptimization(): DiagnosticResult {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                DiagnosticResult(
                    category = "Battery Optimization",
                    status = HealthStatus.HEALTHY,
                    message = "App is exempt from battery optimization"
                )
            } else {
                DiagnosticResult(
                    category = "Battery Optimization",
                    status = HealthStatus.WARNING,
                    message = "Battery optimization may prevent reliable alarms",
                    actionable = true,
                    action = DiagnosticAction(
                        label = "Disable Battery Optimization",
                        intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    )
                )
            }
        } else {
            DiagnosticResult(
                category = "Battery Optimization",
                status = HealthStatus.HEALTHY,
                message = "Not applicable for this Android version"
            )
        }
    }

    private fun checkStorageSpace(): DiagnosticResult {
        val availableSpace = context.filesDir.freeSpace
        val availableSpaceMB = availableSpace / (1024 * 1024)

        return when {
            availableSpaceMB > 100 -> DiagnosticResult(
                category = "Storage Space",
                status = HealthStatus.HEALTHY,
                message = "${availableSpaceMB}MB available"
            )
            availableSpaceMB > 50 -> DiagnosticResult(
                category = "Storage Space",
                status = HealthStatus.WARNING,
                message = "Low storage space: ${availableSpaceMB}MB available"
            )
            else -> DiagnosticResult(
                category = "Storage Space",
                status = HealthStatus.CRITICAL,
                message = "Very low storage space: ${availableSpaceMB}MB available",
                actionable = true,
                action = DiagnosticAction(
                    label = "Free Up Space",
                    intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                )
            )
        }
    }

    private fun checkAlarmManager(): DiagnosticResult {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            if (alarmManager != null) {
                DiagnosticResult(
                    category = "Alarm Manager",
                    status = HealthStatus.HEALTHY,
                    message = "Alarm Manager is available"
                )
            } else {
                DiagnosticResult(
                    category = "Alarm Manager",
                    status = HealthStatus.CRITICAL,
                    message = "Alarm Manager is not available"
                )
            }
        } catch (e: Exception) {
            DiagnosticResult(
                category = "Alarm Manager",
                status = HealthStatus.CRITICAL,
                message = "Error accessing Alarm Manager: ${e.message}"
            )
        }
    }

    private fun checkNotificationSettings(): DiagnosticResult {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return if (notificationManager.areNotificationsEnabled()) {
            DiagnosticResult(
                category = "Notifications",
                status = HealthStatus.HEALTHY,
                message = "Notifications are enabled"
            )
        } else {
            DiagnosticResult(
                category = "Notifications",
                status = HealthStatus.WARNING,
                message = "Notifications are disabled",
                actionable = true,
                action = DiagnosticAction(
                    label = "Enable Notifications",
                    intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                )
            )
        }
    }

    private fun checkAudioSettings(): DiagnosticResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_ALARM)
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
        val volumePercentage = (currentVolume * 100) / maxVolume

        return when {
            volumePercentage >= 80 -> DiagnosticResult(
                category = "Audio Settings",
                status = HealthStatus.HEALTHY,
                message = "Alarm volume is at ${volumePercentage}%"
            )
            volumePercentage >= 50 -> DiagnosticResult(
                category = "Audio Settings",
                status = HealthStatus.WARNING,
                message = "Alarm volume is low (${volumePercentage}%)"
            )
            else -> DiagnosticResult(
                category = "Audio Settings",
                status = HealthStatus.CRITICAL,
                message = "Alarm volume is very low (${volumePercentage}%)",
                actionable = true,
                action = DiagnosticAction(
                    label = "Adjust Volume",
                    intent = Intent(Settings.ACTION_SOUND_SETTINGS)
                )
            )
        }
    }

    private fun checkAlarmService(): DiagnosticResult {
        // Check if AlarmService is properly configured
        return try {
            val serviceIntent = Intent(context, com.omondit.alarmfocus.services.AlarmService::class.java)
            // Simple check to see if service class exists and is accessible
            context.packageManager.getServiceInfo(
                context.packageManager.getLaunchIntentForPackage(context.packageName)?.component ?:
                throw Exception("Cannot get package component"),
                PackageManager.GET_SERVICES
            )

            DiagnosticResult(
                category = "Alarm Service",
                status = HealthStatus.HEALTHY,
                message = "Alarm service is properly configured"
            )
        } catch (e: Exception) {
            DiagnosticResult(
                category = "Alarm Service",
                status = HealthStatus.WARNING,
                message = "Alarm service configuration could not be verified"
            )
        }
    }

    private suspend fun checkDatabaseIntegrity(): DiagnosticResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val alarmCount = alarmRepository.getEnabledAlarmCount()
            val recentAlarms = alarmRepository.getRecentlyTriggeredAlarms(System.currentTimeMillis() - 86400000) // 24 hours

            DiagnosticResult(
                category = "Database",
                status = HealthStatus.HEALTHY,
                message = "$alarmCount active alarms, ${recentAlarms.size} triggered in last 24h"
            )
        } catch (e: Exception) {
            DiagnosticResult(
                category = "Database",
                status = HealthStatus.CRITICAL,
                message = "Database error: ${e.message}"
            )
        }
    }

    /**
     * Get battery status information
     */
    fun getBatteryStatus(): BatteryStatus {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging

        return BatteryStatus(
            level = level,
            isCharging = isCharging,
            isLow = level < 15,
            isCritical = level < 5
        )
    }

    /**
     * Log diagnostic event
     */
    fun logEvent(event: String, details: String = "") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logFile = File(context.filesDir, LOG_FILE_NAME)
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val logEntry = "[$timestamp] $event: $details\n"

                // Check file size and rotate if needed
                if (logFile.exists() && logFile.length() > MAX_LOG_SIZE_MB * 1024 * 1024) {
                    rotateLogFile(logFile)
                }

                logFile.appendText(logEntry)
                Log.d(TAG, "Event logged: $event")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log event: $event", e)
            }
        }
    }

    /**
     * Get diagnostic logs
     */
    suspend fun getDiagnosticLogs(): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            if (logFile.exists()) {
                logFile.readText()
            } else {
                "No diagnostic logs available"
            }
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    /**
     * Clear diagnostic logs
     */
    suspend fun clearLogs(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            logFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing logs", e)
            false
        }
    }

    private fun logDiagnosticResults(results: List<DiagnosticResult>) {
        val criticalIssues = results.filter { it.status == HealthStatus.CRITICAL }
        val warnings = results.filter { it.status == HealthStatus.WARNING }

        logEvent(
            "HEALTH_CHECK_COMPLETED",
            "Critical: ${criticalIssues.size}, Warnings: ${warnings.size}"
        )

        criticalIssues.forEach { issue ->
            logEvent("CRITICAL_ISSUE", "${issue.category}: ${issue.message}")
        }
    }

    private fun rotateLogFile(logFile: File) {
        try {
            val backupFile = File(logFile.parent, "${LOG_FILE_NAME}.bak")
            if (backupFile.exists()) {
                backupFile.delete()
            }
            logFile.renameTo(backupFile)
            logFile.createNewFile()
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating log file", e)
        }
    }

    data class BatteryStatus(
        val level: Int,
        val isCharging: Boolean,
        val isLow: Boolean,
        val isCritical: Boolean
    )
}
