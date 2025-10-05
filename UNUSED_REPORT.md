# Unused Files and Resources Report
**Project:** AlarmFocus
**Date:** 2025-10-05
**Analysis Type:** Comprehensive source code scan

## Executive Summary

This report identifies unused files, resources, and code in the AlarmFocus Android application. The analysis scanned:
- 79 Kotlin source files
- 2 drawable resources
- 2 string resources
- 7 color resources
- All XML resources and configuration files

### Statistics
- **Total Kotlin Files Analyzed:** 79
- **Unused Kotlin Files:** 8
- **Total Drawable Resources:** 2 (both used)
- **Total String Resources:** 2 (both used)
- **Unused Color Resources:** 7 (all colors in colors.xml)
- **Unused Theme Components:** 1

---

## 1. Unused Kotlin Files

### 1.1 Use Cases

#### TriggerAlarmUseCase.kt
**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/domain/usecase/TriggerAlarmUseCase.kt`

**Status:** UNUSED

**Reason:**
- No imports found in any source file
- Only referenced in documentation (docs/deliverables/all.md)
- The functionality appears to be handled directly by `AlarmReceiver` which starts `AlarmService`

**Lines:** 1-28 (entire file)

**Recommendation:** Safe to delete. The alarm triggering is already handled by `AlarmReceiver.kt` which directly starts the `AlarmService`.

---

### 1.2 Services

#### AudioManagerHelper.kt
**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/services/AudioManagerHelper.kt`

**Status:** UNUSED

**Reason:**
- No imports of this class found in any source file
- The functionality seems to be replaced by direct audio management in `AlarmService.kt`
- `AlarmService` handles DND override and volume management directly

**Lines:** 1-71 (entire file)

**Recommendation:** Safe to delete. Audio management is now handled directly in `AlarmService.kt`.

---

### 1.3 Screens

#### EnhancedAlarmsScreen.kt
**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/EnhancedAlarmsScreen.kt`

**Status:** UNUSED

**Reason:**
- Not referenced in `MainActivity.kt` navigation
- The main app uses `AlarmsScreen.kt` instead
- Only self-reference found

**Recommendation:** Safe to delete. `AlarmsScreen.kt` is the active alarm list screen.

---

#### AppUsageScreen.kt
**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/AppUsageScreen.kt`

**Status:** UNUSED

**Reason:**
- Not referenced in any navigation code
- Feature appears to be planned but not integrated into app flow
- Only self-reference found

**Recommendation:** Safe to delete if app usage tracking is not a planned feature. Otherwise, integrate into navigation.

---

#### SleepAnalyticsScreen.kt
**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/SleepAnalyticsScreen.kt`

**Status:** UNUSED

**Reason:**
- Not referenced in any navigation code
- No navigation route to this screen in `MainActivity.kt`
- Feature appears to be planned but not integrated

**Recommendation:** Safe to delete if sleep analytics is not a planned feature. Otherwise, integrate into navigation.

---

### 1.4 Utilities

#### SleepTracker.kt
**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/utils/SleepTracker.kt`

**Status:** UNUSED

**Reason:**
- Only referenced by `SleepViewModel.kt`
- However, `SleepViewModel` itself is only used by `SleepAnalyticsScreen.kt` which is unused
- Not integrated into active app features

**Recommendation:** Safe to delete along with `SleepAnalyticsScreen.kt` and `SleepViewModel.kt` if sleep tracking is not a required feature.

---

#### BackupManager.kt
**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/utils/BackupManager.kt`

**Status:** UNUSED

**Reason:**
- No imports found in any active source file
- Backup/restore functionality not exposed in UI
- Only self-reference found

**Recommendation:** Safe to delete if backup feature is not planned. Otherwise, integrate into `SettingsScreen.kt`.

---

### 1.5 Theme Components

#### ADHDColorScheme.kt
**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/presentation/theme/ADHDColorScheme.kt`

**Status:** UNUSED

**Reason:**
- Color definitions exist but are not imported or used
- The app currently uses Material3 default color schemes in `Theme.kt`
- No references found in any composables

**Lines:** 1-50+ (defines color palettes)

**Recommendation:** Either integrate these ADHD-optimized colors into `Theme.kt` or delete the file.

---

### 1.6 Dialogs

#### MissionSettingsDialog.kt
**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/presentation/ui/dialogs/MissionSettingsDialog.kt`

**Status:** UNUSED

**Reason:**
- Not imported or used in any screen
- Mission configuration appears to be handled elsewhere in the UI
- Only self-reference found

**Recommendation:** Safe to delete if mission settings are configured through another UI component.

---

## 2. Unused XML Resources

### 2.1 Color Resources (colors.xml)

**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/res/values/colors.xml`

