# ADHD Focus Alarm - Project Integration & Continuation Guide

## Overview
This document serves as a comprehensive integration guide for continuing the ADHD Focus Alarm project. It provides all necessary interfaces, contracts, and architectural decisions that subsequent deliverables must follow to ensure seamless integration.

---

## 1. Project Architecture & Standards

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (NO XML layouts)
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room (SQLite)
- **Dependency Injection**: Manual DI (can upgrade to Hilt later)
- **Concurrency**: Coroutines + Flow
- **Testing**: JUnit4 + Espresso + Compose Testing

### Package Structure (MANDATORY)
```
com.adhdfocusalarm/
├── data/
│   ├── database/
│   │   ├── entities/
│   │   ├── dao/
│   │   └── AppDatabase.kt
│   ├── repository/
│   └── model/
├── domain/
│   ├── model/
│   ├── usecase/
│   └── repository/
├── presentation/
│   ├── ui/
│   │   ├── alarm/
│   │   ├── mission/
│   │   ├── focus/
│   │   └── settings/
│   ├── viewmodel/
│   ├── theme/
│   └── MainActivity.kt
├── service/
│   ├── AlarmService.kt (✓ Implemented)
│   ├── AlarmReceiver.kt (✓ Implemented)
│   ├── BootReceiver.kt (✓ Implemented)
│   └── AudioManagerHelper.kt (✓ Implemented)
├── util/
│   ├── PermissionManager.kt (✓ Implemented)
│   ├── AlarmValidator.kt (✓ Implemented)
│   └── Constants.kt
└── di/
    └── AppModule.kt
```

### Naming Conventions
- **Files**: PascalCase (`AlarmViewModel.kt`)
- **Classes/Interfaces**: PascalCase (`AlarmRepository`)
- **Functions**: camelCase (`createAlarm()`)
- **Variables**: camelCase (`alarmTime`)
- **Constants**: SCREAMING_SNAKE_CASE (`MAX_ALARM_VOLUME`)
- **Composables**: PascalCase (`AlarmListScreen()`)

---

## 2. Core Data Models & Contracts

### Database Schema (Room Entities)

```kotlin
// PRIMARY ENTITY - All future deliverables reference this
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean = true,
    val repeatDays: String, // JSON: ["MON","TUE","WED"]
    val soundUri: String? = null, // null = default sound
    val missionType: String = "NONE", // NONE, MATH, BARCODE, PHOTO, ACTIVITY, QUOTE
    val missionConfig: String = "{}", // JSON configuration per mission
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggered: Long? = null
)

// MISSION CONFIGURATION MODELS
data class MissionConfig(
    val type: MissionType,
    val difficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
    val data: String = "{}" // Type-specific JSON data
)

enum class MissionType {
    NONE, MATH, BARCODE, PHOTO, ACTIVITY, QUOTE
}

// APP USAGE TRACKING (Deliverable 11)
@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val id: Long = 0,
    val packageName: String,
    val appName: String,
    val categoryId: Int,
    val usageTimeMs: Long,
    val openCount: Int,
    val date: String, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)

// FOCUS SESSIONS (Deliverable 13)
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val startTime: String, // HH:mm
    val durationMinutes: Int,
    val repeatDays: String, // JSON array
    val blockedApps: String, // JSON array of package names
    val intensityLevel: String, // GENTLE, MODERATE, STRICT
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

// SLEEP TRACKING (Deliverable 14)
@Entity(tableName = "sleep_data")
data class SleepDataEntity(
    @PrimaryKey val id: Long = 0,
    val bedtime: Long, // timestamp
    val wakeTime: Long, // timestamp
    val qualityScore: Float, // 0.0 - 1.0
    val movementData: String, // JSON array of accelerometer data
    val alarmSuccessRate: Float, // correlation with alarm dismissal
    val date: String, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)
```

### Repository Interfaces (MUST IMPLEMENT)

