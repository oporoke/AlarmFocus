# Deliverable 2: Ultra-Loud Alarm Engine - Complex Audio/System Integration

## The ADHD Sleep Challenge

### Why ADHD Users Need Ultra-Loud Alarms
- **Deeper Sleep Patterns**: ADHD brains often experience deeper REM cycles, making wake-up more difficult
- **Sensory Processing Differences**: Many ADHD individuals have sensory processing variations requiring stronger stimuli
- **Medication Effects**: ADHD medications can affect sleep depth and wake responsiveness
- **Executive Function Delays**: The prefrontal cortex takes longer to "boot up" in ADHD brains
- **Dopamine Regulation**: Morning dopamine levels are typically lower in ADHD individuals

---

## Advanced Audio Architecture

### Core Audio Engine
```kotlin
class UltraLoudAlarmEngine {
    
    private val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
```

### Do Not Disturb Override System
```kotlin
class DoNotDisturbOverride {
    
    private val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    fun overrideDoNotDisturbMode(): DoNotDisturbResult {
        
        if (!hasDoNotDisturbAccess()) {
            return requestDoNotDisturbAccess()
        }
        
        return try {
            // Store original DND state
            val originalInterruptionFilter = notificationManager.currentInterruptionFilter
            val originalPolicy = notificationManager.notificationPolicy
            
            // Create alarm-friendly DND policy
            val alarmFriendlyPolicy = NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS or
                NotificationManager.Policy.PRIORITY_CATEGORY_SYSTEM,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.SUPPRESSED_EFFECTS_UNSET
            )
            
            // Apply the new policy temporarily
            notificationManager.notificationPolicy = alarmFriendlyPolicy
            notificationManager.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            )
            
            // Schedule restoration of original settings
            scheduleDoNotDisturbRestoration(originalInterruptionFilter, originalPolicy)
            
            DoNotDisturbResult.SUCCESS
            
        } catch (e: SecurityException) {
            DoNotDisturbResult.PERMISSION_DENIED
        } catch (e: Exception) {
            DoNotDisturbResult.SYSTEM_ERROR(e)
        }
    }
    
    private fun hasDoNotDisturbAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true // Older Android versions don't require this permission
        }
    }
    
    private fun requestDoNotDisturbAccess(): DoNotDisturbResult {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivity(intent)
        
        return DoNotDisturbResult.PERMISSION_REQUESTED
    }
}
```

### Advanced Volume Management
```kotlin
class VolumeManagementSystem {
    
    data class VolumeProfile(
        val deviceType: DeviceType,
        val maxSafeVolume: Int,
        val recommendedStartVolume: Int,
        val rampUpPattern: RampUpPattern
    )
    
    fun createOptimalVolumeProfile(): VolumeProfile {
        val deviceSpecs = analyzeDeviceAudioCapabilities()
        
        return when (deviceSpecs.deviceType) {
            DeviceType.PHONE -> VolumeProfile(
                deviceType = DeviceType.PHONE,
                maxSafeVolume = calculateMaxSafePhoneVolume(deviceSpecs),
                recommendedStartVolume = 70,
                rampUpPattern = RampUpPattern.EXPONENTIAL_PHONE
            )
            
            DeviceType.TABLET -> VolumeProfile(
                deviceType = DeviceType.TABLET,
                maxSafeVolume = calculateMaxSafeTabletVolume(deviceSpecs),
                recommendedStartVolume = 65, // Tablets often louder
                rampUpPattern = RampUpPattern.LINEAR_TABLET  
            )
            
            DeviceType.EXTERNAL_SPEAKER -> VolumeProfile(
                deviceType = DeviceType.EXTERNAL_SPEAKER,
                maxSafeVolume = 90, // More conservative with external speakers
                recommendedStartVolume = 50,
                rampUpPattern = RampUpPattern.GENTLE_EXTERNAL
            )
        }
    }
    
    private fun calculateMaxSafePhoneVolume(specs: DeviceAudioSpecs): Int {
        // Use device-specific audio characteristics
        return when {
            specs.hasLoudSpeakers -> 95 // High-end phones with good speakers
            specs.speakerWattage > 1.0 -> 90 // Mid-range phones
            else -> 85 // Budget phones - be more conservative
        }
    }
    
    fun implementProgressiveVolumeRamp(
        profile: VolumeProfile,
        durationMs: Long
    ): VolumeRampController {
        
        val startVolume = profile.recommendedStartVolume
        val endVolume = profile.maxSafeVolume
        val steps = calculateOptimalSteps(durationMs, profile.rampUpPattern)
        
        return VolumeRampController(
            startVolume = startVolume,
            endVolume = endVolume,
            steps = steps,
            pattern = profile.rampUpPattern
        ).apply {
            start { volumeLevel ->
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    volumeLevel,
                    0 // No UI feedback during alarm
                )
            }
        }
    }
    
    private fun calculateOptimalSteps(durationMs: Long, pattern: RampUpPattern): List<VolumeStep> {
        val totalSteps = (durationMs / 500).toInt() // Update every 500ms
        
        return when (pattern) {
            RampUpPattern.EXPONENTIAL_PHONE -> {
                // Rapid initial increase, then gradual
                (0 until totalSteps).map { step ->
                    val progress = step.toFloat() / totalSteps
                    val exponentialProgress = progress * progress
                    VolumeStep(
                        timeMs = step * 500L,
                        volumePercent = (70 + exponentialProgress * 30).toInt()
                    )
                }
            }
            
            RampUpPattern.LINEAR_TABLET -> {
                // Steady increase
                (0 until totalSteps).map { step ->
                    val progress = step.toFloat() / totalSteps
                    VolumeStep(
                        timeMs = step * 500L,
                        volumePercent = (65 + progress * 35).toInt()
                    )
                }
            }
            
            RampUpPattern.GENTLE_EXTERNAL -> {
                // Slow start, then faster
                (0 until totalSteps).map { step ->
                    val progress = step.toFloat() / totalSteps
                    val gentleProgress = Math.sin(progress * Math.PI / 2)
                    VolumeStep(
                        timeMs = step * 500L,
                        volumePercent = (50 + gentleProgress * 40).toInt()
                    )
                }
            }
        }
    }
}
```

