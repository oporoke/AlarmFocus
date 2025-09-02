# ADHD Focus Alarm App - Technical Specification Document
**Document Version:** 1.0  
**Date:** September 2025  
**Document Owner:** Lead Android Developer  
**Classification:** Internal Technical

---

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Design](#architecture-design)
3. [Data Model](#data-model)
4. [API Specifications](#api-specifications)
5. [Security Architecture](#security-architecture)
6. [Performance Requirements](#performance-requirements)
7. [Third-Party Integrations](#third-party-integrations)

---

## System Overview

### Technology Stack
- **Language:** Kotlin 1.9.0
- **Minimum SDK:** API 21 (Android 5.0)
- **Target SDK:** API 34 (Android 14)
- **Architecture Pattern:** MVVM with Repository Pattern
- **Database:** Room (SQLite) with encryption
- **Dependency Injection:** Hilt
- **Async Operations:** Kotlin Coroutines + Flow
- **Testing:** JUnit4, Espresso, Robolectric

### System Components
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Presentation  │    │    Domain       │    │      Data       │
│   Layer (UI)    │◄──►│   Layer (BL)    │◄──►│  Layer (DB/API) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ViewModels    │    │  Use Cases      │    │  Repositories   │
│   Fragments     │    │  Entities       │    │  Data Sources   │
│   Activities    │    │  Interfaces     │    │  Database       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## Architecture Design

### Module Structure
```
app/
├── presentation/
│   ├── ui/
│   │   ├── alarm/          # Alarm management screens
│   │   ├── missions/       # Wake-up mission screens
│   │   ├── focus/          # Focus mode screens
│   │   └── settings/       # Settings and preferences
│   ├── viewmodels/         # ViewModels for each feature
│   └── adapters/           # RecyclerView adapters
├── domain/
│   ├── entities/           # Business entities
│   ├── usecases/          # Business logic use cases
│   └── repositories/       # Repository interfaces
├── data/
│   ├── repositories/       # Repository implementations
│   ├── datasources/       # Local and remote data sources
│   ├── database/          # Room database and DAOs
│   └── models/            # Data transfer objects
└── di/                    # Dependency injection modules
```

### Core Services
```kotlin
// Alarm Service Architecture
┌─────────────────────────────────────────┐
│           AlarmManagerService            │
├─────────────────────────────────────────┤
│  - Schedules alarms with system         │
│  - Handles device reboot recovery       │
│  - Manages alarm persistence            │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│          AlarmTriggerService            │
├─────────────────────────────────────────┤
│  - Plays alarm sounds                   │
│  - Manages volume override              │
│  - Controls vibration patterns          │
│  - Launches mission activities          │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│          MissionEngineService           │
├─────────────────────────────────────────┤
│  - Validates mission completion         │
│  - Tracks mission performance           │
│  - Manages difficulty scaling           │
└─────────────────────────────────────────┘
```

---

## Data Model

### Database Schema
```sql
-- Alarms table
CREATE TABLE alarms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    time TEXT NOT NULL,           -- ISO 8601 time format
    label TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT 1,
    repeat_pattern TEXT,          -- JSON: {"days": [1,2,3,4,5]}
    sound_uri TEXT,
    volume_level INTEGER DEFAULT 100,
    vibration_pattern TEXT,       -- JSON array of vibration timings
    mission_type TEXT NOT NULL,   -- MATH, BARCODE, PHOTO, PHYSICAL, TYPING
    mission_config TEXT,          -- JSON configuration for mission
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

-- Mission Results table
CREATE TABLE mission_results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    alarm_id INTEGER NOT NULL,
    mission_type TEXT NOT NULL,
    start_time INTEGER NOT NULL,
    completion_time INTEGER,
    success BOOLEAN NOT NULL DEFAULT 0,
    attempts INTEGER DEFAULT 1,
    difficulty_level INTEGER DEFAULT 1,
    performance_data TEXT,        -- JSON: time taken, accuracy, etc.
    FOREIGN KEY (alarm_id) REFERENCES alarms(id)
);

-- Focus Sessions table
CREATE TABLE focus_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    start_time INTEGER NOT NULL,
    end_time INTEGER NOT NULL,
    blocked_apps TEXT NOT NULL,   -- JSON array of package names
    intensity_level INTEGER DEFAULT 1, -- 1=gentle, 2=moderate, 3=strict
    break_duration INTEGER DEFAULT 0,  -- minutes
    is_active BOOLEAN DEFAULT 0,
    created_at INTEGER NOT NULL
);

-- Sleep Tracking table
CREATE TABLE sleep_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL,           -- ISO 8601 date
    bedtime INTEGER,              -- Unix timestamp
    sleep_time INTEGER,           -- Unix timestamp (when actually asleep)
    wake_time INTEGER,            -- Unix timestamp
    quality_score REAL,           -- 0.0 to 10.0
    movement_data TEXT,           -- JSON accelerometer data summary
    environment_data TEXT        -- JSON: room temp, noise level, etc.
);

-- App Usage Tracking table
CREATE TABLE app_usage (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    package_name TEXT NOT NULL,
    app_name TEXT NOT NULL,
    date TEXT NOT NULL,
    usage_time INTEGER NOT NULL, -- milliseconds
    launch_count INTEGER NOT NULL,
    last_used INTEGER            -- Unix timestamp
);

-- Custom Sounds table
CREATE TABLE custom_sounds (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    file_path TEXT NOT NULL,
    file_size INTEGER NOT NULL,
    duration INTEGER,            -- milliseconds
    is_encrypted BOOLEAN DEFAULT 1,
    checksum TEXT,              -- MD5 hash for integrity
    created_at INTEGER NOT NULL
);
```

### Entity Models
```kotlin
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: String,                    // "HH:mm" format
    val label: String?,
    val isEnabled: Boolean = true,
    val repeatPattern: String?,          // JSON serialized RepeatPattern
    val soundUri: String?,
    val volumeLevel: Int = 100,
    val vibrationPattern: String?,       // JSON serialized pattern
    val missionType: MissionType,
    val missionConfig: String?,          // JSON serialized config
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class MissionType {
    MATH, BARCODE, PHOTO, PHYSICAL, TYPING, NONE
}

data class RepeatPattern(
    val days: Set<DayOfWeek>,
    val isRecurring: Boolean = true
)

data class MissionConfig(
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val timeoutSeconds: Int = 120,
    val maxAttempts: Int = 3,
    val escalateOnFailure: Boolean = true,
    val specificConfig: Map<String, Any> = emptyMap()
)

enum class DifficultyLevel { EASY, MEDIUM, HARD }
```

---

## API Specifications

### Internal API Interfaces

#### AlarmRepository Interface
```kotlin
interface AlarmRepository {
    suspend fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(id: Long)
    suspend fun getActiveAlarms(): List<Alarm>
    suspend fun getNextScheduledAlarm(): Alarm?
}
```

#### MissionEngine Interface
```kotlin
interface MissionEngine {
    suspend fun startMission(
        missionType: MissionType, 
        config: MissionConfig
    ): MissionSession
    
    suspend fun validateMissionCompletion(
        sessionId: String, 
        userInput: Any
    ): MissionResult
    
    suspend fun escalateDifficulty(sessionId: String): MissionConfig
    
    suspend fun getMissionPerformanceAnalytics(
        alarmId: Long, 
        dateRange: DateRange
    ): MissionAnalytics
}
```

#### FocusManager Interface
```kotlin
interface FocusManager {
    suspend fun startFocusSession(config: FocusSessionConfig): String
    suspend fun endFocusSession(sessionId: String)
    suspend fun isAppBlocked(packageName: String): Boolean
    suspend fun getBlockedApps(): List<String>
    suspend fun addBlockedApp(packageName: String)
    suspend fun removeBlockedApp(packageName: String)
    suspend fun getFocusSessionHistory(): List<FocusSession>
}
```

### External Service Integrations

#### Firebase Analytics Events
```kotlin
// Custom events for user behavior tracking
object AnalyticsEvents {
    const val ALARM_CREATED = "alarm_created"
    const val ALARM_TRIGGERED = "alarm_triggered"
    const val MISSION_STARTED = "mission_started"
    const val MISSION_COMPLETED = "mission_completed"
    const val MISSION_FAILED = "mission_failed"
    const val FOCUS_SESSION_STARTED = "focus_session_started"
    const val APP_BLOCKED = "app_blocked"
    const val SLEEP_TRACKING_ENABLED = "sleep_tracking_enabled"
}

// Event parameters
data class AlarmCreatedEvent(
    val missionType: String,
    val timeOfDay: String,        // "morning", "afternoon", "evening"
    val isRecurring: Boolean
)
```

---

## Security Architecture

### Data Encryption Strategy
```kotlin
// AES-256 encryption for sensitive data
class DataEncryption {
    private val keyAlias = "ADHDAlarmSecretKey"
    private val transformation = "AES/GCM/NoPadding"
    
    fun encryptSensitiveData(data: ByteArray): EncryptedData {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .build()
        
        keyGenerator.init(keyGenParameterSpec)
        val secretKey = keyGenerator.generateKey()
        
        // Encryption implementation...
    }
}
```

### Permission Management
```xml
<!-- Required Permissions -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

<!-- Dangerous Permissions (Runtime Request Required) -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
<uses-permission android:name="android.permission.DEVICE_ADMIN" />
```

### Privacy Protection Measures
- All personal data encrypted at rest
- No data transmitted to external servers without explicit consent
- User data can be completely deleted on request
- Crash reports anonymized and aggregated only
- Location data never collected or stored

---

## Performance Requirements

### Response Time Requirements
| Operation | Target Time | Maximum Time |
|-----------|-------------|--------------|
| App Launch | <2 seconds | <3 seconds |
| Alarm Creation | <500ms | <1 second |
| Mission Loading | <1 second | <2 seconds |
| Settings Update | <200ms | <500ms |
| Database Query | <100ms | <300ms |

### Memory Management
```kotlin
// Memory optimization strategies
class MemoryManager {
    // Lazy initialization for heavy objects
    private val audioManager by lazy { 
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager 
    }
    
    // Proper cleanup in lifecycle methods
    override fun onDestroy() {
        mediaPlayer?.release()
        vibrator?.cancel()
        alarmService?.unbind()
        super.onDestroy()
    }
    
    // Use object pools for frequently created objects
    private val missionResultPool = object : ObjectPool<MissionResult> {
        override fun create(): MissionResult = MissionResult()
        override fun reset(obj: MissionResult) = obj.reset()
    }
}
```

### Battery Optimization
- Use JobScheduler for background tasks
- Implement Doze mode whitelist requests
- Optimize alarm service to minimize CPU usage
- Use efficient data structures and algorithms
- Implement proper wake lock management

---

## Third-Party Integrations

### Firebase Services
```kotlin
// Firebase configuration
class FirebaseConfig {
    // Analytics for user behavior insights
    private val analytics = FirebaseAnalytics.getInstance(context)
    
    // Crashlytics for crash reporting
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    // Remote Config for feature flags
    private val remoteConfig = FirebaseRemoteConfig.getInstance().apply {
        setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 hour
                .build()
        )
    }
}
```

### Camera and Barcode Scanning
```kotlin
// Camera2 API integration for missions
class CameraManager {
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private val imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)
    
    // ML Kit for barcode scanning
    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODE_128)
            .build()
    )
}
```

### Audio Processing
```kotlin
// Audio management for custom sounds and ultra-loud alarms
class AudioProcessor {
    private val mediaPlayer = MediaPlayer()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    fun playUltraLoudAlarm(soundUri: Uri, volumeLevel: Int) {
        // Override system volume settings
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            volumeLevel,
            AudioManager.FLAG_SHOW_UI
        )
        
        mediaPlayer.apply {
            setAudioStreamType(AudioManager.STREAM_ALARM)
            setDataSource(context, soundUri)
            prepare()
            start()
        }
    }
}
```

---

## Error Handling & Logging

### Error Classification
```kotlin
sealed class ADHDAppError : Exception() {
    data class AlarmSchedulingError(override val message: String) : ADHDAppError()
    data class MissionValidationError(override val message: String) : ADHDAppError()
    data class DatabaseError(override val message: String) : ADHDAppError()
    data class PermissionDeniedError(val permission: String) : ADHDAppError()
    data class NetworkError(override val message: String) : ADHDAppError()
}

class ErrorHandler {
    fun handleError(error: ADHDAppError, context: String) {
        when (error) {
            is ADHDAppError.AlarmSchedulingError -> {
                // Log critical alarm issues
                FirebaseCrashlytics.getInstance().recordException(error)
                // Show user-friendly message
                showErrorDialog("Alarm scheduling failed. Please try again.")
            }
            // Handle other error types...
        }
    }
}
```

### Logging Strategy
- Use structured logging with consistent format
- Log levels: VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT
- Never log sensitive user data (times, personal info)
- Implement log rotation to prevent storage issues
- Send critical errors to Firebase Crashlytics

---

## Testing Strategy

### Unit Testing
```kotlin
@RunWith(JUnit4::class)
class AlarmManagerTest {
    
    @MockK
    private lateinit var alarmRepository: AlarmRepository
    
    @MockK
    private lateinit var systemAlarmManager: AlarmManager
    
    private lateinit var alarmManager: AlarmManagerImpl
    
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        alarmManager = AlarmManagerImpl(alarmRepository, systemAlarmManager)
    }
    
    @Test
    fun `scheduleAlarm should set system alarm correctly`() = runTest {
        // Given
        val alarm = createTestAlarm()
        every { systemAlarmManager.setExact(any(), any(), any()) } just Runs
        
        // When
        alarmManager.scheduleAlarm(alarm)
        
        // Then
        verify { systemAlarmManager.setExact(AlarmManager.RTC_WAKEUP, any(), any()) }
    }
}
```

### Integration Testing
```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class AlarmIntegrationTest {
    
    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java)
    
    @Test
    fun alarmCreationFlow() {
        // Navigate to alarm creation
        onView(withId(R.id.fab_add_alarm)).perform(click())
        
        // Set alarm time
        onView(withId(R.id.time_picker)).perform(setTime(9, 30))
        
        // Select mission type
        onView(withId(R.id.spinner_mission_type)).perform(click())
        onView(withText("Math Challenge")).perform(click())
        
        // Save alarm
        onView(withId(R.id.btn_save_alarm)).perform(click())
        
        // Verify alarm appears in list
        onView(withId(R.id.recycler_alarms))
            .check(matches(hasDescendant(withText("9:30 AM"))))
    }
}
```

---

**Document Approval:**
- [ ] Lead Developer: _________________ Date: _______
- [ ] System Architect: ______________ Date: _______
- [ ] QA Engineer: __________________ Date: _______

**Next Review Date:** October 15, 2025