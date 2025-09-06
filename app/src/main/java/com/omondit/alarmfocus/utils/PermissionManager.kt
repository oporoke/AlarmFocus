package com.omondit.alarmfocus.utils

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 * Manages all permissions required for ADHD Focus Alarm
 * Handles graceful permission requesting with explanations
 */
class PermissionManager(private val activity: ComponentActivity) {

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.VIBRATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }

    private val dndPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // DND permission result handled in onResume
    }

    private val exactAlarmPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Exact alarm permission result handled in onResume
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestAllPermissions() {
        // Step 1: Request standard permissions
        val missingPermissions = REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions)
        } else {
            // Move to special permissions
            requestSpecialPermissions()
        }
    }

    private fun requestSpecialPermissions() {
        // Request Do Not Disturb override
        if (!hasDndPermission()) {
            requestDndPermission()
        }
        // Request exact alarm permission (Android 12+)
        else if (!hasExactAlarmPermission()) {
            requestExactAlarmPermission()
        }
        // All permissions granted
        else {
            onAllPermissionsGranted()
        }
    }

    private fun hasDndPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    private fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun requestDndPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            dndPermissionLauncher.launch(intent)
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            exactAlarmPermissionLauncher.launch(intent)
        }
    }

    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val deniedPermissions = permissions.filter { !it.value }.keys

        if (deniedPermissions.isEmpty()) {
            // All standard permissions granted, move to special permissions
            requestSpecialPermissions()
        } else {
            // Handle denied permissions with explanations
            showPermissionExplanation(deniedPermissions.toList())
        }
    }

    private fun showPermissionExplanation(deniedPermissions: List<String>) {
        // This would show a dialog explaining why each permission is needed
        // For ADHD users, explanations should be clear and benefit-focused

        val explanationMap = mapOf(
            Manifest.permission.WAKE_LOCK to "Keeps alarm active even when screen is off",
            Manifest.permission.VIBRATE to "Provides vibration alerts for heavy sleepers",
            Manifest.permission.MODIFY_AUDIO_SETTINGS to "Ensures alarm plays at maximum volume",
            Manifest.permission.POST_NOTIFICATIONS to "Shows alarm notifications and status",
            Manifest.permission.RECEIVE_BOOT_COMPLETED to "Restores alarms after phone restart"
        )

        // Implementation would show user-friendly dialog with retry option
    }

    private fun onAllPermissionsGranted() {
        // All permissions ready - proceed to main app functionality
        // This callback would be handled by the main activity
    }

    /**
     * Check if all critical permissions are granted
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun areAllPermissionsGranted(): Boolean {
        val standardPermissions = REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }

        return standardPermissions && hasDndPermission() && hasExactAlarmPermission()
    }

    /**
     * Get list of missing permissions with user-friendly descriptions
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getMissingPermissions(): List<Pair<String, String>> {
        val missing = mutableListOf<Pair<String, String>>()

        if (!hasDndPermission()) {
            missing.add("Do Not Disturb Override" to "Allows alarm to sound even in silent mode")
        }

        if (!hasExactAlarmPermission()) {
            missing.add("Exact Alarm Scheduling" to "Ensures alarms trigger at precise times")
        }

        REQUIRED_PERMISSIONS.forEach { permission ->
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                val description = when (permission) {
                    Manifest.permission.WAKE_LOCK -> "Keeps alarm active when screen is off"
                    Manifest.permission.VIBRATE -> "Provides vibration alerts"
                    Manifest.permission.MODIFY_AUDIO_SETTINGS -> "Controls alarm volume"
                    Manifest.permission.POST_NOTIFICATIONS -> "Shows alarm notifications"
                    Manifest.permission.RECEIVE_BOOT_COMPLETED -> "Restores alarms after restart"
                    else -> "Required for alarm functionality"
                }
                missing.add(permission to description)
            }
        }

        return missing
    }
}