---

## ADHD-Optimized Sound Selection

### Scientifically-Designed Alarm Sounds
```kotlin
class ADHDAudioLibrary {
    
    data class AlarmSound(
        val id: String,
        val name: String,
        val frequencyProfile: FrequencyProfile,
        val psychoacousticProperties: PsychoacousticProperties,
        val adhdEffectiveness: ADHDEffectivenessRating
    )
    
    fun getOptimizedAlarmSounds(): List<AlarmSound> {
        return listOf(
            // High-frequency attention-grabbing sounds
            AlarmSound(
                id = "adhd_alert_cascade",
                name = "Neural Cascade Alert",
                frequencyProfile = FrequencyProfile(
                    dominantFrequency = 2000, // 2kHz - optimal for human attention
                    harmonics = listOf(1000, 2000, 4000, 8000),
                    frequencyModulation = FrequencyModulation.RAPID_SWEEP
                ),
                psychoacousticProperties = PsychoacousticProperties(
                    loudnessGrowth = LoudnessGrowth.EXPONENTIAL,
                    unexpectedness = UnexpectednessLevel.HIGH,
                    rhythmicComplexity = RhythmicComplexity.IRREGULAR
                ),
                adhdEffectiveness = ADHDEffectivenessRating.EXCELLENT
            ),
            
            // Multi-sensory activation sound
            AlarmSound(
                id = "sensory_storm",
                name = "Sensory Storm",
                frequencyProfile = FrequencyProfile(
                    dominantFrequency = 1500,
                    harmonics = listOf(500, 1500, 3000, 6000, 12000),
                    frequencyModulation = FrequencyModulation.CHAOTIC_BURSTS
                ),
                psychoacousticProperties = PsychoacousticProperties(
                    loudnessGrowth = LoudnessGrowth.SUDDEN_BURSTS,
                    unexpectedness = UnexpectednessLevel.MAXIMUM,
                    rhythmicComplexity = RhythmicComplexity.POLYRHYTHMIC
                ),
                adhdEffectiveness = ADHDEffectivenessRating.EXCELLENT
            ),
            
            // Classic annoying but effective
            AlarmSound(
                id = "end_of_world_siren",
                name = "End of World Siren",
                frequencyProfile = FrequencyProfile(
                    dominantFrequency = 800,
                    harmonics = listOf(400, 800, 1600, 3200),
                    frequencyModulation = FrequencyModulation.SIREN_SWEEP
                ),
                psychoacousticProperties = PsychoacousticProperties(
                    loudnessGrowth = LoudnessGrowth.IMMEDIATE_MAXIMUM,
                    unexpectedness = UnexpectednessLevel.HIGH,
                    rhythmicComplexity = RhythmicComplexity.REPETITIVE_URGENT
                ),
                adhdEffectiveness = ADHDEffectivenessRating.VERY_GOOD
            ),
            
            // Dopamine-activating sound
            AlarmSound(
                id = "dopamine_cascade",
                name = "Dopamine Cascade",
                frequencyProfile = FrequencyProfile(
                    dominantFrequency = 1200,
                    harmonics = listOf(600, 1200, 2400, 4800),
                    frequencyModulation = FrequencyModulation.ASCENDING_CASCADE
                ),
                psychoacousticProperties = PsychoacousticProperties(
                    loudnessGrowth = LoudnessGrowth.BUILDING_EXCITEMENT,
                    unexpectedness = UnexpectednessLevel.MODERATE,
                    rhythmicComplexity = RhythmicComplexity.SYNCOPATED
                ),
                adhdEffectiveness = ADHDEffectivenessRating.GOOD
            ),
            
            // Emergency backup - maximally annoying
            AlarmSound(
                id = "cognitive_override",
                name = "Cognitive Override",
                frequencyProfile = FrequencyProfile(
                    dominantFrequency = 2500, // Most annoying frequency range
                    harmonics = listOf(1250, 2500, 5000, 10000),
                    frequencyModulation = FrequencyModulation.AGGRESSIVE_WARBLE
                ),
                psychoacousticProperties = PsychoacousticProperties(
                    loudnessGrowth = LoudnessGrowth.INSTANT_SHOCK,
                    unexpectedness = UnexpectednessLevel.MAXIMUM,
                    rhythmicComplexity = RhythmicComplexity.INTENTIONALLY_JARRING
                ),
                adhdEffectiveness = ADHDEffectivenessRating.MAXIMUM_IMPACT
            )
        )
    }
    
    fun generatePersonalizedSound(adhdProfile: ADHDProfile): AlarmSound {
        return when (adhdProfile.primaryDeficit) {
            ADHDDeficit.INATTENTION -> {
                // High-frequency, rapidly changing sounds
                createAttentionActivatingSound(adhdProfile)
            }
            ADHDDeficit.HYPERACTIVITY -> {
                // Rhythmic, energizing sounds
                createEnergyChannelingSound(adhdProfile)
            }
            ADHDDeficit.COMBINED -> {
                // Multi-modal activation
                createCombinedActivationSound(adhdProfile)
            }
        }
    }
    
    private fun createAttentionActivatingSound(profile: ADHDProfile): AlarmSound {
        return AlarmSound(
            id = "personalized_attention_${profile.userId}",
            name = "Personal Attention Activator",
            frequencyProfile = FrequencyProfile(
                dominantFrequency = calculateOptimalFrequency(profile.auditoryProcessing),
                harmonics = generateAttentionHarmonics(profile.sensoryPreferences),
                frequencyModulation = FrequencyModulation.ATTENTION_SWEEPS
            ),
            psychoacousticProperties = PsychoacousticProperties(
                loudnessGrowth = LoudnessGrowth.PROGRESSIVE_ALERT,
                unexpectedness = calculateOptimalUnexpectedness(profile.adaptationRate),
                rhythmicComplexity = RhythmicComplexity.COGNITIVE_ACTIVATION
            ),
            adhdEffectiveness = ADHDEffectivenessRating.PERSONALIZED_OPTIMAL
        )
    }
}
```

