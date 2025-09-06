package com.omondit.alarmfocus.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Ultra-Loud Alarm Service
 * Handles maximum volume alarm playback with DND override and persistence
 */
class AlarmService : Service() {

    companion object {
        const val ACTION_START_ALARM = "START_ALARM"
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "ADHD_ALARM_CHANNEL"

        private const val VOLUME_RAMP_DURATION = 10_000L // 10 seconds
        private const val VOLUME_RAMP_STEPS = 20
        private const val MAX_VOLUME_PERCENTAGE = 1.0f
        private const val INITIAL_VOLUME_PERCENTAGE = 0.7f
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var volumeRampJob: Job? = null
    private var serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var originalAlarmVolume: Int = 0
    private var originalRingerMode: Int = 0
    private var currentAlarmId: Long = -1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeSystemServices()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
                val soundUri = intent.getStringExtra(EXTRA_SOUND_URI)
                startAlarm(alarmId, soundUri)
            }
            ACTION_STOP_ALARM -> {
                stopAlarm()
            }
        }
        return START_STICKY // Restart if killed
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun initializeSystemServices() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "ADHDAlarm::AlarmWakeLock"
        )
    }

    private fun startAlarm(alarmId: Long, soundUri: String?) {
        currentAlarmId = alarmId

        // Acquire wake lock to keep device awake
        wakeLock?.acquire(30 * 60 * 1000L) // 30 minutes max

        // Save original audio settings
        saveOriginalAudioSettings()

        // Override audio settings for maximum impact
        overrideAudioSettings()

        // Start foreground service with notification
        startForeground(NOTIFICATION_ID, createAlarmNotification(alarmId))

        // Initialize and start media player
        initializeMediaPlayer(soundUri)

        // Start vibration
        startVibration()

        // Start volume ramping
        startVolumeRamping()
    }

    private fun saveOriginalAudioSettings() {
        audioManager?.let { am ->
            originalAlarmVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
            originalRingerMode = am.ringerMode
        }
    }

    private fun overrideAudioSettings() {
        audioManager?.let { am ->
            // Override Do Not Disturb mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Request DND access if needed - this should be handled in setup
                try {
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                } catch (e: SecurityException) {
                    // Graceful degradation if permission not granted
                }
            }

            // Set alarm stream to maximum volume
            val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            am.setStreamVolume(
                AudioManager.STREAM_ALARM,
                maxVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
            )
        }
    }

    private fun initializeMediaPlayer(soundUri: String?) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                // Set data source (default or custom sound)
                if (soundUri != null && soundUri.isNotEmpty()) {
                    setDataSource(this@AlarmService, android.net.Uri.parse(soundUri))
                } else {
                    // Use default ultra-annoying alarm sound
                    val descriptor = assets.openFd("default_alarm_ultra_loud.mp3")
                    setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                    descriptor.close()
                }

                // Configure playback
                isLooping = true
                setVolume(INITIAL_VOLUME_PERCENTAGE, INITIAL_VOLUME_PERCENTAGE)

                setOnPreparedListener { player ->
                    player.start()
                }

                setOnErrorListener { _, what, extra ->
                    // Fallback to system alarm sound
                    try {
                        reset()
                        setDataSource(
                            this@AlarmService,
                            android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                        )
                        prepareAsync()
                    } catch (e: Exception) {
                        // Last resort: use notification sound
                        setDataSource(
                            this@AlarmService,
                            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                        )
                        prepareAsync()
                    }
                    true // Error handled
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            // Fallback to system alarm
            initializeFallbackAlarm()
        }
    }

    private fun initializeFallbackAlarm() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(
                    this@AlarmService,
                    android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                )
                isLooping = true
                setVolume(INITIAL_VOLUME_PERCENTAGE, INITIAL_VOLUME_PERCENTAGE)
                prepareAsync()
                setOnPreparedListener { it.start() }
            }
        } catch (e: Exception) {
            // Critical error - log and attempt notification sound
        }
    }

    private fun startVibration() {
        try {
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Create aggressive vibration pattern for ADHD users
                    val pattern = longArrayOf(0, 500, 200, 500, 200, 1000, 200, 500)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)

                    val vibrationEffect = VibrationEffect.createWaveform(
                        pattern,
                        amplitudes,
                        0 // Repeat from beginning
                    )
                    vib.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    val pattern = longArrayOf(0, 500, 200, 500, 200, 1000, 200, 500)
                    vib.vibrate(pattern, 0)
                }
            }
        } catch (e: Exception) {
            // Vibration not critical - continue without it
        }
    }

    private fun startVolumeRamping() {
        volumeRampJob = serviceScope.launch {
            val stepDelay = VOLUME_RAMP_DURATION / VOLUME_RAMP_STEPS
            val volumeIncrement = (MAX_VOLUME_PERCENTAGE - INITIAL_VOLUME_PERCENTAGE) / VOLUME_RAMP_STEPS

            for (step in 1..VOLUME_RAMP_STEPS) {
                delay(stepDelay)

                val newVolume = min(
                    INITIAL_VOLUME_PERCENTAGE + (volumeIncrement * step),
                    MAX_VOLUME_PERCENTAGE
                )

                try {
                    mediaPlayer?.setVolume(newVolume, newVolume)
                } catch (e: Exception) {
                    // Continue ramping even if one step fails
                }
            }
        }
    }

    private fun stopAlarm() {
        // Cancel volume ramping
        volumeRampJob?.cancel()

        // Stop media player
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            // Ensure cleanup continues
        }
        mediaPlayer = null

        // Stop vibration
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Continue cleanup
        }

        // Restore original audio settings
        restoreOriginalAudioSettings()

        // Release wake lock
        try {
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                }
            }
        } catch (e: Exception) {
            // Continue cleanup
        }

        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun restoreOriginalAudioSettings() {
        try {
            audioManager?.let { am ->
                am.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    originalAlarmVolume,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                )
                am.ringerMode = originalRingerMode
            }
        } catch (e: Exception) {
            // Best effort restoration
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ADHD Focus Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ultra-loud alarms for ADHD focus"
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createAlarmNotification(alarmId: Long): Notification {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ADHD Focus Alarm Active")
            .setContentText("Alarm $alarmId is ringing - Complete your mission to dismiss")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Alarm",
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        stopAlarm()
        serviceScope.cancel()
        super.onDestroy()
    }
}