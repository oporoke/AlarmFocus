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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.omondit.alarmfocus.AlarmFocusApplication
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.presentation.MissionActivity
import com.omondit.alarmfocus.utils.EncryptionManager
import com.omondit.alarmfocus.utils.MissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.min

class AlarmService : Service() {

    companion object {
        const val ACTION_START_ALARM = "START_ALARM"
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val ACTION_MISSION_COMPLETED = "MISSION_COMPLETED"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "ADHD_ALARM_CHANNEL"
        private const val TAG = "AlarmService"

        private const val VOLUME_RAMP_DURATION = 10_000L
        private const val VOLUME_RAMP_STEPS = 20
        private const val MAX_VOLUME_PERCENTAGE = 1.0f
        private const val INITIAL_VOLUME_PERCENTAGE = 0.7f
        private const val MISSION_START_DELAY = 3_000L
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var volumeRampJob: Job? = null
    private var missionStartJob: Job? = null

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var missionManager: MissionManager
    private lateinit var encryptionManager: EncryptionManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var originalAlarmVolume: Int = 0
    private var originalRingerMode: Int = 0
    private var currentAlarmId: Long = -1
    private var isMissionActive: Boolean = false

    override fun onCreate() {
        super.onCreate()
        val appModule = (application as AlarmFocusApplication).appModule
        alarmRepository = appModule.alarmRepository
        missionManager = appModule.missionManager
        encryptionManager = appModule.encryptionManager
        createNotificationChannel()
        initializeSystemServices()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started: action=${intent?.action}")

        val savedState = encryptionManager.getAlarmServiceState()

        if (savedState.activeAlarmId != -1L && intent?.action == ACTION_START_ALARM) {
            val requestedAlarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
            if (savedState.activeAlarmId == requestedAlarmId) {
                currentAlarmId = savedState.activeAlarmId
                isMissionActive = savedState.missionActive
                Log.d(TAG, "Restored encrypted state: alarmId=$currentAlarmId, missionActive=$isMissionActive")
            }
        }

        when (intent?.action) {
            ACTION_START_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
                val soundUri = intent.getStringExtra(EXTRA_SOUND_URI)

                if (alarmId != -1L) {
                    startForeground(NOTIFICATION_ID, createAlarmNotification(alarmId))
                    serviceScope.launch {
                        alarmRepository.markAlarmTriggered(alarmId)
                    }
                    startAlarm(alarmId, soundUri)
                }
            }
            ACTION_STOP_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
                if (alarmId != -1L) {
                    if (isMissionActive && alarmId == currentAlarmId) {
                        Log.w(TAG, "Cannot stop - mission active")
                        val notification = createMissionActiveNotification(alarmId)
                        val notificationManager = getSystemService(NotificationManager::class.java)
                        notificationManager?.notify(NOTIFICATION_ID, notification)
                        return START_STICKY
                    }
                    serviceScope.launch {
                        alarmRepository.markAlarmDismissed(alarmId)
                    }
                }
                stopAlarm()
            }
            ACTION_MISSION_COMPLETED -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
                if (alarmId != -1L && alarmId == currentAlarmId && isMissionActive) {
                    Log.i(TAG, "Mission completed: $alarmId")
                    isMissionActive = false
                    encryptionManager.updateMissionActiveStatus(false)
                    serviceScope.launch {
                        alarmRepository.markAlarmDismissed(alarmId)

                        // Enable post-alarm app blocking
                        val appBlockManager = com.omondit.alarmfocus.utils.AppBlockManager(this@AlarmService)
                        appBlockManager.enablePostAlarmBlocking()
                        Log.d(TAG, "Post-alarm blocking enabled")
                    }
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
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
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
        wakeLock?.acquire(30 * 60 * 1000L)
        currentAlarmId = alarmId
        isMissionActive = false

        val alarm = runBlocking { alarmRepository.getAlarmById(alarmId) }
        val missionConfigJson = alarm?.missionConfig ?: "{}"

        encryptionManager.saveAlarmServiceState(
            alarmId = alarmId,
            soundUri = soundUri,
            missionConfig = missionConfigJson,
            missionActive = false
        )

        saveOriginalAudioSettings()
        overrideAudioSettings()
        initializeMediaPlayer(soundUri)
        startVibration()
        startVolumeRamping()
        scheduleMissionStart(alarmId)
    }

    private fun scheduleMissionStart(alarmId: Long) {
        missionStartJob = serviceScope.launch {
            delay(MISSION_START_DELAY)
            if (currentAlarmId == alarmId && !isMissionActive) {
                val missionStarted = missionManager.startMission(alarmId)
                if (missionStarted) {
                    isMissionActive = true
                    encryptionManager.updateMissionActiveStatus(true)

                    val notification = createMissionActiveNotification(alarmId)
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager?.notify(NOTIFICATION_ID, notification)
                    Log.d(TAG, "Mission active: $alarmId")
                }
            }
        }
    }

    private fun saveOriginalAudioSettings() {
        audioManager?.let {
            originalAlarmVolume = it.getStreamVolume(AudioManager.STREAM_ALARM)
            originalRingerMode = it.ringerMode
        }
    }

    private fun overrideAudioSettings() {
        audioManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                } catch (e: SecurityException) {
                    Log.w(TAG, "Cannot override DND")
                }
            }
            val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            am.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
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