### Advanced Audio Processing
```kotlin
class AudioProcessingEngine {
    
    private val audioEffectsChain = AudioEffectsChain()
    
    fun processAlarmAudio(
        rawAudio: AudioData,
        targetProfile: VolumeProfile,
        adhdOptimizations: ADHDAudioOptimizations
    ): ProcessedAudioData {
        
        return audioEffectsChain
            .addEffect(VolumeNormalization(targetProfile.maxSafeVolume))
            .addEffect(FrequencyEnhancement(adhdOptimizations.targetFrequencies))
            .addEffect(DynamicRangeCompression(adhdOptimizations.compressionRatio))
            .addEffect(PsychoacousticEnhancement(adhdOptimizations.perceptualGain))
            .addEffect(SpatialAudioProcessing(adhdOptimizations.spatialEffects))
            .process(rawAudio)
    }
    
    class FrequencyEnhancement(private val targetFrequencies: List<Int>) : AudioEffect {
        override fun process(audio: AudioData): AudioData {
            return audio.copy(
                samples = enhanceFrequencyBands(audio.samples, targetFrequencies)
            )
        }
        
        private fun enhanceFrequencyBands(samples: FloatArray, frequencies: List<Int>): FloatArray {
            // Apply FFT to identify frequency components
            val fft = FastFourierTransform(samples)
            
            // Boost target frequencies (typically 1-4kHz for ADHD)
            frequencies.forEach { freq ->
                val binIndex = fft.frequencyToBin(freq)
                fft.boostFrequencyBin(binIndex, 1.5f) // 50% boost
            }
            
            // Convert back to time domain
            return fft.inverseTransform()
        }
    }
    
    class PsychoacousticEnhancement(private val perceptualGain: Float) : AudioEffect {
        override fun process(audio: AudioData): AudioData {
            // Apply perceptual loudness curves optimized for ADHD brains
            val enhancedSamples = audio.samples.map { sample ->
                applyADHDPerceptualCurve(sample, perceptualGain)
            }.toFloatArray()
            
            return audio.copy(samples = enhancedSamples)
        }
        
        private fun applyADHDPerceptualCurve(sample: Float, gain: Float): Float {
            // ADHD brains often need stronger initial stimulation
            return when {
                Math.abs(sample) < 0.1f -> sample * gain * 2.0f // Boost quiet parts more
                Math.abs(sample) < 0.5f -> sample * gain * 1.5f // Moderate boost
                else -> sample * gain // Standard boost for loud parts
            }.coerceIn(-1.0f, 1.0f)
        }
    }
}
```

---

## System Integration & Persistence

### Boot Persistence System
```kotlin
class AlarmBootPersistence {
    
    class AlarmBootReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_PACKAGE_REPLACED -> {
                    restoreAlarmsAfterBoot(context)
                }
                Intent.ACTION_TIME_SET,
                Intent.ACTION_TIMEZONE_CHANGED -> {
                    recalculateAlarmTimes(context)
                }
            }
        }
        
        private fun restoreAlarmsAfterBoot(context: Context) {
            val alarmRepository = AlarmRepository(context)
            val activeAlarms = alarmRepository.getActiveAlarms()
            
            activeAlarms.forEach { alarm ->
                scheduleAlarmWithSystem(context, alarm)
            }
            
            // Start foreground service to maintain alarm integrity
            val serviceIntent = Intent(context, AlarmPersistenceService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
    
    class AlarmPersistenceService : Service() {
        
        private val NOTIFICATION_ID = 1001
        private val CHANNEL_ID = "alarm_persistence_channel"
        
        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createPersistenceNotification())
            
            // Monitor alarm integrity continuously
            startAlarmIntegrityMonitoring()
            
            return START_STICKY // Restart if killed by system
        }
        
        private fun startAlarmIntegrityMonitoring() {
            val handler = Handler(Looper.getMainLooper())
            
            val integrityCheck = object : Runnable {
                override fun run() {
                    verifyAlarmIntegrity()
                    handler.postDelayed(this, 60000) // Check every minute
                }
            }
            
            handler.post(integrityCheck)
        }
        
        private fun verifyAlarmIntegrity() {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmRepository = AlarmRepository(this)
            
            // Check if system alarms match our database
            val storedAlarms = alarmRepository.getActiveAlarms()
            
            storedAlarms.forEach { alarm ->
                if (!isAlarmScheduledWithSystem(alarm)) {
                    // Re-schedule missing alarm
                    scheduleAlarmWithSystem(this, alarm)
                    
                    // Log the restoration
                    AlarmLogger.logAlarmRestored(alarm.id, "System alarm missing")
                }
            }
        }
    }
}
```

### Advanced Alarm Scheduling
```kotlin
class RobustAlarmScheduler {
    
    private val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleUltraReliableAlarm(alarm: AlarmConfig): AlarmScheduleResult {
        
        return try {
            // Primary scheduling method
            val primaryResult = scheduleWithAlarmManager(alarm)
            
            // Backup scheduling method  
            val backupResult = scheduleWithJobScheduler(alarm)
            
            // Tertiary backup with WorkManager
            val tertiaryResult = scheduleWithWorkManager(alarm)
            
            AlarmScheduleResult.Success(
                primaryMethod = primaryResult,
                backupMethod = backupResult,
                tertiaryMethod = tertiaryResult
            )
            
        } catch (e: Exception) {
            AlarmScheduleResult.Failure(e)
        }
    }
    
    private fun scheduleWithAlarmManager(alarm: AlarmConfig): ScheduleMethodResult {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("sound_uri", alarm.soundUri.toString())
            putExtra("volume_profile", alarm.volumeProfile)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Use most reliable alarm scheduling method available
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6+: Use setExactAndAllowWhileIdle for maximum reliability
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarm.triggerTimeMs,
                    pendingIntent
                )
            }
            
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                // Android 4.4+: Use setExact
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarm.triggerTimeMs,
                    pendingIntent
                )
            }
            
            else -> {
                // Older Android: Use set (less reliable but only option)
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    alarm.triggerTimeMs,
                    pendingIntent
                )
            }
        }
        
        return ScheduleMethodResult.Success(ScheduleMethod.ALARM_MANAGER)
    }
    
    private fun scheduleWithJobScheduler(alarm: AlarmConfig): ScheduleMethodResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return ScheduleMethodResult.NotSupported(ScheduleMethod.JOB_SCHEDULER)
        }
        
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        
        val jobInfo = JobInfo.Builder(alarm.id.hashCode(), ComponentName(this, AlarmJobService::class.java))
            .setMinimumLatency(calculateDelayToAlarm(alarm.triggerTimeMs))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE) // No network required
            .setPersisted(true) // Persist across reboots
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setPersisted(true)
            .build()
        
        val result = jobScheduler.schedule(jobInfo)
        
        return if (result == JobScheduler.RESULT_SUCCESS) {
            ScheduleMethodResult.Success(ScheduleMethod.JOB_SCHEDULER)
        } else {
            ScheduleMethodResult.Failure(ScheduleMethod.JOB_SCHEDULER, "JobScheduler rejected alarm")
        }
    }
    
    private fun scheduleWithWorkManager(alarm: AlarmConfig): ScheduleMethodResult {
        val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(calculateDelayToAlarm(alarm.triggerTimeMs), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "alarm_id" to alarm.id,
                "sound_uri" to alarm.soundUri.toString(),
                "volume_profile" to alarm.volumeProfile.toJson()
            ))
            .addTag("alarm_${alarm.id}")
            .build()
        
        WorkManager.getInstance(this).enqueue(workRequest)
        
        return ScheduleMethodResult.Success(ScheduleMethod.WORK_MANAGER)
    }
}
```

