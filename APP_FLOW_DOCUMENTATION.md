# AlarmFocus Application Flow Documentation

This document provides a comprehensive overview of how the AlarmFocus Android application flows from startup to alarm dismissal, including all navigation paths, data flows, and system interactions.

---

## Table of Contents
1. [Application Startup Flow](#1-application-startup-flow)
2. [Navigation Flow](#2-navigation-flow)
3. [Alarm Creation Flow](#3-alarm-creation-flow)
4. [Alarm Trigger Flow](#4-alarm-trigger-flow)
5. [Mission System Flow](#5-mission-system-flow)
6. [Focus Mode Flow](#6-focus-mode-flow)
7. [Database Flow](#7-database-flow)
8. [Utility Usage Flow](#8-utility-usage-flow)

---

## 1. Application Startup Flow

### 1.1 Application Initialization

```
Android System
    ↓
AlarmFocusApplication.onCreate()
    ├─ Log: "AlarmFocus application started"
    └─ Provides utility methods: logException(), setUserId(), logEvent()
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/AlarmFocusApplication.kt` (Lines 12-15)

### 1.2 MainActivity Launch

```
Android System
    ↓
MainActivity.onCreate() (Line 49)
    ├─ enableEdgeToEdge()
    ├─ Initialize PermissionManager (Line 56)
    ├─ Initialize AppModule (Line 57)
    │   └─ AppModule constructor
    │       ├─ Creates applicationScope (Line 24)
    │       ├─ Lazy initializes database (Line 27-29)
    │       ├─ Lazy initializes AlarmRepository (Line 32-34)
    │       ├─ Lazy initializes AlarmScheduler (Line 37-39)
    │       ├─ Lazy initializes AlarmValidator (Line 41-43)
    │       └─ Lazy initializes Use Cases (Lines 46-60)
    ├─ checkAndRequestPermissions() (Line 60)
    │   └─ PermissionManager.requestAllPermissions()
    ├─ setContent { AlarmFocusTheme { ... } } (Line 67-73)
    └─ checkAndRestoreActiveAlarm() (Line 78)
        ├─ Read SharedPreferences("alarm_service_state") (Line 83)
        ├─ Check activeAlarmId and missionActive (Lines 84-85)
        └─ If mission active:
            └─ Launch MissionActivity (Lines 98-103)
```

**Files**:
- `/app/src/main/java/com/omondit/alarmfocus/presentation/MainActivity.kt`
- `/app/src/main/java/com/omondit/alarmfocus/di/AppModule.kt`

### 1.3 Database Setup

```
AppDatabase.getDatabase(context) (Line 40)
    ├─ Check INSTANCE (synchronized) (Line 41)
    ├─ If null, build database:
    │   ├─ Room.databaseBuilder() (Lines 42-46)
    │   │   ├─ Name: "adhd_alarm_database"
    │   │   ├─ Version: 4
    │   │   └─ Entities: AlarmEntity, AppUsageEntity, BlockedAppEntity,
    │   │              FocusSessionEntity, SleepSessionEntity
    │   ├─ fallbackToDestructiveMigration(false) (Line 47)
    │   └─ build()
    └─ Return INSTANCE (Line 50)
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/data/database/AppDatabase.kt` (Lines 40-52)

### 1.4 Compose UI Setup

```
ADHDAlarmApp(context) (Line 128)
    ├─ Create NavController (Line 129)
    ├─ Initialize dependencies (Lines 133-140):
    │   ├─ AppDatabase.getDatabase(context)
    │   ├─ AlarmRepositoryImpl(database.alarmDao())
    │   ├─ AlarmScheduler(context)
    │   ├─ AlarmValidator(context)
    │   └─ Use Cases
    ├─ Create AlarmViewModel (Lines 142-145)
    ├─ Setup Scaffold with ADHDBottomNavigation (Lines 147-161)
    └─ Setup NavHost (Lines 163-179)
        ├─ Start destination: "alarms"
        └─ Routes: "alarms", "missions", "focus", "settings"
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/MainActivity.kt` (Lines 128-180)

---

## 2. Navigation Flow

### 2.1 Bottom Navigation Structure

```
ADHDBottomNavigation
    ├─ "alarms" → AlarmsScreen
    ├─ "missions" → MissionsScreen
    ├─ "focus" → FocusScreen
    └─ "settings" → SettingsScreen
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/navigation/ADHDBottomNavigation.kt`

### 2.2 AlarmsScreen Navigation

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/AlarmsScreen.kt`

| User Action | Navigation | Line |
|------------|------------|------|
| Click "Create Alarm" button | Opens QuickCreateAlarmDialog | 122 |
| Click FAB (Add) | Navigate to create (currently no-op) | 201 |
| Click alarm card | Navigate to alarm details (TODO) | 347 |
| Click "Skip next" in menu | Calls viewModel.skipNextAlarm() | 450 |
| Click "Duplicate" in menu | Calls viewModel.duplicateAlarm() | 461 |
| Click "Delete" in menu | Calls viewModel.deleteAlarm() | 474 |
| Toggle alarm switch | Calls viewModel.toggleAlarm() | 430 |

### 2.3 MissionsScreen Navigation

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/MissionsScreen.kt`

| User Action | Navigation | Line |
|------------|------------|------|
| Click "Sleep Analytics" card | Shows SleepAnalyticsScreen | 174 |
| Click "App Usage Monitoring" card | Shows AppUsageScreen | 184 |
| Click back arrow in Sleep Analytics | Returns to MissionsScreen | 67 |
| Click back arrow in App Usage | Returns to MissionsScreen | 87 |

**Navigation Pattern**:
```
MissionsScreen
    ├─ When showSleepAnalytics = true (Line 52)
    │   └─ Display: SleepAnalyticsScreen with back button
    ├─ When showAppUsage = true (Line 78)
    │   └─ Display: AppUsageScreen with back button
    └─ Else: Display mission cards list
```

### 2.4 FocusScreen Navigation

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/FocusScreen.kt`

| User Action | Action Taken | Line |
|------------|--------------|------|
| Click "15 minutes" focus session | viewModel.startQuickSession(15) | 156 |
| Click "30 minutes" focus session | viewModel.startQuickSession(30) | 164 |
| Click "1 hour" focus session | viewModel.startQuickSession(60) | 172 |
| Click "2 hours" focus session | viewModel.startQuickSession(120) | 180 |
| Click "Stop Focus Session" | viewModel.stopFocusSession() | 129 |

### 2.5 SettingsScreen Navigation

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/SettingsScreen.kt`

| User Action | Navigation | Line |
|------------|------------|------|
| Click "System Diagnostics" | Shows DiagnosticsScreen | 113 |
| Click "Manage Barcodes" | Shows BarcodeManagementScreen | 122 |
| Click "Manage Photos" | Shows PhotoManagementScreen | 131 |
| Click "Sound Settings" | Shows SoundPickerDialog | 149 |
| Click "Alarm Protection" | Shows DeviceAdminOnboardingScreen | 158 |

**Navigation Pattern**:
```
SettingsScreen
    ├─ If showDiagnostics = true (Line 53)
    │   └─ Display: DiagnosticsScreen
    ├─ If showBarcodeManagement = true (Line 62)
    │   └─ Display: BarcodeManagementScreen
    ├─ If showPhotoManagement = true (Line 66)
    │   └─ Display: PhotoManagementScreen
    ├─ If showDeviceAdminOnboarding = true (Line 44)
    │   └─ Display: DeviceAdminOnboardingScreen
    └─ Else: Display settings list
```

### 2.6 Sub-Screen Navigations

#### DiagnosticsScreen
**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/DiagnosticsScreen.kt`

- Refresh button → Reloads system health (Line 75)
- View Logs button → Shows/hides logs section (Line 92-100)
- Action button on diagnostic result → Opens system settings intent (Line 143)

#### BarcodeManagementScreen
**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/BarcodeManagementScreen.kt`

- Back button → onNavigateBack() (Line 64)
- Add button → Shows AddBarcodeDialog (Line 76)
- Scan barcode → Opens BarcodeScannerForRegistration (Line 161)
- Manual entry → Registers barcode directly (Line 163)
- Edit barcode → Opens edit dialog (Line 357)
- Delete barcode → Shows confirmation dialog (Line 368)

#### DeviceAdminOnboardingScreen
**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/DeviceAdminOnboardingScreen.kt`

- Enable Protection button → onEnableDeviceAdmin() (Line 87)
- Skip for Now button → onSkip() (Line 107)

---

## 3. Alarm Creation Flow

### 3.1 User Initiates Creation

```
User clicks "Create Alarm" in AlarmsScreen (Line 122)
    ↓
QuickCreateAlarmDialog displayed (Line 651)
    ├─ User selects hour/minute (Lines 705-757)
    ├─ User enters label (Lines 769-775)
    ├─ User selects repeat schedule (Lines 787-813)
    │   ├─ Once
    │   ├─ Daily
    │   ├─ Weekdays
    │   └─ Weekends
    └─ User clicks "CREATE ALARM" (Line 819)
        ↓
        onCreate(hour, minute, label, schedule) callback (Line 222)
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/AlarmsScreen.kt`

### 3.2 ViewModel Processes Creation

```
AlarmViewModel.createAlarm() (Line 83)
    ├─ Set isLoading = true (Line 92)
    ├─ Create AlarmTime(hour, minute) (Line 95)
    ├─ Call CreateAlarmUseCase (Lines 96-102)
    │   ├─ Pass: time, label, schedule, soundUri, vibrationEnabled
    │   └─ Returns: Result<Long> (alarm ID)
    ├─ On Success:
    │   ├─ Set isLoading = false (Line 106)
    │   ├─ Emit ShowSnackbar("Alarm created successfully") (Line 107)
    │   └─ Emit AlarmCreated(alarmId) event (Line 108)
    └─ On Failure:
        ├─ Emit ShowSnackbar(error.message) (Line 112)
        └─ Set error state (Lines 113-118)
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/viewmodel/AlarmViewModel.kt`

### 3.3 UseCase Validates and Creates

```
CreateAlarmUseCase.invoke() (Line 17)
    ├─ Validate time (Lines 27-30)
    │   └─ AlarmValidator.validateAlarmTime(hour, minute)
    ├─ Validate label (Lines 32-35)
    │   └─ AlarmValidator.validateAlarmLabel(label)
    ├─ Validate system state (Lines 38-41)
    │   └─ AlarmValidator.validateSystemState()
    ├─ Create alarm in repository (Line 44)
    │   └─ repository.createRepeatingAlarm(time, label, schedule)
    ├─ Retrieve created alarm (Lines 47-48)
    │   └─ repository.getAlarmById(alarmId)
    └─ Schedule with AlarmScheduler (Line 51)
        └─ See Section 3.4
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/domain/usecase/CreateAlarmUseCase.kt`

### 3.4 Repository Saves to Database

```
AlarmRepository.createRepeatingAlarm()
    ├─ Create AlarmEntity from parameters
    ├─ Calculate next trigger time
    ├─ Insert into Room database via AlarmDao
    │   └─ INSERT into alarm_table
    └─ Return alarm ID (Long)
```

**Files**:
- `/app/src/main/java/com/omondit/alarmfocus/data/repository/AlarmRepositoryImpl.kt`
- `/app/src/main/java/com/omondit/alarmfocus/data/database/dao/AlarmDao.kt`

### 3.5 Scheduler Registers with AlarmManager

```
AlarmScheduler.scheduleAlarm(alarm) (Line 34)
    ├─ Check if enabled (Lines 36-38)
    ├─ Check if skipNextAlarm (Lines 40-42)
    ├─ Validate alarm time (Lines 44-47)
    ├─ Calculate next trigger time (Lines 49-51)
    │   └─ calculateNextTriggerTime(alarmTime, repeatSchedule)
    ├─ Create alarm intent (Line 53)
    │   └─ createAlarmIntent(alarm) → Intent for AlarmReceiver
    ├─ Create PendingIntent (Line 54)
    │   └─ createPendingIntent(alarmId, intent)
    ├─ Schedule with AlarmManager (Line 57)
    │   └─ scheduleWithAlarmManager(nextTriggerTime, pendingIntent)
    │       ├─ API 23+: setExactAndAllowWhileIdle() (Lines 161-165)
    │       └─ API <23: setExact() (Lines 168-172)
    └─ Return ScheduleResult.Success(nextTriggerTime) (Line 65)
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/utils/AlarmScheduler.kt`

### 3.6 Complete Creation Flow Diagram

```
User
 ↓ (creates alarm)
QuickCreateAlarmDialog
 ↓ (submits)
AlarmViewModel.createAlarm()
 ↓
CreateAlarmUseCase
 ├─ AlarmValidator.validateAlarmTime()
 ├─ AlarmValidator.validateAlarmLabel()
 ├─ AlarmValidator.validateSystemState()
 ├─ AlarmRepository.createRepeatingAlarm()
 │   └─ AlarmDao.insert() → Room Database
 └─ AlarmScheduler.scheduleAlarm()
     └─ AlarmManager.setExactAndAllowWhileIdle()
         └─ System Alarm Registered ✓
```

---

## 4. Alarm Trigger Flow

### 4.1 AlarmManager Fires

```
System AlarmManager (at trigger time)
    ↓
Broadcasts Intent to AlarmReceiver
    ├─ EXTRA_ALARM_ID
    ├─ EXTRA_SOUND_URI
    ├─ EXTRA_ALARM_LABEL
    ├─ EXTRA_VIBRATION_ENABLED
    └─ EXTRA_VOLUME
```

### 4.2 AlarmReceiver Handles Broadcast

```
AlarmReceiver.onReceive() (Line 29)
    ├─ Extract extras (Lines 30-35)
    │   ├─ alarmId
    │   ├─ soundUri
    │   ├─ label
    │   ├─ vibrationEnabled
    │   └─ volume
    ├─ Validate alarmId (Lines 39-42)
    ├─ goAsync() to extend timeout (Line 45)
    ├─ Create AlarmService intent (Lines 47-54)
    │   ├─ action = ACTION_START_ALARM
    │   └─ Put all extras
    ├─ Start AlarmService (Lines 56-61)
    │   ├─ API 26+: startForegroundService()
    │   └─ API <26: startService()
    └─ Finish pending result (Line 71)
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/services/AlarmReceiver.kt`

### 4.3 AlarmService Starts

```
AlarmService.onCreate() (Line 70)
    ├─ Initialize database and repository (Lines 72-73)
    ├─ Initialize MissionManager (Line 74)
    ├─ createNotificationChannel() (Line 75)
    └─ initializeSystemServices() (Line 76)
        ├─ Get AudioManager (Line 148)
        ├─ Get Vibrator (Lines 149-154)
        └─ Acquire WakeLock (Lines 155-159)

AlarmService.onStartCommand() (Line 79)
    ├─ Check for saved state in SharedPreferences (Lines 82-93)
    ├─ Match action: ACTION_START_ALARM (Line 96)
    ├─ Extract alarmId and soundUri (Lines 97-98)
    ├─ Start foreground service (Line 101)
    │   └─ startForeground(NOTIFICATION_ID, createAlarmNotification())
    ├─ Mark alarm as triggered in DB (Lines 102-104)
    └─ Call startAlarm(alarmId, soundUri) (Line 105)
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/services/AlarmService.kt`

### 4.4 Alarm Playback Begins

```
AlarmService.startAlarm() (Line 162)
    ├─ Acquire wake lock (30 min timeout) (Line 163)
    ├─ Set currentAlarmId and isMissionActive=false (Lines 164-165)
    ├─ Get alarm from repository (Line 167)
    ├─ Save state to SharedPreferences (Lines 170-176)
    │   ├─ active_alarm_id
    │   ├─ active_alarm_sound
    │   ├─ mission_config
    │   └─ alarm_start_time
    ├─ saveOriginalAudioSettings() (Line 178)
    │   └─ Save current volume and ringer mode (Lines 207-209)
    ├─ overrideAudioSettings() (Line 179)
    │   ├─ Set RINGER_MODE_NORMAL (override DND) (Line 216)
    │   └─ Set max volume (Lines 221-222)
    ├─ initializeMediaPlayer(soundUri) (Line 180)
    │   ├─ Create MediaPlayer with USAGE_ALARM (Lines 229-234)
    │   ├─ Set data source (Lines 236-242)
    │   ├─ Set looping=true (Line 244)
    │   ├─ Set initial volume 70% (Line 245)
    │   └─ prepareAsync() (Line 251)
    ├─ startVibration() (Line 181)
    │   └─ Vibrate with pattern (Lines 285-287)
    ├─ startVolumeRamping() (Line 182)
    │   └─ Ramp from 70% to 100% over 10s (Lines 299-312)
    └─ scheduleMissionStart(alarmId) (Line 183)
        └─ Delay 3 seconds, then start mission
```

### 4.5 Mission Scheduling

```
AlarmService.scheduleMissionStart() (Line 186)
    ├─ Launch coroutine (Line 187)
    ├─ Delay MISSION_START_DELAY (3000ms) (Line 188)
    ├─ Check if still active (Line 189)
    ├─ Call MissionManager.startMission(alarmId) (Line 190)
    ├─ On success:
    │   ├─ Set isMissionActive = true (Line 192)
    │   ├─ Save to SharedPreferences (Lines 193-194)
    │   └─ Update notification (Lines 196-198)
    └─ Log mission started (Line 199)
```

### 4.6 MissionManager Launches Activity

```
MissionManager.startMission() (Line 32)
    ├─ Get alarm from repository (Line 34)
    ├─ Parse MissionConfig from alarm.missionConfig (Line 40)
    ├─ If NONE type, use default EASY math (Lines 42-52)
    ├─ Create mission via MissionFactory (Line 54)
    ├─ Create MissionSession (Line 55)
    ├─ Save to SharedPreferences (Line 57)
    └─ Launch MissionActivity (Lines 59-68)
        ├─ FLAG_ACTIVITY_NEW_TASK
        ├─ FLAG_ACTIVITY_CLEAR_TOP
        ├─ FLAG_ACTIVITY_NO_USER_ACTION
        ├─ FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        └─ Extras: alarm_id, mission_config
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/utils/MissionManager.kt`

### 4.7 Complete Trigger Flow Diagram

```
AlarmManager (system)
 ↓ (fires at trigger time)
AlarmReceiver.onReceive()
 ↓ (starts service)
AlarmService.onStartCommand()
 ├─ startForeground() (notification)
 ├─ startAlarm()
 │   ├─ Acquire WakeLock
 │   ├─ Override DND & Max Volume
 │   ├─ Play Sound (MediaPlayer)
 │   ├─ Start Vibration
 │   └─ Ramp Volume (70% → 100%)
 └─ scheduleMissionStart() (after 3s delay)
     └─ MissionManager.startMission()
         └─ Launch MissionActivity
```

---

## 5. Mission System Flow

### 5.1 MissionActivity Initialization

```
MissionActivity.onCreate() (Line 30)
    ├─ Set window flags (Lines 33-44)
    │   ├─ setShowWhenLocked(true)
    │   ├─ setTurnScreenOn(true)
    │   └─ FLAG_KEEP_SCREEN_ON
    ├─ Initialize dependencies (Lines 46-48)
    │   ├─ AppDatabase.getDatabase()
    │   ├─ AlarmRepositoryImpl
    │   └─ MissionManager
    ├─ Get intent extras (Lines 50-51)
    │   ├─ alarm_id
    │   └─ mission_config (JSON)
    ├─ Validate alarmId (Lines 53-56)
    ├─ Parse MissionConfig (Line 58)
    └─ setContent { IntegratedMissionScreen } (Lines 60-89)
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/MissionActivity.kt`

### 5.2 Mission Type Routing

```
IntegratedMissionScreen (Line 25)
    ├─ When missionConfig.type == MATH (Line 35)
    │   └─ MissionScreen (math challenges)
    ├─ When missionConfig.type == BARCODE (Line 45)
    │   └─ BarcodeMissionScreen
    ├─ When missionConfig.type == PHOTO (Line 55)
    │   └─ PhotoMissionScreen
    ├─ When missionConfig.type == ACTIVITY (Line 65)
    │   └─ ActivityMissionScreen
    ├─ When missionConfig.type == TYPING (Line 75)
    │   └─ TypingMissionScreen
    └─ When missionConfig.type == NONE (Line 85)
        └─ Auto-complete immediately
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/IntegratedMissionScreen.kt`

### 5.3 Mission Execution Flow (Example: Barcode)

```
BarcodeMissionScreen (Line 308)
    ├─ Initialize BarcodeManager (Line 319)
    ├─ Get registered barcodes (Line 320)
    ├─ Create BarcodeMission (Line 321)
    ├─ Create MissionSession (Line 323)
    ├─ Generate challenge (Line 330)
    │   └─ If no barcodes: show NoBarcodeRegisteredScreen (Line 339)
    ├─ Display BarcodeScannerScreen (Line 350)
    └─ On barcode scanned:
        ├─ Submit answer to session (Line 354)
        ├─ If correct (Line 356):
        │   ├─ Update barcode usage (Lines 358-362)
        │   ├─ Get completion result (Line 364)
        │   └─ Call onMissionCompleted(result) (Line 365)
        └─ If incorrect:
            ├─ Check max attempts (Line 367)
            │   ├─ If reached: onMissionFailed() (Line 368)
            │   └─ Else: generate new challenge (Line 371)
```

### 5.4 Mission Completion Path

```
onMissionCompleted(result) (Line 69 in MissionActivity)
    ↓
lifecycleScope.launch
    ├─ missionManager.completeMission(alarmId, result) (Line 71)
    │   └─ See Section 5.5
    ├─ Create ACTION_MISSION_COMPLETED intent (Lines 72-75)
    ├─ startService(completeIntent) (Line 76)
    └─ finish() activity (Line 77)
```

### 5.5 MissionManager Processes Completion

```
MissionManager.completeMission() (Line 80)
    ├─ If success (Line 82):
    │   ├─ Mark alarm as dismissed in DB (Line 84)
    │   ├─ Log mission result (Line 87)
    │   ├─ Create ACTION_STOP_ALARM intent (Lines 90-93)
    │   └─ Start AlarmService with stop action (Line 94)
    ├─ Clear active mission (Line 103)
    │   └─ Remove from SharedPreferences (Line 104)
    └─ Return success boolean (Line 106)
```

### 5.6 AlarmService Stops

```
AlarmService.onStartCommand() with ACTION_MISSION_COMPLETED (Line 124)
    ├─ Validate alarmId matches (Line 126)
    ├─ Set isMissionActive = false (Line 128)
    ├─ Update SharedPreferences (Line 129)
    ├─ Mark alarm dismissed in DB (Line 131)
    ├─ Enable post-alarm app blocking (Lines 134-136)
    └─ stopAlarm() (Line 138)

AlarmService.stopAlarm() (Line 315)
    ├─ Reset state variables (Lines 316-317)
    ├─ Cancel jobs (Lines 318-319)
    ├─ Stop and release MediaPlayer (Lines 321-329)
    ├─ Cancel vibration (Lines 331-335)
    ├─ Restore original audio settings (Line 337)
    │   └─ Restore volume and ringer mode (Lines 354-356)
    ├─ Release wake lock (Lines 339-343)
    ├─ Clear SharedPreferences (Lines 345-346)
    ├─ stopForeground(STOP_FOREGROUND_REMOVE) (Line 348)
    └─ stopSelf() (Line 349)
```

### 5.7 Complete Mission Flow Diagram

```
MissionActivity launches
 ↓
IntegratedMissionScreen (routes by type)
 ├─ MATH → MissionScreen
 ├─ BARCODE → BarcodeMissionScreen
 ├─ PHOTO → PhotoMissionScreen
 ├─ ACTIVITY → ActivityMissionScreen
 └─ TYPING → TypingMissionScreen
     ↓
Mission.generateChallenge()
 ↓
User interacts with challenge
 ↓
MissionSession.submitAnswer()
 ├─ Mission.validateAnswer()
 └─ If correct:
     ├─ MissionSession.getCompletionResult()
     └─ onMissionCompleted(result)
         ├─ MissionManager.completeMission()
         ├─ AlarmRepository.markAlarmDismissed()
         ├─ Send ACTION_MISSION_COMPLETED to AlarmService
         └─ AlarmService.stopAlarm()
             ├─ Stop sound
             ├─ Stop vibration
             ├─ Restore audio
             ├─ Release wake lock
             └─ Stop service
```

---

## 6. Focus Mode Flow

### 6.1 Starting Focus Session

```
FocusScreen (Line 40)
    ├─ Initialize FocusViewModel (Line 42)
    │   └─ With FocusModeManager(context)
    ├─ Collect UI state (Line 43)
    └─ Display FocusScreenContent

User clicks focus duration (e.g., "30 minutes") (Line 164)
    ↓
onStartQuickSession(30) (Line 48)
    ↓
FocusViewModel.startQuickSession(duration) (in FocusViewModel)
    ├─ Create FocusSession
    ├─ Start AccessibilityService for app blocking
    ├─ Update UI state (isSessionActive = true)
    └─ Store session in database
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/FocusScreen.kt`

### 6.2 FocusModeManager

```
FocusModeManager (likely in utils/FocusModeManager.kt)
    ├─ startFocusSession(duration, blockedApps)
    │   ├─ Create FocusSessionEntity
    │   ├─ Insert into database via FocusSessionDao
    │   ├─ Start AccessibilityService
    │   └─ Configure app blocking rules
    ├─ isSessionActive()
    │   └─ Check SharedPreferences or DB
    └─ stopFocusSession()
        ├─ Update session end time in DB
        ├─ Stop AccessibilityService
        └─ Clear blocking rules
```

### 6.3 App Blocking via AccessibilityService

```
AppBlockingAccessibilityService (not shown in files, but inferred)
    ├─ onAccessibilityEvent()
    ├─ Detect app launches
    ├─ If app is blocked:
    │   ├─ Show blocking overlay
    │   └─ Redirect to focus screen
    └─ If session expired:
        └─ Disable blocking
```

### 6.4 Stopping Focus Session

```
User clicks "Stop Focus Session" (Line 129)
    ↓
viewModel.stopFocusSession() (Line 49)
    ↓
FocusModeManager.stopFocusSession()
    ├─ Calculate session duration
    ├─ Update FocusSessionEntity in DB
    │   ├─ endTime
    │   ├─ duration
    │   └─ appsBlocked count
    ├─ Stop AccessibilityService
    └─ Update UI state (isSessionActive = false)
```

### 6.5 Focus Flow Diagram

```
User selects duration
 ↓
FocusViewModel.startQuickSession()
 ↓
FocusModeManager.startFocusSession()
 ├─ Insert FocusSessionEntity → DB
 ├─ Start AccessibilityService
 └─ Configure app blocking
     ↓
Session Active (blocking apps)
 ↓
User stops session OR timer expires
 ↓
FocusModeManager.stopFocusSession()
 ├─ Update FocusSessionEntity → DB
 └─ Stop AccessibilityService
     ↓
Session Complete
```

---

## 7. Database Flow

### 7.1 Database Architecture

```
AppDatabase (Room)
    ├─ AlarmEntity → AlarmDao
    ├─ AppUsageEntity → AppUsageDao
    ├─ BlockedAppEntity → BlockedAppDao
    ├─ FocusSessionEntity → FocusSessionDao
    └─ SleepSessionEntity → SleepSessionDao
```

**File**: `/app/src/main/java/com/omondit/alarmfocus/data/database/AppDatabase.kt` (Lines 18-34)

### 7.2 Alarm Data Flow

```
UI Layer (AlarmsScreen)
    ↓ (user action)
AlarmViewModel
    ├─ allAlarms: StateFlow<List<AlarmEntity>> (Line 44)
    │   └─ Collected from repository.getAllAlarms()
    ├─ upcomingAlarms: StateFlow (Line 52)
    │   └─ From GetUpcomingAlarmsUseCase()
    └─ Methods:
        ├─ createAlarm() → CreateAlarmUseCase
        ├─ toggleAlarm() → ToggleAlarmUseCase
        ├─ deleteAlarm() → DeleteAlarmUseCase
        └─ skipNextAlarm() → repository.setSkipNextAlarm()

Use Cases
    ↓
AlarmRepository (interface)
    ↓
AlarmRepositoryImpl
    ├─ Uses AlarmDao for DB operations
    └─ Methods:
        ├─ getAllAlarms(): Flow<List<AlarmEntity>>
        ├─ getAlarmById(id): AlarmEntity?
        ├─ createRepeatingAlarm(): Long
        ├─ updateAlarm(alarm): Unit
        ├─ deleteAlarmById(id): Unit
        ├─ markAlarmTriggered(id): Unit
        └─ markAlarmDismissed(id): Unit

AlarmDao (Room DAO)
    ├─ @Query: getAllAlarms()
    ├─ @Query: getAlarmById()
    ├─ @Insert: insert(AlarmEntity)
    ├─ @Update: update(AlarmEntity)
    ├─ @Delete: delete(AlarmEntity)
    └─ SQL operations on alarm_table

SQLite Database
    └─ Table: alarm_table
```

### 7.3 Focus Session Data Flow

```
FocusScreen
    ↓
FocusViewModel
    ↓
FocusModeManager
    ↓
FocusSessionDao
    ├─ @Insert: insert(FocusSessionEntity)
    ├─ @Update: update(FocusSessionEntity)
    ├─ @Query: getActiveSessions()
    └─ @Query: getSessionsForDateRange()
    ↓
SQLite Database
    └─ Table: focus_sessions
```

### 7.4 Sleep Data Flow

```
SleepAnalyticsScreen
    ↓
SleepViewModel
    ├─ recentSessions: StateFlow (Line 36)
    ├─ averageQuality: StateFlow (Line 37)
    └─ weeklyData: StateFlow (Line 38)
    ↓
SleepSessionDao
    ├─ @Query: getRecentSessions(limit)
    ├─ @Query: getSessionsForDateRange()
    └─ @Query: getAverageQuality()
    ↓
SQLite Database
    └─ Table: sleep_sessions
```

### 7.5 App Usage Data Flow

```
AppUsageScreen
    ↓
AppUsageMonitor
    ├─ collectTodayUsage() (Line 46)
    ├─ getUsageForDateRange() (Line 49)
    └─ getCategoryBreakdown() (Line 54)
    ↓
AppUsageDao
    ├─ @Insert: insert(AppUsageEntity)
    ├─ @Query: getUsageForDate()
    ├─ @Query: getUsageForDateRange()
    └─ @Query: getCategoryTotals()
    ↓
SQLite Database
    └─ Table: app_usage
```

### 7.6 Complete Database Flow Diagram

```
UI Layer
 ↓ (displays data)
ViewModel
 ↓ (business logic)
Use Case (optional)
 ↓ (validation, coordination)
Repository
 ↓ (abstraction)
DAO (Data Access Object)
 ↓ (SQL operations)
Room Database
 ↓ (SQLite)
Persistent Storage
```

**Pattern**: All database operations follow this flow, with StateFlow/Flow for reactive updates.

---

## 8. Utility Usage Flow

### 8.1 AlarmScheduler Usage

**File**: `/app/src/main/java/com/omondit/alarmfocus/utils/AlarmScheduler.kt`

**Called by**:
- `CreateAlarmUseCase.invoke()` (Line 51) - Schedule new alarm
- `ToggleAlarmUseCase.invoke()` - Enable/disable alarm
- `DeleteAlarmUseCase.invoke()` - Cancel alarm
- `BootReceiver` - Reschedule alarms after reboot

**Methods**:
- `scheduleAlarm(alarm)` → Schedules single alarm with AlarmManager
- `cancelAlarm(alarmId)` → Cancels scheduled alarm
- `rescheduleAlarms(alarms)` → Batch reschedule
- `canScheduleExactAlarms()` → Check permission

### 8.2 MissionManager Usage

**File**: `/app/src/main/java/com/omondit/alarmfocus/utils/MissionManager.kt`

**Called by**:
- `AlarmService.scheduleMissionStart()` (Line 190) - Start mission
- `MissionActivity.onMissionCompleted()` (Line 71) - Complete mission
- `MissionActivity.onMissionFailed()` (Line 82) - Fail mission

**Methods**:
- `startMission(alarmId)` → Launch MissionActivity
- `completeMission(alarmId, result)` → Process completion
- `failMission(alarmId, reason)` → Handle failure
- `hasActiveMission()` → Check state
- `getMissionStats(alarmId)` → Get statistics

### 8.3 PermissionManager Usage

**File**: `/app/src/main/java/com/omondit/alarmfocus/utils/PermissionManager.kt`

**Called by**:
- `MainActivity.onCreate()` (Line 56) - Initialize
- `MainActivity.checkAndRequestPermissions()` (Line 113) - Request permissions
- `MainActivity.onResume()` (Line 122) - Recheck permissions

**Permissions Managed**:
- SCHEDULE_EXACT_ALARM
- USE_EXACT_ALARM
- POST_NOTIFICATIONS
- CAMERA
- WAKE_LOCK
- VIBRATE
- RECEIVE_BOOT_COMPLETED
- SYSTEM_ALERT_WINDOW

### 8.4 AlarmDiagnostics Usage

**File**: `/app/src/main/java/com/omondit/alarmfocus/utils/AlarmDiagnostics.kt`

**Called by**:
- `DiagnosticsScreen` (Line 32) - Initialize diagnostics
- `MissionManager.logMissionResult()` (Line 200) - Log events

**Methods**:
- `performHealthCheck()` → SystemHealth
- `getBatteryStatus()` → BatteryStatus
- `getDiagnosticLogs()` → String
- `logEvent(type, message)` → Unit
- `clearLogs()` → Unit

### 8.5 BarcodeManager Usage

**File**: Inferred from usage in `IntegratedMissionScreen.kt` and `BarcodeManagementScreen.kt`

**Called by**:
- `BarcodeMissionScreen` (Line 319) - Get registered barcodes
- `BarcodeManagementScreen` (Line 31) - Manage barcodes

**Methods**:
- `getRegisteredBarcodes()` → List<BarcodeData>
- `registerBarcode(code, type, ...)` → Result<String>
- `updateBarcodeUsage(id)` → Unit
- `removeBarcode(id)` → Boolean
- `getBarcodeStats()` → BarcodeStats

### 8.6 PhotoManager Usage

**File**: Inferred from usage in `IntegratedMissionScreen.kt`

**Called by**:
- `PhotoMissionScreen` (Line 113) - Get registered photos

**Methods**:
- `getRegisteredPhotos()` → List<PhotoData>
- `registerPhoto(path, ...)` → Result<String>
- `updatePhotoUsage(id)` → Unit
- `removePhoto(id)` → Boolean

### 8.7 QuoteManager Usage

**File**: Inferred from usage in `IntegratedMissionScreen.kt`

**Called by**:
- `TypingMissionScreen` (Line 224) - Get quotes for typing mission

**Methods**:
- `getRandomQuote()` → String
- `getQuotesByCategory(category)` → List<String>
- `addCustomQuote(text)` → Unit

### 8.8 FocusModeManager Usage

**File**: `/app/src/main/java/com/omondit/alarmfocus/utils/FocusModeManager.kt`

**Called by**:
- `FocusViewModel` (Line 42 in FocusScreen) - Manage focus sessions

**Methods**:
- `startFocusSession(duration)` → Result<Unit>
- `stopFocusSession()` → Unit
- `isSessionActive()` → Boolean
- `getActiveSession()` → FocusSession?

### 8.9 AppUsageMonitor Usage

**File**: `/app/src/main/java/com/omondit/alarmfocus/utils/AppUsageMonitor.kt`

**Called by**:
- `AppUsageScreen` (Line 34) - Monitor app usage

**Methods**:
- `hasUsageStatsPermission()` → Boolean
- `collectTodayUsage()` → Unit
- `getUsageForDateRange(start, end)` → Flow<List<AppUsageEntity>>
- `getCategoryBreakdown(date)` → List<AppUsageEntity>

### 8.10 DeviceAdminManager Usage

**File**: `/app/src/main/java/com/omondit/alarmfocus/utils/DeviceAdminManager.kt`

**Called by**:
- `DeviceAdminOnboardingScreen` (Line 45 in SettingsScreen) - Enable device admin

**Methods**:
- `requestDeviceAdminActivation()` → Unit
- `isDeviceAdminEnabled()` → Boolean
- `removeDeviceAdmin()` → Unit

### 8.11 Utility Flow Diagram

```
UI Layer
 ↓
ViewModel / Screen
 ↓
Utility Manager
 ├─ AlarmScheduler → AlarmManager (System)
 ├─ MissionManager → MissionActivity, AlarmService
 ├─ PermissionManager → System Permissions
 ├─ AlarmDiagnostics → Logs, Health Checks
 ├─ BarcodeManager → SharedPreferences/DB
 ├─ PhotoManager → File System/DB
 ├─ QuoteManager → Assets/DB
 ├─ FocusModeManager → AccessibilityService
 ├─ AppUsageMonitor → UsageStatsManager (System)
 └─ DeviceAdminManager → DevicePolicyManager (System)
```

---

## Summary: Key Flow Paths

### Path 1: App Startup
```
AlarmFocusApplication → MainActivity → AppModule → AppDatabase → NavHost → AlarmsScreen
```

### Path 2: Create Alarm
```
User → QuickCreateAlarmDialog → AlarmViewModel → CreateAlarmUseCase → AlarmRepository → AlarmDao → AlarmScheduler → AlarmManager
```

### Path 3: Alarm Triggers
```
AlarmManager → AlarmReceiver → AlarmService → (3s delay) → MissionManager → MissionActivity
```

### Path 4: Mission Completion
```
User completes mission → IntegratedMissionScreen → onMissionCompleted → MissionManager → AlarmService → Stop alarm
```

### Path 5: Focus Session
```
User selects duration → FocusViewModel → FocusModeManager → FocusSessionDao → AccessibilityService
```

### Path 6: Navigation
```
Bottom Navigation → NavController → Screen (Alarms/Missions/Focus/Settings) → Sub-screens (conditional rendering)
```

---

## File Reference Index

### Core Application
- Application: `/app/src/main/java/com/omondit/alarmfocus/AlarmFocusApplication.kt`
- MainActivity: `/app/src/main/java/com/omondit/alarmfocus/presentation/MainActivity.kt`
- MissionActivity: `/app/src/main/java/com/omondit/alarmfocus/presentation/MissionActivity.kt`

### Dependency Injection
- AppModule: `/app/src/main/java/com/omondit/alarmfocus/di/AppModule.kt`

### Database
- AppDatabase: `/app/src/main/java/com/omondit/alarmfocus/data/database/AppDatabase.kt`
- DAOs: `/app/src/main/java/com/omondit/alarmfocus/data/database/dao/`
- Entities: `/app/src/main/java/com/omondit/alarmfocus/data/database/entities/`
- Repository: `/app/src/main/java/com/omondit/alarmfocus/data/repository/AlarmRepositoryImpl.kt`

### Services
- AlarmReceiver: `/app/src/main/java/com/omondit/alarmfocus/services/AlarmReceiver.kt`
- AlarmService: `/app/src/main/java/com/omondit/alarmfocus/services/AlarmService.kt`

### ViewModels
- AlarmViewModel: `/app/src/main/java/com/omondit/alarmfocus/presentation/viewmodel/AlarmViewModel.kt`
- FocusViewModel: `/app/src/main/java/com/omondit/alarmfocus/presentation/viewmodel/FocusViewModel.kt`
- SleepViewModel: `/app/src/main/java/com/omondit/alarmfocus/presentation/viewmodel/SleepViewModel.kt`

### Use Cases
- CreateAlarmUseCase: `/app/src/main/java/com/omondit/alarmfocus/domain/usecase/CreateAlarmUseCase.kt`
- ToggleAlarmUseCase: `/app/src/main/java/com/omondit/alarmfocus/domain/usecase/ToggleAlarmUseCase.kt`
- DeleteAlarmUseCase: `/app/src/main/java/com/omondit/alarmfocus/domain/usecase/DeleteAlarmUseCase.kt`
- GetUpcomingAlarmsUseCase: `/app/src/main/java/com/omondit/alarmfocus/domain/usecase/GetUpcomingAlarmsUseCase.kt`

### Screens
- AlarmsScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/AlarmsScreen.kt`
- MissionsScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/MissionsScreen.kt`
- FocusScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/FocusScreen.kt`
- SettingsScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/SettingsScreen.kt`
- IntegratedMissionScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/IntegratedMissionScreen.kt`
- DiagnosticsScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/DiagnosticsScreen.kt`
- DeviceAdminOnboardingScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/DeviceAdminOnboardingScreen.kt`
- BarcodeManagementScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/BarcodeManagementScreen.kt`
- SleepAnalyticsScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/SleepAnalyticsScreen.kt`
- AppUsageScreen: `/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/AppUsageScreen.kt`

### Utilities
- AlarmScheduler: `/app/src/main/java/com/omondit/alarmfocus/utils/AlarmScheduler.kt`
- MissionManager: `/app/src/main/java/com/omondit/alarmfocus/utils/MissionManager.kt`
- PermissionManager: `/app/src/main/java/com/omondit/alarmfocus/utils/PermissionManager.kt`
- AlarmDiagnostics: `/app/src/main/java/com/omondit/alarmfocus/utils/AlarmDiagnostics.kt`
- FocusModeManager: `/app/src/main/java/com/omondit/alarmfocus/utils/FocusModeManager.kt`
- AppUsageMonitor: `/app/src/main/java/com/omondit/alarmfocus/utils/AppUsageMonitor.kt`
- BarcodeManager: `/app/src/main/java/com/omondit/alarmfocus/utils/BarcodeManager.kt`
- PhotoManager: `/app/src/main/java/com/omondit/alarmfocus/utils/PhotoManager.kt`
- QuoteManager: `/app/src/main/java/com/omondit/alarmfocus/utils/QuoteManager.kt`
- DeviceAdminManager: `/app/src/main/java/com/omondit/alarmfocus/utils/DeviceAdminManager.kt`

---

## Architecture Summary

The AlarmFocus app follows **Clean Architecture** principles:

1. **Presentation Layer**: Jetpack Compose UI, ViewModels
2. **Domain Layer**: Use Cases, Repository Interfaces, Business Models
3. **Data Layer**: Repository Implementations, Room DAOs, Entities

**Key Patterns**:
- **Manual Dependency Injection** via AppModule
- **Repository Pattern** for data access abstraction
- **Use Case Pattern** for business logic encapsulation
- **StateFlow/Flow** for reactive UI updates
- **Coroutines** for async operations
- **Foreground Service** for reliable alarm playback
- **Broadcast Receiver** for alarm triggers
- **SharedPreferences** for state persistence

**Critical Flows**:
1. Alarm scheduling uses AlarmManager with exact alarms
2. AlarmService runs as foreground service with wake lock
3. Mission enforcement prevents dismissal until completion
4. State persisted to survive process death
5. Post-mission app blocking enforces productivity