**All colors UNUSED:**
- `purple_200` (#FFBB86FC) - Not referenced
- `purple_500` (#FF6200EE) - Not referenced
- `purple_700` (#FF3700B3) - Not referenced
- `teal_200` (#FF03DAC5) - Not referenced
- `teal_700` (#FF018786) - Not referenced
- `black` (#FF000000) - Not referenced (Compose uses Color.Black)
- `white` (#FFFFFFFF) - Not referenced (Compose uses Color.White)

**Reason:**
- App uses Jetpack Compose with Material3
- Colors defined in Compose code (`Color.kt`, `ADHDColorScheme.kt`)
- No @color/ references found in codebase

**Recommendation:** Safe to delete entire `colors.xml` file. The app uses Compose's Color definitions.

---

### 2.2 String Resources (strings.xml)

**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/res/values/strings.xml`

**Status:** ALL USED

- `app_name` - Used in AndroidManifest.xml (android:label)
- `accessibility_service_description` - Used in accessibility_service_config.xml

**Recommendation:** Keep all string resources.

---

### 2.3 Drawable Resources

**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/res/drawable/`

**Status:** ALL USED

- `ic_launcher_background.xml` - Used by ic_launcher.xml (mipmap)
- `ic_launcher_foreground.xml` - Used by ic_launcher.xml (mipmap)

**Recommendation:** Keep all drawable resources.

---

## 3. Potentially Unused Features (Further Investigation Needed)

### 3.1 Sleep Tracking System
**Related Files:**
- `SleepTracker.kt`
- `SleepViewModel.kt`
- `SleepAnalyticsScreen.kt`
- `SleepSessionEntity.kt`
- `SleepSessionDao.kt`

**Status:** Implemented but not integrated into app navigation

**Recommendation:** Either:
1. Delete all sleep tracking files if feature is not required
2. Integrate into app by adding navigation route and UI access point

---

### 3.2 App Usage Monitoring
**Related Files:**
- `AppUsageScreen.kt`
- `AppUsageMonitor.kt`
- `AppUsageEntity.kt`
- `AppUsageDao.kt`

**Status:** Implemented but not integrated into main app flow

**Recommendation:** Either:
1. Delete if not required
2. Add navigation route to access this screen

---

## 4. Files with Naming Issues

### BarcodeScannerScreen.kt - Typo in Function Name

**Path:** `/home/nichotieno/AndroidStudioProjects/2025/Sept/AlarmFocus/app/src/main/java/com/omondit/alarmfocus/presentation/ui/screens/BarcodeScannerScreen.kt`

**Issue:** Function is named `BaarcodeScannerScreen` (with double 'a') instead of `BarcodeScannerScreen`

**Line:** 44

**Current Usage:** The misspelled function IS being used in:
- `IntegratedMissionScreen.kt`
- `BarcodeManagementScreen.kt`

**Recommendation:** Rename function to correct spelling `BarcodeScannerScreen`.

---

## 5. Summary of Deletable Files

### Definitely Safe to Delete (8 files):

1. **TriggerAlarmUseCase.kt** - Functionality replaced by AlarmReceiver
2. **AudioManagerHelper.kt** - Functionality integrated into AlarmService
3. **EnhancedAlarmsScreen.kt** - Replaced by AlarmsScreen
4. **MissionSettingsDialog.kt** - Not used in UI flow
5. **ADHDColorScheme.kt** - Colors not integrated into theme
6. **BackupManager.kt** - Feature not exposed in UI
7. **colors.xml** - All colors unused (Compose app)

### Conditional Deletion (depends on feature requirements):

**Sleep Tracking (5 files):**
- SleepTracker.kt
- SleepViewModel.kt
- SleepAnalyticsScreen.kt
- SleepSessionEntity.kt (used by AppDatabase)
- SleepSessionDao.kt (used by AppDatabase)

**App Usage (4 files):**
- AppUsageScreen.kt
- AppUsageMonitor.kt
- AppUsageEntity.kt (used by AppDatabase)
- AppUsageDao.kt (used by AppDatabase)

**Note:** If deleting entities/DAOs, also update `AppDatabase.kt` to remove them from the entities list and DAO methods.

---

## 6. Recommended Actions

### Immediate Actions:
1. Delete 8 definitely unused files
2. Delete colors.xml
3. Fix typo: `BaarcodeScannerScreen` → `BarcodeScannerScreen`

### Decision Required:
1. **Sleep Tracking Feature** - Keep or delete all related files?
2. **App Usage Feature** - Keep or delete all related files?
3. **ADHDColorScheme** - Integrate into theme or delete?

### Clean-up Impact:
- **Total deletable files:** 8-17 files (depending on feature decisions)
- **Estimated LOC reduction:** ~2,000-3,500 lines
- **Build impact:** None (all unused code)
- **Risk level:** Low (no active features affected)

---

## 7. Database Schema Impact

If deleting sleep tracking and app usage features, update `AppDatabase.kt`:

**Current entities:**
```kotlin
@Database(
    entities = [
        AlarmEntity::class,
        AppUsageEntity::class,      // DELETE if removing app usage
        BlockedAppEntity::class,
        FocusSessionEntity::class,
        SleepSessionEntity::class    // DELETE if removing sleep tracking
    ],
    version = 1
)
```

**Updated entities (if deleting both features):**
```kotlin
@Database(
    entities = [
        AlarmEntity::class,
        BlockedAppEntity::class,
        FocusSessionEntity::class
    ],
    version = 1
)
```

Also remove corresponding DAO abstract methods from `AppDatabase.kt`.

---

## Conclusion

The AlarmFocus codebase contains **8 definitively unused files** and **2 incomplete feature sets** (sleep tracking and app usage) totaling **9 additional files**.

**Recommended cleanup will:**
- Remove ~2,000-3,500 lines of unused code
- Improve code maintainability
- Reduce build times marginally
- Simplify codebase for future developers

**No risk to existing functionality** as all identified unused files are completely disconnected from the active application flow.