```kotlin
interface AlarmRepository {
    suspend fun getAllAlarms(): Flow<List<AlarmEntity>>
    suspend fun getAlarmById(id: Long): AlarmEntity?
    suspend fun insertAlarm(alarm: AlarmEntity): Long
    suspend fun updateAlarm(alarm: AlarmEntity)
    suspend fun deleteAlarm(id: Long)
    suspend fun getActiveAlarms(): List<AlarmEntity>
    suspend fun updateLastTriggered(id: Long, timestamp: Long)
}

interface MissionRepository {
    suspend fun getMissionConfig(alarmId: Long): MissionConfig?
    suspend fun saveMissionConfig(alarmId: Long, config: MissionConfig)
    suspend fun validateMission(type: MissionType, data: String): Boolean
}

interface FocusRepository {
    suspend fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>
    suspend fun getActiveFocusSession(): FocusSessionEntity?
    suspend fun insertFocusSession(session: FocusSessionEntity): Long
    suspend fun updateFocusSession(session: FocusSessionEntity)
    suspend fun deleteFocusSession(id: Long)
}
```

---

## 3. Service Integration Contracts

### AlarmService Integration Points

```kotlin
// EXISTING SERVICE (Deliverable 2) - DO NOT MODIFY CORE LOGIC
class AlarmService : Service() {
    companion object {
        const val ACTION_START_ALARM = "START_ALARM"
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val ACTION_MISSION_COMPLETED = "MISSION_COMPLETED" // NEW for D6+
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val EXTRA_MISSION_TYPE = "mission_type" // NEW for D6+
        const val EXTRA_MISSION_DATA = "mission_data" // NEW for D6+
    }
    
    // INTEGRATION POINT: Mission system will send this intent
    // Implementation in Deliverable 6 must handle mission completion
    private fun handleMissionCompleted() {
        // Stop alarm, log success, trigger post-alarm actions
        // This method will be implemented in D6
    }
}

// INTEGRATION CONTRACT for Mission Activities
interface MissionActivity {
    fun startMission(alarmId: Long, config: MissionConfig)
    fun onMissionSuccess()
    fun onMissionFailed()
    fun onMissionTimeout() // 2-minute timeout -> restart alarm
}
```

### App Blocking Service Contract (Deliverable 12)

```kotlin
// SERVICE CONTRACT - Must be implemented in Deliverable 12
interface AppBlockingService {
    fun startBlocking(blockedApps: List<String>, durationMs: Long)
    fun stopBlocking()
    fun isAppBlocked(packageName: String): Boolean
    fun addToBlockList(packageName: String)
    fun removeFromBlockList(packageName: String)
    fun getBlockingStatus(): BlockingStatus
}

data class BlockingStatus(
    val isActive: Boolean,
    val remainingTimeMs: Long,
    val blockedApps: List<String>,
    val reason: String // "ALARM_TRIGGERED", "FOCUS_MODE", "CUSTOM"
)
```

---

## 4. UI/UX Standards & Compose Guidelines

### Design System Constants

```kotlin
// File: util/Constants.kt - MUST BE CREATED in next deliverable
object ADHDDesignConstants {
    // ACCESSIBILITY REQUIREMENTS
    const val MIN_TOUCH_TARGET_DP = 44
    const val MIN_FONT_SIZE_SP = 16
    const val CONTRAST_RATIO_MIN = 4.5f
    
    // ADHD-FRIENDLY COLORS (High contrast, calming)
    val PRIMARY_COLOR = Color(0xFF1976D2) // Blue - focus
    val SECONDARY_COLOR = Color(0xFF388E3C) // Green - success
    val ERROR_COLOR = Color(0xFFD32F2F) // Red - urgent
    val WARNING_COLOR = Color(0xFFF57C00) // Orange - caution
    val BACKGROUND_COLOR = Color(0xFFFAFAFA) // Light gray
    val SURFACE_COLOR = Color(0xFFFFFFFF) // White
    
    // ANIMATION DURATIONS (Reduced for ADHD)
    const val QUICK_ANIMATION_MS = 150
    const val NORMAL_ANIMATION_MS = 300
    const val SLOW_ANIMATION_MS = 500
    
    // SPACING SYSTEM
    val SPACING_XS = 4.dp
    val SPACING_S = 8.dp
    val SPACING_M = 16.dp
    val SPACING_L = 24.dp
    val SPACING_XL = 32.dp
}

// MANDATORY: All Composables must follow this pattern
@Composable
fun ADHDScreen(
    modifier: Modifier = Modifier,
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ADHDDesignConstants.BACKGROUND_COLOR)
            .padding(ADHDDesignConstants.SPACING_M)
    ) {
        // Standard header for all screens
        if (onNavigationClick != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigationClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = ADHDDesignConstants.SPACING_L)
            )
        }
        
        content()
    }
}
```