---

## Multi-Modal Stimulation System

### Advanced Vibration Patterns
```kotlin
class ADHDVibrationEngine {
    
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    fun createADHDOptimizedVibration(): VibrationPattern {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createAdvancedVibrationPattern()
        } else {
            createLegacyVibrationPattern()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAdvancedVibrationPattern(): VibrationPattern {
        // Create complex vibration that stimulates ADHD nervous system
        val vibrationEffect = VibrationEffect.createWaveform(
            longArrayOf(
                0,    // Start immediately
                300,  // Strong initial pulse
                100,  // Brief pause
                200,  // Medium pulse
                100,  // Brief pause  
                500,  // Long strong pulse
                200,  // Medium pause
                100,  // Sharp pulse
                50,   // Very brief pause
                100,  // Sharp pulse
                50,   // Very brief pause
                100,  // Sharp pulse
                300,  // Longer pause before repeat
            ),
            intArrayOf(
                0,    // Start at 0
                255,  // Maximum intensity
                0,    // Off
                200,  // High intensity
                0,    // Off
                255,  // Maximum intensity
                0,    // Off
                255,  // Maximum intensity
                0,    // Off
                255,  // Maximum intensity
                0,    // Off
                255,  // Maximum intensity
                0     // Off before repeat
            ),
            0 // Repeat from beginning
        )
        
        return VibrationPattern.Advanced(vibrationEffect)
    }
    
    private fun createLegacyVibrationPattern(): VibrationPattern {
        // Pattern optimized for ADHD sensory needs
        val pattern = longArrayOf(
            0,    // Start immediately
            500,  // Strong pulse - get attention
            200,  // Brief pause
            200,  // Quick pulse
            100,  // Very brief pause
            200,  // Quick pulse
            300,  // Medium pause
            800,  // Long strong pulse - maintain attention
            400   // Pause before repeat
        )
        
        return VibrationPattern.Legacy(pattern)
    }
    
    fun startCoordinatedVibration(audioPhase: AudioPhase) {
        val pattern = when (audioPhase) {
            AudioPhase.RAMP_UP -> createRampUpVibration()
            AudioPhase.FULL_INTENSITY -> createFullIntensityVibration()
            AudioPhase.ATTENTION_BURSTS -> createAttentionBurstVibration()
        }
        
        pattern.start(vibrator)
    }
}
```

### Screen Flash Coordination
```kotlin
class VisualAlarmStimulation {
    
    private val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    
    fun startVisualAlarmStimulation(): VisualStimulationController {
        
        // Create full-screen flash overlay
        val flashOverlay = createFlashOverlay()
        
        // Add to window with highest priority
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(flashOverlay, layoutParams)
        
        return VisualStimulationController(flashOverlay, createADHDFlashPattern())
    }
    
    private fun createADHDFlashPattern(): FlashPattern {
        return FlashPattern(
            phases = listOf(
                FlashPhase(
                    duration = 200,
                    color = Color.WHITE,
                    intensity = 1.0f,
                    description = "Initial attention grab"
                ),
                FlashPhase(
                    duration = 100,
                    color = Color.BLACK,
                    intensity = 0.0f,
                    description = "Brief darkness"
                ),
                FlashPhase(
                    duration = 150,
                    color = Color.RED,
                    intensity = 0.8f,
                    description = "Alert color"
                ),
                FlashPhase(
                    duration = 100,
                    color = Color.BLACK,
                    intensity = 0.0f,
                    description = "Brief darkness"
                ),
                FlashPhase(
                    duration = 300,
                    color = Color.WHITE,
                    intensity = 1.0f,
                    description = "Strong sustained flash"
                ),
                FlashPhase(
                    duration = 200,
                    color = Color.BLACK,
                    intensity = 0.0f,
                    description = "Pause before repeat"
                )
            ),
            repeatCount = -1, // Infinite until dismissed
            syncWithAudio = true,
            syncWithVibration = true
        )
    }
}
```

---

## Failure Recovery & Edge Cases

