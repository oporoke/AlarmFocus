# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**AlarmFocus** (also known as FocusAlarmy) is an Android alarm clock app designed for heavy sleepers and ADHD users. It features ultra-loud alarms, wake-up missions (math, barcode scanning, photo matching, typing, activity), and enforces mission completion to dismiss alarms.

**Target**: Android 10.0+ (minSdk 29, targetSdk 36)

## Build Commands

```bash
# Build the project
./gradlew build

# Run on device/emulator
./gradlew installDebug

# Clean build
./gradlew clean

# Run lint checks
./gradlew ktlintCheck

# Run detekt static analysis
./gradlew detekt

# Auto-format code with ktlint
./gradlew ktlintFormat

# Build release APK (minified)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Architecture

The project follows **Clean Architecture** with manual dependency injection (no Hilt/Dagger).

### Layer Structure

```
app/src/main/java/com/omondit/alarmfocus/
├── data/           # Data layer
│   ├── database/   # Room database, DAOs, entities
│   └── repository/ # Repository implementations
├── domain/         # Domain layer
│   ├── model/      # Business models (Alarm, Mission, etc.)
│   ├── repository/ # Repository interfaces
│   └── usecase/    # Use cases (CreateAlarmUseCase, ToggleAlarmUseCase, etc.)
├── presentation/   # Presentation layer
│   ├── ui/         # Compose UI screens, dialogs, navigation
│   ├── viewmodel/  # ViewModels
│   ├── theme/      # Compose theme, colors, typography
│   ├── MainActivity.kt
│   └── MissionActivity.kt
├── services/       # Android services
│   ├── AlarmService.kt    # Foreground service for alarm playback
│   ├── AlarmReceiver.kt   # BroadcastReceiver for alarm triggers
│   └── BootReceiver.kt    # Restores alarms after device reboot
└── utils/          # Utilities
    ├── AlarmScheduler.kt   # Schedules alarms with AlarmManager
    ├── MissionManager.kt   # Coordinates missions between service and UI
    ├── SoundManager.kt     # Audio file management
    ├── BarcodeManager.kt   # Barcode registration/validation
    ├── PhotoManager.kt     # Photo registration/validation
    └── PermissionManager.kt
