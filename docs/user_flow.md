# ADHD Focus Alarm App - User Flow Document

## Primary User Flows

### 1. First-Time Setup Flow
```
App Launch → Welcome Screen → Permission Requests → Tutorial → Main Dashboard
```

**Detailed Steps:**
1. **App Launch**: Splash screen with accessibility check
2. **Welcome Screen**: ADHD-focused onboarding message
3. **Permission Requests** (Sequential):
    - Alarm & Notification permissions
    - Do Not Disturb override
    - Battery optimization bypass
    - Usage access (for app blocking)
    - Camera (for missions)
    - Device admin (for anti-uninstall)
4. **Quick Tutorial**: 30-second overview of core features
5. **Main Dashboard**: Shows "No alarms set" with prominent "+" button

### 2. Core Alarm Creation Flow
```
Dashboard → Add Alarm → Time Selection → Mission Selection → Sound Selection → Save → Dashboard
```

**Detailed Steps:**
1. **Dashboard**: Tap floating action button "+"
2. **Add Alarm Screen**:
    - Large time picker (accessible)
    - Repeat options (daily, weekdays, custom)
    - Alarm label input
3. **Mission Selection**:
    - None (basic alarm)
    - Math problems
    - Barcode scan
    - Photo verification
    - Physical activity
    - Quote typing
4. **Sound Selection**:
    - 5 pre-loaded ultra-annoying sounds
    - Upload custom MP3
    - Preview functionality
5. **Save Confirmation**: Shows alarm in list with clear active indicator

### 3. Morning Alarm Experience Flow
```
Alarm Triggers → Ultra-Loud Sound → Mission Challenge → Success/Fail → Post-Alarm Actions
```

**Detailed Steps:**
1. **Alarm Triggers**:
    - Overrides silent/DND mode
    - Displays full-screen alarm interface
    - Vibration + maximum volume audio
2. **Mission Challenge** (if enabled):
    - Full-screen mission interface
    - Clear instructions
    - Progress feedback
    - 2-minute timeout (restarts alarm)
3. **Success Path**:
    - Congratulatory message
    - Optional motivational quote
    - App blocking activation (1-hour period)
    - Return to dashboard
4. **Fail Path**:
    - Mission escalates (harder math, etc.)
    - Alarm continues playing
    - Visual feedback on failure
5. **Post-Alarm Actions**:
    - Social media apps blocked for 1 hour
    - Sleep tracking data recorded
    - Wake-up success logged

### 4. Focus Mode Activation Flow
```
Dashboard → Focus Mode → Select Apps to Block → Set Duration → Activate → Block Enforcement
```

**Detailed Steps:**
1. **Dashboard**: Tap "Focus Mode" tab
2. **Focus Mode Setup**:
    - Quick templates (Work, Study, Exercise)
    - Custom app selection
    - Duration picker (15min to 8 hours)
    - Intensity level (Gentle, Moderate, Strict)
3. **Activate Focus**:
    - Confirmation dialog
    - 10-second countdown
    - Notification posted
4. **Block Enforcement**:
    - Blocked apps show overlay with explanation
    - Emergency override available (with friction)
    - Progress notification shows remaining time

### 5. Mission Setup Flow (Barcode Example)
```
Alarm Creation → Select Barcode Mission → Register Barcode → Label Barcode → Test Scan → Save
```

**Detailed Steps:**
1. **Mission Selection**: Choose "Barcode Scan" from mission options
2. **Register Barcode**:
    - Camera opens immediately
    - Clear instructions: "Scan barcode you want to use"
    - Auto-detect and capture
3. **Label Barcode**:
    - Suggest location-based labels ("Kitchen Coffee", "Bathroom")
    - Allow custom labels
4. **Test Scan**:
    - Quick verification scan
    - Shows success/failure feedback
5. **Save Configuration**:
    - Returns to alarm creation
    - Shows selected mission in alarm summary

### 6. Settings & Customization Flow
```
Dashboard → Settings → Category Selection → Modify Settings → Apply Changes
```

**Settings Categories:**
- **Alarm Settings**: Default sound, volume ramping, vibration patterns
- **Mission Settings**: Difficulty levels, timeout periods, accessibility options
- **Focus Settings**: Default blocked apps, break schedules, intensity levels
- **Sleep Settings**: Tracking preferences, bedtime reminders
- **Accessibility**: Font sizes, contrast, voice prompts
- **Data**: Backup/restore, export settings, clear data

### 7. App Blocking Override Flow
```
Blocked App Launch → Block Overlay → Emergency Override Request → Friction Mechanism → Override Granted/Denied
```

**Detailed Steps:**
1. **Blocked App Launch**: User taps blocked app icon
2. **Block Overlay**:
    - Full-screen explanation
    - Shows remaining block time
    - "Focus Mode Active" message
3. **Emergency Override Request**:
    - "I really need to access this app" button
    - Requires deliberate tap (not accidental)
4. **Friction Mechanism**:
    - 30-second wait timer
    - Requires typing "OVERRIDE" in caps
    - Shows impact message
5. **Override Decision**:
    - **Granted**: App opens, logs override event
    - **Denied**: Returns to home screen

## Error & Edge Case Flows

### Alarm Failure Recovery
```
Alarm Fails → Auto-Restart → Log Error → User Notification → Diagnostic Mode
```

### Permission Denied Handling
```
Permission Denied → Explain Impact → Alternative Options → Graceful Degradation
```

### Low Battery Scenarios
```
Battery < 15% → Disable Battery-Intensive Features → Show Warning → Maintain Core Alarm
```

## Accessibility Considerations

### Visual Impairments
- All flows support screen readers
- High contrast mode available
- Large text options (up to 24pt)
- Voice confirmations for critical actions

### Motor Impairments
- Large touch targets (minimum 44dp)
- Long press alternatives for complex gestures
- Voice control for mission completion (where possible)

### Cognitive Load Reduction
- Single-purpose screens (no information overload)
- Clear progress indicators
- Consistent navigation patterns
- Error messages with clear next steps

## Performance Requirements for Flows

### Critical Timing Requirements:
- **Alarm Trigger**: <1 second from scheduled time
- **Mission Loading**: <2 seconds on low-end devices
- **App Block Response**: <500ms when blocked app launched
- **Sound Upload**: <10 seconds for 3MB MP3 file
- **Barcode Scan**: <3 seconds recognition time

### User Experience Metrics:
- **Alarm Success Rate**: >99% reliable triggering
- **Mission Completion Rate**: Track user success rates
- **App Blocking Effectiveness**: <5% override rate during focus mode
- **User Retention**: Measure daily active users after 7 days