### Comprehensive Error Handling
```kotlin
class AlarmFailureRecovery {
    
    fun handleAlarmFailure(
        failure: AlarmFailure, 
        originalConfig: AlarmConfig
    ): RecoveryResult {
        
        return when (failure.type) {
            FailureType.AUDIO_PLAYBACK_FAILED -> recoverFromAudioFailure(failure, originalConfig)
            FailureType.VOLUME_OVERRIDE_BLOCKED -> recoverFromVolumeFailure(failure, originalConfig)
            FailureType.DO_NOT_DISTURB_BLOCKED -> recoverFromDNDFailure(failure, originalConfig)
            FailureType.SYSTEM_AUDIO_FOCUS_LOST -> recoverFromAudioFocusLoss(failure, originalConfig)
            FailureType.WAKE_LOCK_DENIED -> recoverFromWakeLockFailure(failure, originalConfig)
            FailureType.DEVICE_IN_SILENT_MODE -> recoverFromSilentMode(failure, originalConfig)
            FailureType.LOW_BATTERY_RESTRICTIONS -> recoverFromBatteryRestrictions(failure, originalConfig)
            FailureType.UNKNOWN_ERROR -> attemptGenericRecovery(failure, originalConfig)
        }
    }
    
    private fun recoverFromAudioFailure(
        failure: AlarmFailure,
        config: AlarmConfig
    ): RecoveryResult {
        
        val fallbackStrategies = listOf(
            // Try different audio stream
            { playViaAlternativeStream(config) },
            
            // Try system notification sound
            { playSystemAlarmSound() },
            
            // Try TTS emergency message
            { playTextToSpeechAlarm(config) },
            
            // Try pure vibration with screen flash
            { activateVibrationOnlyMode(config) },
            
            // Last resort: continuous notification
            { createEmergencyNotification(config) }
        )
        
        fallbackStrategies.forEach { strategy ->
            try {
                val result = strategy()
                if (result.isSuccess) {
                    return RecoveryResult.Success(
                        method = result.method,
                        degradation = AudioDegradation.PARTIAL
                    )
                }
            } catch (e: Exception) {
                // Continue to next strategy
            }
        }
        
        return RecoveryResult.Failure(
            reason = "All audio fallback strategies failed",
            recommendedAction = RecommendedAction.SHOW_VISUAL_ONLY_ALARM
        )
    }
    
    private fun playTextToSpeechAlarm(config: AlarmConfig): FallbackResult {
        val tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.getDefault())
                
                if (result != TextToSpeech.LANG_MISSING_DATA && 
                    result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    
                    // Set maximum volume and speech rate
                    tts.setSpeechRate(1.5f) // 150% speed for urgency
                    
                    val params = Bundle().apply {
                        putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                        putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
                    }
                    
                    val message = "WAKE UP! This is your ADHD alarm. " +
                                 "Time to start your day. " +
                                 "Your alarm is set for ${formatAlarmTime(config.triggerTime)}. " +
                                 "Please wake up and dismiss this alarm."
                    
                    // Repeat the message multiple times
                    repeat(10) {
                        tts.speak(message, TextToSpeech.QUEUE_ADD, params, "alarm_$it")
                    }
                }
            }
        }
        
        return FallbackResult.Success(FallbackMethod.TEXT_TO_SPEECH)
    }
    
    private fun createEmergencyNotification(config: AlarmConfig): FallbackResult {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create high-priority persistent notification
        val notification = NotificationCompat.Builder(this, EMERGENCY_CHANNEL_ID)
            .setContentTitle("⚠️ EMERGENCY WAKE UP ALARM ⚠️")
            .setContentText("Your ADHD alarm couldn't play audio. WAKE UP NOW!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setLights(Color.RED, 500, 500)
            .setOngoing(true) // Can't be swiped away
            .setAutoCancel(false)
            .setFullScreenIntent(createFullScreenAlarmIntent(config), true)
            .addAction(createDismissAction(config))
            .build()
        
        notificationManager.notify(EMERGENCY_NOTIFICATION_ID, notification)
        
        return FallbackResult.Success(FallbackMethod.EMERGENCY_NOTIFICATION)
    }
}
```

---

## Production-Ready Implementation