```

### Key Components

**Dependency Injection**: `AppModule` (di/AppModule.kt) provides manual DI. Instantiate it with `AppModule(context)` and access dependencies like `appModule.createAlarmViewModel()`.

**Database**: Room database with single entity `AlarmEntity`. Access via `AppDatabase.getDatabase(context)`.

**Mission System**:
- `Mission` (abstract class): Base for all mission types (MATH, BARCODE, PHOTO, TYPING, ACTIVITY, NONE)
- `MissionFactory`: Creates missions from `MissionConfig`
- `MissionSession`: Manages active challenge state, attempts, validation
- `MissionManager`: Coordinates mission lifecycle between `AlarmService` and `MissionActivity`

**Alarm Flow**:
1. User creates alarm → `AlarmViewModel` → `CreateAlarmUseCase` → `AlarmRepository` → Room database
2. `AlarmScheduler` schedules with `AlarmManager`
3. At trigger time → `AlarmReceiver` → starts `AlarmService` (foreground service)
4. `AlarmService` plays sound, vibrates, schedules mission start after 3s delay
5. `MissionManager.startMission()` launches `MissionActivity`
6. User completes mission → `AlarmService` stops → alarm dismissed

**State Persistence**: `AlarmService` saves state to SharedPreferences (`alarm_service_state`) to survive process death:
- `active_alarm_id`, `mission_active`, `mission_config`, `alarm_start_time`

**Activities**:
- `MainActivity`: Main app with bottom navigation (Alarms, Missions, Focus, Settings screens)
- `MissionActivity`: Full-screen mission UI that blocks dismissal until mission completes (launched with `showWhenLocked=true`, `turnScreenOn=true`)

## Important Implementation Details

### Alarm Service Lifecycle

The `AlarmService` is a foreground service that:
- Acquires wake lock for 30 minutes
- Overrides Do Not Disturb (DND) by setting ringer mode to NORMAL and max volume
- Ramps volume from 70% to 100% over 10 seconds
- Starts vibration pattern
- Waits 3 seconds before launching mission (to give user time to wake up)
- Prevents stop via ACTION_STOP_ALARM when mission is active
- Restores original audio settings on stop

### Mission Validation

All missions implement:
- `generateChallenge()`: Returns a `Challenge` with question, correct answer, timeout, allowed attempts
- `validateAnswer(challenge, answer)`: Returns `ValidationResult` with correctness, message, escalation flag

Math missions escalate difficulty on wrong answers. Missions track attempts in `MissionSession`.

### Permissions

Required permissions (see AndroidManifest.xml):
- `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM` (critical for alarms)
- `POST_NOTIFICATIONS` (Android 13+)
- `CAMERA` (barcode/photo missions)
- `WAKE_LOCK`, `VIBRATE` (alarm service)
- `RECEIVE_BOOT_COMPLETED` (restore alarms after reboot)
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `SYSTEM_ALERT_WINDOW` (show mission activity over lock screen)

`PermissionManager` handles runtime permission requests.

### Receivers

**AlarmReceiver**: Receives exact alarm broadcasts from `AlarmManager`, starts `AlarmService` with alarm ID and sound URI.

**BootReceiver**: Listens for `BOOT_COMPLETED`, `PACKAGE_REPLACED`, `LOCKED_BOOT_COMPLETED` to reschedule active alarms.

## Code Style

- **Kotlin**: 100% Kotlin codebase
- **Linting**: ktlint (config in root build.gradle.kts) - run `./gradlew ktlintFormat` before committing
- **Static Analysis**: detekt (config at config/detekt/detekt.yml)
- **Formatting**: Excludes generated code (`**/generated/**`)

## Testing

- Unit tests: `app/src/test/java/` (currently minimal)
- Instrumented tests: `app/src/androidTest/java/` (currently minimal)
- Manual testing on real devices recommended due to alarm/wake lock behavior

## Common Gotchas

1. **Mission not starting**: Check `AlarmService` logs for `scheduleMissionStart()` and ensure `MISSION_START_DELAY` (3s) has elapsed.

2. **Alarm not firing**: Verify `SCHEDULE_EXACT_ALARM` permission granted. Check battery optimization settings. Use `AlarmDiagnostics` utility for debugging.

3. **Service killed**: Ensure `AlarmService` is running as foreground service with notification. Check wake lock acquisition in logs.

4. **Mission state lost**: Service saves state to SharedPreferences. On app restart, `MainActivity.checkAndRestoreActiveAlarm()` checks for active missions and relaunches `MissionActivity`.

5. **Room queries on main thread**: All repository methods are `suspend` functions. Always call from coroutine scope (e.g., `viewModelScope.launch`).

6. **Compose recomposition**: ViewModels expose `StateFlow` for UI state. Collect in composables with `collectAsState()`.

7. **Alarm scheduling**: Use `AlarmScheduler` which wraps `AlarmManager.setExactAndAllowWhileIdle()` for API 23+. Handles repeating alarms by rescheduling after trigger.

## Key Files to Review for Changes

- **Alarm logic**: `services/AlarmService.kt`, `services/AlarmReceiver.kt`, `utils/AlarmScheduler.kt`
- **Mission system**: `domain/model/Mission.kt`, `utils/MissionManager.kt`, `presentation/MissionActivity.kt`
- **Data persistence**: `data/database/AppDatabase.kt`, `data/database/entities/AlarmEntity.kt`, `data/repository/AlarmRepositoryImpl.kt`
- **UI**: `presentation/ui/screens/AlarmsScreen.kt`, `presentation/ui/screens/IntegratedMissionScreen.kt`
- **DI**: `di/AppModule.kt`

## Dependencies

Built with Jetpack Compose (BOM-based versioning), Room, CameraX, ML Kit Barcode Scanning, WorkManager, Kotlin Coroutines. See `app/build.gradle.kts` for full list.

## Documentation

See `docs/` for project charter, SRS, user flows, testing strategy, and deliverables.