### Compose Component Standards

```kotlin
// MANDATORY BUTTON STYLE for all interactive elements
@Composable
fun ADHDButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    type: ButtonType = ButtonType.PRIMARY
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(56.dp) // Large touch target
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = when(type) {
                ButtonType.PRIMARY -> ADHDDesignConstants.PRIMARY_COLOR
                ButtonType.DANGER -> ADHDDesignConstants.ERROR_COLOR
                ButtonType.SUCCESS -> ADHDDesignConstants.SECONDARY_COLOR
            }
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp, // Large, readable text
            fontWeight = FontWeight.Medium
        )
    }
}

enum class ButtonType { PRIMARY, DANGER, SUCCESS }
```

---

## 5. Navigation & State Management

### Navigation Structure

```kotlin
// MANDATORY: All screens must follow this route naming
object ADHDNavigation {
    const val ALARM_LIST = "alarm_list"
    const val ALARM_CREATE = "alarm_create"
    const val ALARM_EDIT = "alarm_edit/{alarmId}"
    const val MISSION_SELECT = "mission_select/{alarmId}"
    const val MISSION_MATH = "mission_math/{alarmId}"
    const val MISSION_BARCODE = "mission_barcode/{alarmId}"
    const val MISSION_PHOTO = "mission_photo/{alarmId}"
    const val MISSION_ACTIVITY = "mission_activity/{alarmId}"
    const val MISSION_QUOTE = "mission_quote/{alarmId}"
    const val FOCUS_MODE = "focus_mode"
    const val FOCUS_CREATE = "focus_create"
    const val APP_BLOCKING = "app_blocking"
    const val SLEEP_TRACKING = "sleep_tracking"
    const val SETTINGS = "settings"
}

// STATE MANAGEMENT: All ViewModels must extend this
abstract class ADHDViewModel : ViewModel() {
    protected val _uiState = MutableStateFlow(getInitialState())
    val uiState = _uiState.asStateFlow()
    
    protected val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()
    
    abstract fun getInitialState(): Any
    
    protected fun updateState(update: (Any) -> Any) {
        _uiState.value = update(_uiState.value)
    }
    
    protected fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
}

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    data class NavigateBack(val result: String? = null) : UiEvent()
    object ShowLoading : UiEvent()
    object HideLoading : UiEvent()
}
```

---

## 6. Integration Points Between Deliverables

### Deliverable Dependencies & Integration Contracts

```kotlin
// D1 -> D2: Database integration (COMPLETED)
interface D1ToD2Contract {
    // AlarmEntity model is available
    // Room database setup is complete
    // Basic UI framework is ready
}

// D2 -> D3: Alarm scheduling integration
interface D2ToD3Contract {
    // AlarmService.ACTION_START_ALARM available
    // AlarmReceiver handles broadcast triggers
    // Audio system fully functional
    
    fun scheduleAlarm(alarm: AlarmEntity)
    fun cancelAlarm(alarmId: Long)
    fun updateAlarm(alarm: AlarmEntity)
}

// D3 -> D6: Mission system integration
interface D3ToD6Contract {
    // Alarm scheduling complete
    // Need: Mission selection in alarm creation
    // Need: Mission trigger when alarm fires
    
    fun triggerMission(alarmId: Long, missionType: MissionType, config: String)
    fun onMissionCompleted(alarmId: Long, success: Boolean)
}

// D6 -> D7,D8,D9,D10: Individual mission implementations
interface MissionContract {
    fun initializeMission(config: MissionConfig)
    fun startMission(): Boolean
    fun validateCompletion(): Boolean
    fun cleanup()
}

// D2 -> D12: Post-alarm app blocking
interface D2ToD12Contract {
    // After alarm dismissed, trigger 1-hour social media block
    fun triggerPostAlarmBlocking()
    // Integration point: AlarmService.handleMissionCompleted()
}

// D11 -> D12,D13: App usage data for blocking
interface D11ToD12Contract {
    fun getAppUsageData(days: Int): List<AppUsageEntity>
    fun getCategoryUsage(category: String): Long
    fun getMostUsedApps(limit: Int): List<String>
}
```

---

## 7. Testing Standards & Requirements

### Testing Contracts (MANDATORY for each deliverable)