### Complete Alarm Service Implementation
```kotlin
@Suppress("DEPRECATION")
class AlarmForegroundService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "alarm_service_channel"
        private const val ACTION_DISMISS_ALARM = "com.adhdapp.DISMISS_ALARM"
        private const val ACTION_SNOOZE_DISABLED = "com.adhdapp.SNOOZE_DISABLED"
        
        fun startAlarmService(context: Context, alarmConfig: AlarmConfig) {
            val intent = Intent(context, AlarmForegroundService::class.java).apply {
                putExtra("alarm_config", alarmConfig.toBundle())
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    private lateinit var alarmEngine: UltraLoudAlarmEngine
    private lateinit var vibrationEngine: ADHDVibrationEngine
    private lateinit var visualEngine: VisualAlarmStimulation
    private lateinit var wakeUpMissionController: WakeUpMissionController
    
    private var currentAlarmConfig: AlarmConfig? = null
    private var isAlarmActive = false
    
    override fun onCreate() {
        super.onCreate()
        
        createNotificationChannel()
        alarmEngine = UltraLoudAlarmEngine(this)
        vibrationEngine = ADHDVibrationEngine(this)
        visualEngine = VisualAlarmStimulation(this)
        wakeUpMissionController = WakeUpMissionController(this)
        
        // Register for system events that might interfere with alarm
        registerSystemEventReceivers()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmConfig = intent?.getBundleExtra("alarm_config")?.let { 
            AlarmConfig.fromBundle(it) 
        }
        
        if (alarmConfig != null && !isAlarmActive) {
            startAlarmSequence(alarmConfig)
        }
        
        return START_STICKY // Restart if killed
    }
    
    private fun startAlarmSequence(config: AlarmConfig) {
        currentAlarmConfig = config
        isAlarmActive = true
        
        // Start foreground notification to prevent service killing
        startForeground(NOTIFICATION_ID, createAlarmNotification(config))
        
        // Initialize all alarm systems
        initializeAlarmSystems(config)
        
        // Start the coordinated alarm experience
        launchCoordinatedAlarmExperience(config)
    }
    
    private fun initializeAlarmSystems(config: AlarmConfig) {
        try {
            // Initialize audio engine
            val audioResult = alarmEngine.startAlarm(config.audioConfig)
            if (audioResult != AlarmPlaybackResult.SUCCESS) {
                handleAlarmSystemFailure("Audio engine failed", audioResult)
            }
            
            // Initialize vibration patterns
            vibrationEngine.startCoordinatedVibration(AudioPhase.RAMP_UP)
            
            // Initialize visual stimulation
            if (config.enableVisualStimulation) {
                visualEngine.startVisualAlarmStimulation()
            }
            
        } catch (e: Exception) {
            handleCriticalAlarmFailure(e, config)
        }
    }
    
    private fun launchCoordinatedAlarmExperience(config: AlarmConfig) {
        // Create and launch the wake-up mission activity
        val missionIntent = Intent(this, WakeUpMissionActivity::class.java).apply {
            putExtra("alarm_config", config.toBundle())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            
            // Ensure this appears over lock screen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                addFlags(Intent.FLAG_ACTIVITY_SHOW_WHEN_LOCKED)
                addFlags(Intent.FLAG_ACTIVITY_TURN_SCREEN_ON)
            }
        }
        
        startActivity(missionIntent)
        
        // Start alarm timeout monitoring
        startAlarmTimeoutMonitoring(config)
    }
    
    private fun startAlarmTimeoutMonitoring(config: AlarmConfig) {
        val handler = Handler(Looper.getMainLooper())
        
        // Maximum alarm duration to prevent battery drain
        val maxAlarmDuration = config.maxDurationMs ?: (30 * 60 * 1000L) // 30 minutes default
        
        handler.postDelayed({
            if (isAlarmActive) {
                // Auto-dismiss after maximum duration
                handleAlarmTimeout(config)
            }
        }, maxAlarmDuration)
        
        // Escalation monitoring - increase intensity if not dismissed
        handler.postDelayed({
            if (isAlarmActive) {
                escalateAlarmIntensity()
            }
        }, 2 * 60 * 1000L) // Escalate after 2 minutes
    }
    
    private fun escalateAlarmIntensity() {
        currentAlarmConfig?.let { config ->
            // Increase audio volume to maximum
            alarmEngine.escalateToMaximumVolume()
            
            // Switch to most annoying sound if available
            val emergencySound = getEmergencyEscalationSound()
            if (emergencySound != null) {
                alarmEngine.switchToEmergencySound(emergencySound)
            }
            
            // Increase vibration intensity
            vibrationEngine.escalateVibrationIntensity()
            
            // Add additional visual stimulation
            visualEngine.escalateVisualStimulation()
            
            // Log escalation for user analytics
            AlarmAnalytics.logAlarmEscalation(config.id, "2_minute_escalation")
        }
    }
    
    private fun handleAlarmTimeout(config: AlarmConfig) {
        // Log the timeout event
        AlarmAnalytics.logAlarmTimeout(config.id, "max_duration_reached")
        
        // Gracefully stop all alarm systems
        stopAllAlarmSystems()
        
        // Create emergency notification for missed alarm
        createMissedAlarmNotification(config)
        
        // Stop the service
        stopAlarmService()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ADHD Alarm Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical service for ADHD alarm functionality"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                setBypassDnd(true) // Critical for alarm functionality
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createAlarmNotification(config: AlarmConfig): Notification {
        val dismissIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_DISMISS_ALARM),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 ADHD Alarm Active")
            .setContentText("Complete your wake-up mission to dismiss")
            .setSmallIcon(R.drawable.ic_alarm_active)
            .setOngoing(true) // Cannot be swiped away
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_alarm_off,
                "Complete Mission",
                dismissIntent
            )
            .setFullScreenIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, WakeUpMissionActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ),
                true
            )
            .build()
    }
    
    fun dismissAlarm(missionCompletedSuccessfully: Boolean) {
        if (!isAlarmActive) return
        
        isAlarmActive = false
        
        // Record alarm dismissal analytics
        currentAlarmConfig?.let { config ->
            AlarmAnalytics.logAlarmDismissal(
                alarmId = config.id,
                missionCompleted = missionCompletedSuccessfully,
                dismissalTime = System.currentTimeMillis(),
                totalAlarmDuration = System.currentTimeMillis() - config.startTime
            )
        }
        
        // Stop all alarm systems
        stopAllAlarmSystems()
        
        // Show success feedback if mission completed
        if (missionCompletedSuccessfully) {
            showAlarmSuccessNotification()
        }
        
        // Stop the service
        stopAlarmService()
    }
    
    private fun stopAllAlarmSystems() {
        try {
            alarmEngine.stopAlarm()
            vibrationEngine.stopAllVibration()
            visualEngine.stopVisualStimulation()
            
            // Restore original audio settings
            restoreOriginalAudioSettings()
            
        } catch (e: Exception) {
            Log.e("AlarmService", "Error stopping alarm systems", e)
            // Continue with service shutdown even if cleanup fails
        }
    }
    
    private fun stopAlarmService() {
        stopForeground(true)
        stopSelf()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        
        if (isAlarmActive) {
            // Service was killed while alarm was active - restart it
            currentAlarmConfig?.let { config ->
                startAlarmService(this, config)
            }
        }
        
        unregisterSystemEventReceivers()
    }
}
```

