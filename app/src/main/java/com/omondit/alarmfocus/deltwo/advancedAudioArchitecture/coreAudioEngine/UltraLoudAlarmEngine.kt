package com.omondit.alarmfocus.deltwo.AdvancedAudioArchitecture.CoreAudioEngine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import androidx.room.RoomSQLiteQuery.Companion.acquire

class UltraLoudAlarmEngine {

    private val audioManager = getSystemService(AudioManager::class.java)
    private val mediaPlayer = MediaPlayer()
    private val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

    companion object {
        private const val TARGET_VOLUME_DB = 100 // Maximum safe loudness
        private const val RAMP_UP_DURATION_MS = 10000 // 10 seconds to full volume
        private const val VIBRATION_PATTERN = longArrayOf(0, 1000, 500, 1000, 500, 2000)
        private const val WAKE_LOCK_TIMEOUT = 30 * 60 * 1000L // 30 minutes max
    }

    data class AlarmAudioConfig(
        val soundUri: Uri,
        val startVolumePercent: Int = 70,
        val maxVolumePercent: Int = 100,
        val rampUpDurationMs: Long = RAMP_UP_DURATION_MS,
        val loopCount: Int = -1, // Infinite loop
        val audioStreamType: Int = AudioManager.STREAM_ALARM,
        val audioAttributes: AudioAttributes
    )

    fun startAlarm(config: AlarmAudioConfig): AlarmPlaybackResult {

        try {
            // Step 1: Acquire all necessary system permissions and locks
            acquireSystemResources()

            // Step 2: Override all system audio limitations
            overrideSystemAudioLimitations()

            // Step 3: Configure audio for maximum impact
            configureAudioForMaximumImpact(config)

            // Step 4: Start audio playback with progressive volume
            startProgressiveVolumePlayback(config)

            // Step 5: Begin coordinated vibration patterns
            startCoordinatedVibration()

            // Step 6: Monitor playback and handle edge cases
            startPlaybackMonitoring()

            return AlarmPlaybackResult.SUCCESS

        } catch (e: Exception) {
            return handleAlarmFailure(e, config)
        }
    }

    private fun acquireSystemResources() {
        // Critical: Acquire wake lock to prevent sleep during alarm
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "ADHDAlarm::UltraLoudWakeLock"
        ).apply {
            acquire(WAKE_LOCK_TIMEOUT)
        }

        // Acquire audio focus with highest priority
        val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener({ /* Never give up focus during alarm */ })
            .build()

        audioManager.requestAudioFocus(audioFocusRequest)
    }

    private fun overrideSystemAudioLimitations() {
        // Store original audio states to restore later
        originalRingerMode = audioManager.ringerMode
        originalStreamVolumes = mutableMapOf<Int, Int>().apply {
            AudioStreams.ALL.forEach { stream ->
                put(stream, audioManager.getStreamVolume(stream))
            }
        }

        // Override ringer mode to ensure audio plays
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL

        // Set all relevant audio streams to maximum
        val criticalStreams = listOf(
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.STREAM_RING
        )

        criticalStreams.forEach { streamType ->
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            audioManager.setStreamVolume(streamType, maxVolume, 0)
        }

        // Disable Do Not Disturb if possible (requires special permissions)
        if (hasDoNotDisturbAccess()) {
            overrideDoNotDisturbMode()
        }
    }
}