                if (!soundUri.isNullOrEmpty()) {
                    setDataSource(this@AlarmService, android.net.Uri.parse(soundUri))
                } else {
                    val afd = this@AlarmService.assets.openFd("default_alarm_ultra_loud.mp3")
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                }

                isLooping = true
                setVolume(INITIAL_VOLUME_PERCENTAGE, INITIAL_VOLUME_PERCENTAGE)
                setOnPreparedListener { it.start() }
                setOnErrorListener { _, _, _ ->
                    initializeFallbackAlarm()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer init failed", e)
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
                val afd = this@AlarmService.assets.openFd("default_alarm_ultra_loud.mp3")
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                setVolume(INITIAL_VOLUME_PERCENTAGE, INITIAL_VOLUME_PERCENTAGE)
                prepareAsync()
                setOnPreparedListener { it.start() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback alarm failed", e)
        }
    }

    private fun startVibration() {
        try {
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 500, 200, 500, 200, 1000, 200, 500)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)
                    vib.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(longArrayOf(0, 500, 200, 500, 200, 1000, 200, 500), 0)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed", e)
        }
    }

    private fun startVolumeRamping() {
        volumeRampJob = serviceScope.launch {
            val stepDelay = VOLUME_RAMP_DURATION / VOLUME_RAMP_STEPS
            val volumeIncrement = (MAX_VOLUME_PERCENTAGE - INITIAL_VOLUME_PERCENTAGE) / VOLUME_RAMP_STEPS

            for (step in 1..VOLUME_RAMP_STEPS) {
                delay(stepDelay)
                val newVolume = min(INITIAL_VOLUME_PERCENTAGE + (volumeIncrement * step), MAX_VOLUME_PERCENTAGE)
                try {
                    mediaPlayer?.setVolume(newVolume, newVolume)
                } catch (e: Exception) {
                    Log.w(TAG, "Volume ramp failed", e)
                }
            }
        }
    }

    private fun stopAlarm() {
        currentAlarmId = -1L
        isMissionActive = false
        volumeRampJob?.cancel()
        missionStartJob?.cancel()

        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.w(TAG, "MediaPlayer cleanup failed", e)
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Vibrator cleanup failed", e)
        }

        restoreOriginalAudioSettings()

        try {
            wakeLock?.let { if (it.isHeld) it.release() }
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock cleanup failed", e)
        }

        encryptionManager.clearAlarmServiceState()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun restoreOriginalAudioSettings() {
        try {
            audioManager?.let {
                it.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, 0)
                it.ringerMode = originalRingerMode
            }
        } catch (e: Exception) {
            Log.w(TAG, "Audio restore failed", e)
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
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun createAlarmNotification(alarmId: Long): Notification {
        val savedState = encryptionManager.getAlarmServiceState()
        val missionConfig = savedState.missionConfig

        val missionIntent = Intent(this, MissionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("alarm_id", alarmId)
            putExtra("mission_config", missionConfig)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            missionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ADHD Focus Alarm Active")
            .setContentText("Tap to complete wake-up challenge")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .build()
    }

    private fun createMissionActiveNotification(alarmId: Long): Notification {
        val savedState = encryptionManager.getAlarmServiceState()
        val missionConfig = savedState.missionConfig

        val missionIntent = Intent(this, MissionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("alarm_id", alarmId)
            putExtra("mission_config", missionConfig)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            missionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wake-Up Challenge Active")
            .setContentText("Tap to complete the challenge")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .build()
    }

    override fun onDestroy() {
        stopAlarm()
        serviceScope.cancel()
        super.onDestroy()
    }
}