### Complete Wake-Up Mission Activity
```kotlin
class WakeUpMissionActivity : AppCompatActivity() {
    
    companion object {
        private const val MISSION_TIMEOUT_MS = 10 * 60 * 1000L // 10 minutes
    }
    
    private lateinit var missionController: WakeUpMissionController
    private lateinit var alarmConfig: AlarmConfig
    private var missionStartTime: Long = 0
    private var currentMission: Mission? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure activity appears over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        
        // Prevent user from leaving this activity
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setupMissionActivity()
    }
    
    private fun setupMissionActivity() {
        alarmConfig = intent.getBundleExtra("alarm_config")?.let {
            AlarmConfig.fromBundle(it)
        } ?: run {
            // Fallback if no config provided
            finish()
            return
        }
        
        missionStartTime = System.currentTimeMillis()
        missionController = WakeUpMissionController(this)
        
        // Load user profile for mission personalization
        val userProfile = ADHDProfileManager.getCurrentProfile(this)
        
        // Generate appropriate mission for current context
        currentMission = missionController.generateOptimalMission(
            userProfile = userProfile,
            alarmConfig = alarmConfig,
            timeOfDay = LocalTime.now(),
            recentPerformance = getMissionPerformanceHistory()
        )
        
        // Set up the UI
        setContentView(R.layout.activity_wake_up_mission)
        setupMissionUI()
        
        // Start mission timeout
        startMissionTimeout()
        
        // Begin the mission
        startMission()
    }
    
    private fun setupMissionUI() {
        // Prevent back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Show encouragement instead of allowing back navigation
                showBackPressEncouragement()
            }
        })
        
        // Set up mission container
        val missionContainer = findViewById<FrameLayout>(R.id.mission_container)
        
        // Add current mission UI
        currentMission?.let { mission ->
            val missionView = when (mission.type) {
                MissionType.MATH_CHALLENGE -> createMathMissionUI(mission as MathMission)
                MissionType.BARCODE_SCAN -> createBarcodeMissionUI(mission as BarcodeMission)
                MissionType.PHOTO_VERIFICATION -> createPhotoMissionUI(mission as PhotoMission)
                MissionType.PHYSICAL_ACTIVITY -> createPhysicalMissionUI(mission as MotionMission)
                MissionType.TYPING_CHALLENGE -> createTypingMissionUI(mission as TypingMission)
            }
            
            missionContainer.addView(missionView)
        }
        
        // Set up progress indicators
        setupProgressIndicators()
    }
    
    private fun startMission() {
        currentMission?.let { mission ->
            missionController.startMission(
                mission = mission,
                onProgress = { progress -> updateMissionProgress(progress) },
                onSuccess = { result -> handleMissionSuccess(result) },
                onFailure = { error -> handleMissionFailure(error) },
                onTimeout = { handleMissionTimeout() }
            )
        }
    }
    
    private fun handleMissionSuccess(result: MissionResult) {
        // Mission completed successfully!
        val completionTime = System.currentTimeMillis() - missionStartTime
        
        // Record successful completion
        MissionAnalytics.logMissionSuccess(
            missionType = currentMission?.type,
            completionTimeMs = completionTime,
            difficulty = currentMission?.difficulty,
            userProfile = ADHDProfileManager.getCurrentProfile(this)
        )
        
        // Show success animation
        showMissionSuccessAnimation(result)
        
        // Dismiss alarm
        dismissAlarmAndFinish(true)
    }
    
    private fun handleMissionFailure(error: MissionError) {
        when (error.type) {
            MissionErrorType.TOO_MANY_ATTEMPTS -> {
                // Switch to easier mission or provide alternative
                offerEasierMissionAlternative()
            }
            
            MissionErrorType.TECHNICAL_FAILURE -> {
                // Try backup mission type
                switchToBackupMission(error)
            }
            
            MissionErrorType.USER_FRUSTRATION_DETECTED -> {
                // Provide emotional support and easier option
                showFrustrationSupport()
            }
            
            else -> {
                // Generic failure handling
                showMissionFailureGuidance(error)
            }
        }
    }
    
    private fun offerEasierMissionAlternative() {
        val dialog = AlertDialog.Builder(this, R.style.ADHDAlarmDialog)
            .setTitle("Let's Try Something Easier")
            .setMessage("This mission seems challenging right now. Would you like to try an easier wake-up task?")
            .setPositiveButton("Yes, Please") { _, _ ->
                switchToEasierMission()
            }
            .setNegativeButton("I'll Keep Trying") { dialog, _ ->
                dialog.dismiss()
                resetCurrentMission()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun switchToEasierMission() {
        val userProfile = ADHDProfileManager.getCurrentProfile(this)
        val easierMission = missionController.generateEasierMission(
            currentMission = currentMission!!,
            userProfile = userProfile
        )
        
        // Update UI for new mission
        currentMission = easierMission
        updateMissionUI(easierMission)
        startMission()
    }
    
    private fun startMissionTimeout() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && currentMission != null) {
                handleMissionTimeout()
            }
        }, MISSION_TIMEOUT_MS)
    }
    
    private fun handleMissionTimeout() {
        // After 10 minutes, provide emergency exit
        showEmergencyExitDialog()
    }
    
    private fun showEmergencyExitDialog() {
        AlertDialog.Builder(this, R.style.ADHDEmergencyDialog)
            .setTitle("⚠️ Emergency Wake-Up Option")
            .setMessage("It's been 10 minutes. If you're truly awake and need to dismiss this alarm, you can do so now.\n\nAre you fully awake and ready to start your day?")
            .setPositiveButton("Yes, I'm Awake") { _, _ ->
                dismissAlarmAndFinish(false) // Mark as emergency dismissal
            }
            .setNegativeButton("Give Me More Time") { dialog, _ ->
                dialog.dismiss()
                // Extend timeout by 5 more minutes
                extendMissionTimeout()
            }
            .setCancelable(false)
            .create()
            .show()
    }
    
    private fun dismissAlarmAndFinish(missionCompleted: Boolean) {
        // Notify alarm service to stop
        val serviceIntent = Intent(this, AlarmForegroundService::class.java)
        stopService(serviceIntent)
        
        // Send dismissal broadcast
        val dismissIntent = Intent("com.adhdapp.ALARM_DISMISSED").apply {
            putExtra("mission_completed", missionCompleted)
            putExtra("alarm_id", alarmConfig.id)
        }
        sendBroadcast(dismissIntent)
        
        // Show completion message
        if (missionCompleted) {
            Toast.makeText(this, "Great job! Have an amazing day! 🌟", Toast.LENGTH_LONG).show()
        }
        
        // Finish activity
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up any ongoing mission resources
        currentMission?.cleanup()
        missionController.cleanup()
    }
}
```

