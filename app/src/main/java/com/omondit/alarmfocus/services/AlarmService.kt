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
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.repository.AlarmRepositoryImpl
import com.omondit.alarmfocus.utils.MissionManager
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Ultra-Loud Alarm Service with Mission Integration
 * Handles maximum volume alarm playback and mission coordination
 */
class AlarmService : Service() {

    companion object {
        const val ACTION_START_ALARM = "START_ALARM"
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val ACTION_MISSION_COMPLETED = "MISSION_COMPLETED"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "ADHD_ALARM_CHANNEL"

        private const val VOLUME_RAMP_DURATION = 10_000L // 10 seconds
        private const val VOLUME_RAMP_STEPS = 20
        private const val MAX_VOLUME_PERCENTAGE = 1.0f
        private const val INITIAL_VOLUME_PERCENTAGE = 0.7f
        private const val MISSION_START_DELAY = 3_000L // 3 seconds after alarm starts
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var volumeRampJob: Job? = null
    private var missionStartJob: Job? = null

    private lateinit var alarmRepository: AlarmRepositoryImpl
    private lateinit var missionManager: MissionManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var originalAlarmVolume: Int = 0
    private var originalRingerMode: Int = 0
    private var currentAlarmId: Long = -1
    private var isMissionActive: Boolean = false

    override fun onCreate() {
        super.onCreate()

        // Initialize repository and mission manager
        val database = AppDatabase.getDatabase(this)
        alarmRepository = AlarmRepositoryImpl(database.alarmDao())
        missionManager = MissionManager(this, alarmRepository)

        createNotificationChannel()
        initializeSystemServices()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
                val soundUri = intent.getStringExtra(EXTRA_SOUND_URI)

                if (alarmId != -1L) {
                    serviceScope.launch {
                        alarmRepository.markAlarmTriggered(alarmId)
                    }
                    startAlarm(alarmId, soundUri)
                }
            }
            ACTION_STOP_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
                if (alarmId != -1L) {
                    serviceScope.launch {
                        alarmRepository.markAlarmDismissed(alarmId)
                    }
                }
                stopAlarm()
            }
            ACTION_MISSION_COMPLETED -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
                if (alarmId != -1L && alarmId == currentAlarmId) {
                    // Mission completed successfully, stop alarm
                    stopAlarm()
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun initializeSystemServices() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
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
        isMissionActive = false

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

        // Schedule mission start after a few seconds of alarm
        scheduleMissionStart(alarmId)
    }

    private fun scheduleMissionStart(alarmId: Long) {
        missionStartJob = serviceScope.launch {
            delay(MISSION_START_DELAY)

            if (currentAlarmId == alarmId && !isMissionActive) {
                val missionStarted = missionManager.startMission(alarmId)
                if (missionStarted) {
                    isMissionActive = true

                    // Update notification to show mission is active
                    val notification = createMissionActiveNotification(alarmId)
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager?.notify(NOTIFICATION_ID, notification)
                } else {
                    // Mission failed to start, continue with regular alarm
                    // User can dismiss manually or wait for timeout
                }
            }
        }
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
                    // Use default system alarm sound
                    setDataSource(
                        this@AlarmService,
                        android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                    )
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
            // Critical error - service will continue but without sound
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
            val volumeIncrement = (MAX_VOLUME_PERCENTAGE - INITIAL_VOLUME_PERCENTAGE) /
                VOLUME_RAMP_STEPS

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
        currentAlarmId = -1L
        isMissionActive = false

        // Cancel jobs
        volumeRampJob?.cancel()
        missionStartJob?.cancel()

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
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ADHD Focus Alarm Active")
            .setContentText("Alarm is ringing - Wake-up challenge will appear shortly")
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

    private fun createMissionActiveNotification(alarmId: Long): Notification {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wake-Up Challenge Active")
            .setContentText("Complete your mission to stop the alarm")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Emergency Stop",
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