```kotlin
// UNIT TEST: Every Repository, UseCase, ViewModel
class ExampleRepositoryTest {
    @Test
    fun `should create alarm successfully`() = runTest {
        // Arrange, Act, Assert pattern
        // Use fake data, mock dependencies
        // Verify state changes and side effects
    }
}

// INTEGRATION TEST: Database operations
@RunWith(AndroidJUnit4::class)
class ExampleDatabaseTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: AppDatabase
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }
}

// UI TEST: Compose screens
class ExampleScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `should display alarm list correctly`() {
        // Test user interactions, state changes, navigation
    }
}
```

### Performance Requirements (MUST MEET)

```kotlin
object PerformanceRequirements {
    const val ALARM_TRIGGER_MAX_DELAY_MS = 1000L
    const val MISSION_LOAD_MAX_TIME_MS = 2000L
    const val APP_BLOCK_RESPONSE_MAX_MS = 500L
    const val DATABASE_QUERY_MAX_MS = 100L
    const val UI_FRAME_TIME_MAX_MS = 16L // 60 FPS
    
    // Battery usage limits
    const val MAX_DAILY_BATTERY_USAGE_PERCENT = 5
    const val MAX_BACKGROUND_CPU_USAGE_PERCENT = 2
}
```

---

## 8. Error Handling & Logging

### Standardized Error Handling

```kotlin
// MANDATORY: All operations must use this pattern
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception, val message: String) : Result<T>()
    data class Loading<T>(val message: String = "Loading...") : Result<T>()
}

// LOGGING STANDARD
object ADHDLogger {
    private const val TAG = "ADHDFocusAlarm"
    
    fun logAlarmTrigger(alarmId: Long, success: Boolean) {
        Log.i("$TAG-Alarm", "Alarm $alarmId triggered: $success")
    }
    
    fun logMissionAttempt(alarmId: Long, type: MissionType, success: Boolean) {
        Log.i("$TAG-Mission", "Mission $type for alarm $alarmId: $success")
    }
    
    fun logError(component: String, error: Exception) {
        Log.e("$TAG-Error", "Error in $component", error)
    }
    
    fun logPerformance(operation: String, durationMs: Long) {
        Log.d("$TAG-Performance", "$operation took ${durationMs}ms")
    }
}
```

---

## 9. Security & Privacy Requirements

### Data Protection Standards

```kotlin
object SecurityRequirements {
    // ENCRYPTION: All sensitive data must be encrypted
    const val ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"
    const val KEY_SIZE_BITS = 256
    
    // PERMISSIONS: Required permissions list
    val CRITICAL_PERMISSIONS = listOf(
        android.Manifest.permission.SCHEDULE_EXACT_ALARM,
        android.Manifest.permission.WAKE_LOCK,
        android.Manifest.permission.VIBRATE,
        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
        android.Manifest.permission.ACCESS_NOTIFICATION_POLICY
    )
    
    // PRIVACY: No data leaves device without explicit consent
    fun encryptSensitiveData(data: String): String {
        // Implementation required in each deliverable handling sensitive data
        // Custom sounds, photos, personal data must be encrypted
    }
}
```

---

## 10. Deliverable Handoff Checklist

### When completing each deliverable, ensure:

**Code Quality:**
- [ ] All interfaces implemented according to contracts
- [ ] MVVM pattern followed consistently
- [ ] Repository pattern used for data access
- [ ] Jetpack Compose for all UI (NO XML)
- [ ] Coroutines used for async operations
- [ ] Error handling implemented with Result<T>

**Integration:**
- [ ] Database schema matches defined entities
- [ ] Service integration points implemented
- [ ] Navigation routes follow naming convention
- [ ] State management follows ADHDViewModel pattern

**Testing:**
- [ ] Unit tests for business logic (80%+ coverage)
- [ ] Integration tests for database operations
- [ ] UI tests for user flows
- [ ] Performance requirements verified

**Accessibility:**
- [ ] Minimum 44dp touch targets
- [ ] 16sp+ font sizes
- [ ] High contrast colors used
- [ ] Screen reader compatibility tested

**Documentation:**
- [ ] Public APIs documented
- [ ] Integration points clearly defined
- [ ] Any architectural decisions recorded
- [ ] Known issues or limitations noted

---

## 11. Quick Reference for Next Implementer