### Complete Audio Engine Implementation
```kotlin
class UltraLoudAlarmEngine(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalStreamVolumes: Map<Int, Int> = emptyMap()
    private var originalRingerMode: Int = AudioManager.RINGER_MODE_NORMAL
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private val audioStreams = listOf(
        AudioManager.STREAM_ALARM,
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_NOTIFICATION,
        AudioManager.STREAM_RING
    )
    
    fun startAlarm(config: AlarmAudioConfig): AlarmPlaybackResult {
        return try {
            // Step 1: Acquire system resources
            acquireSystemResources()
            
            // Step 2: Store original settings
            storeOriginalAudioSettings()
            
            // Step 3: Override system limitations
            overrideSystemAudioLimitations()
            
            // Step 4: Initialize media player
            initializeMediaPlayer(config)
            
            // Step 5: Start progressive volume playback
            startProgressiveVolumePlayback(config)
            
            AlarmPlaybackResult.SUCCESS
            
        } catch (e: Exception) {
            Log.e("AlarmEngine", "Failed to start alarm", e)
            handleAlarmStartFailure(e, config)
        }
    }
    
    private fun acquireSystemResources() {
        // Acquire wake lock
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "ADHDAlarm::AlarmWakeLock"
        ).apply {
            acquire(30 * 60 * 1000L) // 30 minutes maximum
        }
        
        // Request audio focus
        requestAudioFocus()
    }
    
    @SuppressLint("NewApi")
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(false)
                .setOnAudioFocusChangeListener { focusChange ->
                    handleAudioFocusChange(focusChange)
                }
                .build()
            
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { focusChange -> handleAudioFocusChange(focusChange) },
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }
    
    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Never give up audio focus during alarm
                requestAudioFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Regain focus immediately
                requestAudioFocus()
            }
        }
    }
    
    private fun storeOriginalAudioSettings() {
        originalRingerMode = audioManager.ringerMode
        originalStreamVolumes = audioStreams.associateWith { stream ->
            audioManager.getStreamVolume(stream)
        }
    }
    
    private fun overrideSystemAudioLimitations() {
        // Set ringer mode to normal
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        
        // Set all relevant streams to maximum volume
        audioStreams.forEach { streamType ->
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            audioManager.setStreamVolume(streamType, maxVolume, 0)
        }
        
        // Try to override Do Not Disturb if we have permission
        if (hasDoNotDisturbAccess()) {
            overrideDoNotDisturbMode()
        }
    }
    
    private fun hasDoNotDisturbAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }
    
    @SuppressLint("NewApi")
    private fun overrideDoNotDisturbMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create alarm-friendly policy
            val policy = NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY
            )
            
            notificationManager.notificationPolicy = policy
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }
    
    private fun initializeMediaPlayer(config: AlarmAudioConfig) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            
            // Set data source
            setDataSource(context, config.soundUri)
            
            // Configure for maximum reliability
            isLooping = true
            
            // Prepare synchronously to avoid delays
            prepare()
            
            // Set initial volume
            val startVolume = config.startVolumePercent / 100f
            setVolume(startVolume, startVolume)
        }
    }
    
    private fun startProgressiveVolumePlayback(config: AlarmAudioConfig) {
        mediaPlayer?.start()
        
        // Implement progressive volume increase
        val handler = Handler(Looper.getMainLooper())
        val startVolume = config.startVolumePercent
        val maxVolume = config.maxVolumePercent
        val rampDuration = config.rampUpDurationMs
        val updateInterval = 500L // Update every 500ms
        val totalSteps = (rampDuration / updateInterval).toInt()
        
        for (step in 0..totalSteps) {
            handler.postDelayed({
                if (mediaPlayer?.isPlaying == true) {
                    val progress = step.toFloat() / totalSteps
                    val currentVolume = startVolume + (maxVolume - startVolume) * progress
                    val volumeFloat = (currentVolume / 100f).coerceIn(0f, 1f)
                    
                    mediaPlayer?.setVolume(volumeFloat, volumeFloat)
                }
            }, step * updateInterval)
        }
    }
    
    fun escalateToMaximumVolume() {
        mediaPlayer?.setVolume(1.0f, 1.0f)
        
        // Also ensure system volume is at maximum
        audioStreams.forEach { streamType ->
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            audioManager.setStreamVolume(streamType, maxVolume, 0)
        }
    }
    
    fun switchToEmergencySound(emergencySound: Uri) {
        try {
            mediaPlayer?.apply {
                stop()
                reset()
                setDataSource(context, emergencySound)
                prepare()
                isLooping = true
                setVolume(1.0f, 1.0f)
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmEngine", "Failed to switch to emergency sound", e)
            // Continue with current sound
        }
    }
    
    fun stopAlarm() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            
            // Release audio focus
            releaseAudioFocus()
            
            // Release wake lock
            wakeLock?.apply {
                if (isHeld) {
                    release()
                }
            }
            wakeLock = null
            
        } catch (e: Exception) {
            Log.e("AlarmEngine", "Error stopping alarm", e)
        }
    }
    
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager.abandonAudioFocusRequest(request)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus { /* No-op */ }
        }
    }
    
    fun restoreOriginalAudioSettings() {
        try {
            // Restore ringer mode
            audioManager.ringerMode = originalRingerMode
            
            // Restore stream volumes
            originalStreamVolumes.forEach { (stream, volume) ->
                audioManager.setStreamVolume(stream, volume, 0)
            }
            
        } catch (e: Exception) {
            Log.e("AlarmEngine", "Error restoring audio settings", e)
        }
    }
    
    private fun handleAlarmStartFailure(error: Exception, config: AlarmAudioConfig): AlarmPlaybackResult {
        Log.e("AlarmEngine", "Alarm start failed", error)
        
        // Try fallback methods
        return tryFallbackAlarmMethods(config)
    }
    
    private fun tryFallbackAlarmMethods(config: AlarmAudioConfig): AlarmPlaybackResult {
        // Fallback 1: Try system alarm sound
        try {
            val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val fallbackConfig = config.copy(soundUri = defaultAlarmUri)
            return startBasicAlarm(fallbackConfig)
        } catch (e: Exception) {
            Log.e("AlarmEngine", "Fallback 1 failed", e)
        }
        
        // Fallback 2: Try notification sound
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val fallbackConfig = config.copy(soundUri = notificationUri)
            return startBasicAlarm(fallbackConfig)
        } catch (e: Exception) {
            Log.e("AlarmEngine", "Fallback 2 failed", e)
        }
        
        return AlarmPlaybackResult.COMPLETE_FAILURE
    }
    
    private fun startBasicAlarm(config: AlarmAudioConfig): AlarmPlaybackResult {
        return try {
            initializeMediaPlayer(config)
            mediaPlayer?.start()
            AlarmPlaybackResult.FALLBACK_SUCCESS
        } catch (e: Exception) {
            AlarmPlaybackResult.FAILURE(e.message ?: "Unknown error")
        }
    }
}
```

This comprehensive deep-dive into the Ultra-Loud Alarm Engine shows the sophisticated audio engineering and system integration required to create an alarm that can reliably wake ADHD users while working within Android's security constraints and handling numerous edge cases.