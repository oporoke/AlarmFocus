# Deliverable 2: Ultra-Loud Alarm Engine - Complex Audio/System Integration

## The ADHD Sleep Challenge

### Why ADHD Users Need Ultra-Loud Alarms
- **Deeper Sleep Patterns**: ADHD brains often experience deeper REM cycles, making wake-up more difficult
- **Sensory Processing Differences**: Many ADHD individuals have sensory processing variations requiring stronger stimuli
- **Medication Effects**: ADHD medications can affect sleep depth and wake responsiveness
- **Executive Function Delays**: The prefrontal cortex takes longer to "boot up" in ADHD brains
- **Dopamine Regulation**: Morning dopamine levels are typically lower in ADHD individuals

---
### Thought Process & Architecture Decision

**Core Requirements Analysis:**
1. **Maximum Volume Control**: Need to override all system volume settings
2. **DND Override**: Must work even in Do Not Disturb mode
3. **Persistence**: Alarm must survive phone restarts and app kills
4. **Accessibility**: ADHD users need reliability above all else
5. **Performance**: Low battery impact while maintaining 99.9% reliability

**Technical Approach:**
- Use **ForegroundService** for alarm reliability
- **AudioManager** for volume control with **STREAM_ALARM**
- **MediaPlayer** with **AudioAttributes** for proper alarm routing
- **WakeLock** to prevent device sleep during alarm
- **Notification** channel for alarm service visibility

**File Structure Decision:**
```
app/src/main/java/com/adhdfocu alarms/
├── data/
│   ├── database/
│   ├── repository/
├── domain/
│   ├── model/
│   ├── usecase/
├── presentation/
│   ├── ui/
│   ├── viewmodel/
├── service/ ← Deliverable 2 files go here
│   ├── AlarmService.kt
│   ├── AlarmReceiver.kt
│   └── AudioManager.kt
├── util/
└── di/
```

## Summary of Deliverable 2 Implementation
### Key Components Delivered:

1. **AlarmService.kt** - Core ultra-loud alarm engine with:
   - Maximum volume override (ignores silent mode)
   - Do Not Disturb bypass capability
   - Volume ramping (70% → 100% over 10 seconds)
   - Aggressive vibration patterns
   - Wake lock management for reliability
   - Foreground service for persistence
   - Graceful error handling with fallback sounds

2. **AlarmReceiver.kt** - Broadcast receiver for reliable alarm triggering:
   - Handles AlarmManager triggers
   - Starts foreground service appropriately
   - Works across device reboots

3. **AudioManagerHelper.kt** - Audio system management utility:
   - Permission checking for DND override
   - Volume level management
   - System state restoration

4. **Supporting Infrastructure**:
   - **BootReceiver.kt** - Restores alarms after device restart
   - **PermissionManager.kt** - Handles all required permissions with ADHD-friendly explanations
   - **AlarmValidator.kt** - Validates alarm configuration and system state
   - **AndroidManifest.xml** - All required permissions and service declarations
   - **build.gradle.kts** - Essential dependencies

### ADHD-Specific Design Decisions:

1. **Maximum Reliability**: Service runs in foreground, uses wake locks, survives phone restarts
2. **Maximum Impact**: 100dB volume capability, aggressive vibration, designed to wake heavy sleepers
3. **No Snooze**: Intentionally prevents the "snooze trap" common with ADHD
4. **Clear Feedback**: Visual notifications show alarm status clearly
5. **Error Recovery**: Multiple fallback options ensure alarm sounds even if custom sound fails

### File Structure Integration:
```
app/src/main/java/com/adhdfocusalarm/
├── service/           ← All Deliverable 2 files
│   ├── AlarmService.kt
│   ├── AlarmReceiver.kt
│   ├── AudioManagerHelper.kt
│   └── BootReceiver.kt
├── util/             ← Supporting utilities
│   ├── PermissionManager.kt
│   └── AlarmValidator.kt
└── assets/           ← Default alarm sound
└── default_alarm_ultra_loud.mp3
```

### Testing Strategy for Deliverable 2:
1. **Volume Testing**: Verify maximum volume across different device states
2. **DND Testing**: Confirm alarm sounds during Do Not Disturb mode
3. **Persistence Testing**: Restart phone multiple times, verify alarm reliability
4. **Battery Testing**: Test with various battery saver modes enabled
5. **Accessibility Testing**: Verify notification visibility and screen reader compatibility

This implementation provides a solid, standalone foundation for the ultra-loud alarm system while being designed for integration with subsequent deliverables (particularly D3 for scheduling and D6 for mission integration).