### Files to Create in Next Deliverable (D3):
1. `data/database/entities/` - Complete entity definitions
2. `data/database/dao/AlarmDao.kt` - Database access object
3. `data/database/AppDatabase.kt` - Room database setup
4. `data/repository/AlarmRepositoryImpl.kt` - Repository implementation
5. `domain/usecase/CreateAlarmUseCase.kt` - Business logic
6. `presentation/ui/alarm/AlarmListScreen.kt` - Main UI
7. `presentation/viewmodel/AlarmViewModel.kt` - State management
8. `util/AlarmScheduler.kt` - AlarmManager integration
9. `util/Constants.kt` - App constants

### Key Integration Points to Implement:
1. **Database -> Service**: Pass alarm data to AlarmService
2. **UI -> Repository**: CRUD operations for alarms
3. **Scheduler -> AlarmManager**: System alarm scheduling
4. **Service -> Notification**: User feedback on alarm state

This guide should enable any developer to continue the project with full context and integration compatibility.

You're absolutely right! I need to create a comprehensive integration guide that will serve as a bridge between deliverables, allowing any developer (human or AI) to continue the project without needing the full source code of previous deliverables. This will be crucial for maintaining consistency and enabling seamless integration.## Summary

This **Project Integration & Continuation Guide** serves as a comprehensive bridge document that enables seamless continuation of the ADHD Focus Alarm project. Here's what it provides:

### Key Benefits:

1. **Complete Architecture Blueprint**: Defines the exact package structure, naming conventions, and technical standards that must be followed

2. **Data Model Contracts**: Provides the complete database schema and entity definitions that all future deliverables must use

3. **Service Integration Points**: Clearly defines how each component integates with the existing AlarmService and what new services need to be created

4. **UI/UX Standards**: Establishes ADHD-friendly design patterns and Compose component standards for consistency

5. **Inter-Deliverable Contracts**: Specifies exactly how each deliverable should integrate with others, preventing architectural mismatches

6. **Quality Assurance Framework**: Defines testing standards, performance requirements, and security protocols

### How to Use This Guide:

**For the Next Implementer:**
- Follow the exact package structure and naming conventions
- Implement the required interfaces and contracts
- Use the provided data models and database schema
- Follow the UI/UX patterns for ADHD accessibility
- Meet all performance and testing requirements

**For Project Continuity:**
- Each deliverable should reference this guide for integration points
- Update the guide if architectural decisions change
- Use the checklist to ensure completeness before handoff
- Maintain the contracts for future deliverables

**For Quality Control:**
- Verify implementations match the defined contracts
- Ensure all accessibility requirements are met
- Check that performance benchmarks are achieved
- Validate integration points work as specified

This approach allows the project to be built incrementally by different developers while maintaining consistency, quality, and the specific needs of ADHD users. The guide acts as both a technical specification and a quality gate for each deliverable.


### Summary
This **Project Integration & Continuation Guide** serves as a comprehensive bridge document that enables seamless continuation of the ADHD Focus Alarm project. Here's what it provides:

### Key Benefits:

1. **Complete Architecture Blueprint**: Defines the exact package structure, naming conventions, and technical standards that must be followed

2. **Data Model Contracts**: Provides the complete database schema and entity definitions that all future deliverables must use

3. **Service Integration Points**: Clearly defines how each component integates with the existing AlarmService and what new services need to be created

4. **UI/UX Standards**: Establishes ADHD-friendly design patterns and Compose component standards for consistency

5. **Inter-Deliverable Contracts**: Specifies exactly how each deliverable should integrate with others, preventing architectural mismatches

6. **Quality Assurance Framework**: Defines testing standards, performance requirements, and security protocols

### How to Use This Guide:

**For the Next Implementer:**
- Follow the exact package structure and naming conventions
- Implement the required interfaces and contracts
- Use the provided data models and database schema
- Follow the UI/UX patterns for ADHD accessibility
- Meet all performance and testing requirements

**For Project Continuity:**
- Each deliverable should reference this guide for integration points
- Update the guide if architectural decisions change
- Use the checklist to ensure completeness before handoff
- Maintain the contracts for future deliverables

**For Quality Control:**
- Verify implementations match the defined contracts
- Ensure all accessibility requirements are met
- Check that performance benchmarks are achieved
- Validate integration points work as specified

This approach allows the project to be built incrementally by different developers while maintaining consistency, quality, and the specific needs of ADHD users. The guide acts as both a technical specification and a quality gate for each deliverable.