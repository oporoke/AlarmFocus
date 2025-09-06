package com.omondit.alarmfocus.services

import android.content.Context
import android.media.AudioManager
import android.os.Build

/**
 * Helper class for managing audio settings and permissions
 * Handles DND override and volume management
 */
class AudioManagerHelper(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /**
     * Check if app has permission to override Do Not Disturb
     */
    fun hasDndOverridePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as android.app.NotificationManager
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true // No DND on older versions
        }
    }

    /**
     * Get current alarm volume level (0-100)
     */
    fun getAlarmVolumePercentage(): Int {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        return (currentVolume * 100) / maxVolume
    }

    /**
     * Set alarm volume to maximum
     */
    fun setMaximumAlarmVolume(): Int {
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            maxVolume,
            0 // No flags - silent change
        )

        return originalVolume
    }

    /**
     * Restore alarm volume
     */
    fun restoreAlarmVolume(volume: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            volume,
            0
        )
    }

    /**
     * Check if device is currently in silent or vibrate mode
     */
    fun isDeviceSilenced(): Boolean {
        return audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
    }
}