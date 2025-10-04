package com.omondit.alarmfocus.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.omondit.alarmfocus.services.AlarmDeviceAdminReceiver

class DeviceAdminManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "device_admin"
        private const val KEY_ADMIN_ENABLED = "admin_enabled"
        private const val KEY_UNINSTALL_COOLDOWN = "uninstall_cooldown_until"
        private const val COOLDOWN_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val componentName = ComponentName(context, AlarmDeviceAdminReceiver::class.java)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if device admin is enabled
     */
    fun isDeviceAdminEnabled(): Boolean {
        return devicePolicyManager.isAdminActive(componentName)
    }

    /**
     * Request device admin activation
     */
    fun requestDeviceAdminActivation(): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "This app requires device admin permission to prevent accidental uninstallation " +
                "while alarms are active. This helps ensure your wake-up alarms work reliably."
        )
        return intent
    }

    /**
     * Request to disable device admin (with cooldown period)
     */
    fun requestDeviceAdminDeactivation(): DeactivationResult {
        val cooldownUntil = prefs.getLong(KEY_UNINSTALL_COOLDOWN, 0L)
        val currentTime = System.currentTimeMillis()

        return if (currentTime < cooldownUntil) {
            val remainingHours = ((cooldownUntil - currentTime) / (60 * 60 * 1000)).toInt()
            DeactivationResult.CooldownActive(remainingHours)
        } else {
            // Set cooldown period
            prefs.edit()
                .putLong(KEY_UNINSTALL_COOLDOWN, currentTime + COOLDOWN_DURATION_MS)
                .apply()

            DeactivationResult.CooldownStarted
        }
    }

    /**
     * Force disable device admin (emergency override)
     */
    fun forceDisableDeviceAdmin() {
        if (isDeviceAdminEnabled()) {
            devicePolicyManager.removeActiveAdmin(componentName)
            prefs.edit()
                .putBoolean(KEY_ADMIN_ENABLED, false)
                .remove(KEY_UNINSTALL_COOLDOWN)
                .apply()
        }
    }

    /**
     * Complete deactivation after cooldown
     */
    fun completeDeactivation(): Boolean {
        val cooldownUntil = prefs.getLong(KEY_UNINSTALL_COOLDOWN, 0L)
        val currentTime = System.currentTimeMillis()

        return if (currentTime >= cooldownUntil) {
            forceDisableDeviceAdmin()
            true
        } else {
            false
        }
    }

    /**
     * Get remaining cooldown time in hours
     */
    fun getRemainingCooldownHours(): Int {
        val cooldownUntil = prefs.getLong(KEY_UNINSTALL_COOLDOWN, 0L)
        val currentTime = System.currentTimeMillis()

        if (currentTime >= cooldownUntil) return 0

        return ((cooldownUntil - currentTime) / (60 * 60 * 1000)).toInt()
    }

    sealed class DeactivationResult {
        object CooldownStarted : DeactivationResult()
        data class CooldownActive(val remainingHours: Int) : DeactivationResult()
    }
}
