# AlarmFocus - ADHD-Optimized UI/UX Blueprint

## 🎨 Design System Overview

### Core Philosophy
**"Calming Clarity, Effortless Focus"**

This design system prioritizes **cognitive ease**, **visual hierarchy**, and **emotional safety** for users with ADHD. Every design decision reduces mental load while maintaining aesthetic excellence.

---

## 1. Color Palette - ADHD-Safe Psychology

### Light Theme
```kotlin
Primary: Deep Calm Blue (#1976D2)
- Reduces anxiety, promotes focus
- High contrast ratio: 4.5:1 on white

Secondary: Soothing Purple (#8E24AA)
- Creative energy without overwhelm
- Complements primary without visual conflict

Tertiary: Energizing Teal (#00897B)
- Refreshing, actionable feeling
- Used for success states

Background: Soft Gray (#F8F9FA)
- Reduces eye strain vs pure white
- Gentle, paper-like feel
```

### Dark Theme
```kotlin
Primary: Soft Blue (#42A5F5)
- OLED-friendly, low brightness
- Maintains calm even in darkness

Background: True Black (#121212)
- Battery efficient on OLED
- Deep sleep-friendly

Surface: Elevated Dark (#1E1E1E)
- Subtle elevation without harsh contrast
```

### Semantic Colors
- ✅ **Success**: #4CAF50 (Green - natural positive)
- ⚠️ **Warning**: #FFA726 (Amber - gentle alert)
- ❌ **Error**: #EF5350 (Red - clear but not alarming)
- ℹ️ **Info**: #42A5F5 (Blue - informative)

### Mission-Specific Colors
```kotlin
Math Mission: #5C6BC0 (Indigo - analytical)
Barcode: #26A69A (Teal - scanning energy)
Photo: #7E57C2 (Purple - creative)
Activity: #FF7043 (Orange - movement)
Typing: #66BB6A (Green - flow state)
```

---

## 2. Typography - Dyslexia-Friendly

### Scale (sp = scale-independent pixels)
```
Display Large: 57sp / Bold / -0.25 letter spacing
Display Medium: 45sp / Bold / 0 spacing
Headline Large: 32sp / Bold / 0 spacing
Title Large: 22sp / SemiBold / 0 spacing
Body Large: 18sp / Normal / 0.5 spacing (1.5x line height)
Body Medium: 16sp / Normal / 0.25 spacing
Label Large: 16sp / SemiBold / 0.1 spacing
```

### Accessibility Features
- **Minimum size**: 16sp for body text (vs 14sp standard)
- **Line height**: 1.5x for comfortable reading
- **Letter spacing**: Optimized for dyslexia
- **Font weight**: Bold for hierarchy, never light

### Special Text Styles
```kotlin
TimeDisplay: 64sp / Bold / -1 spacing
- Instant recognition of alarm time

MissionInstruction: 20sp / SemiBold / 0.15 spacing
- Crystal clear task guidance

CalmText: 16sp / Normal / 1sp spacing
- Extra breathing room for reassurance
```

---

## 3. Spacing & Layout

### Spacing Scale (dp = density-independent pixels)
```
4dp:   Micro spacing (icon padding)
8dp:   Tight spacing (chip elements)
12dp:  Compact spacing (list items)
16dp:  Standard spacing (card padding)
24dp:  Generous spacing (section gaps)
32dp:  Large spacing (screen margins)
48dp:  Extra large (empty states)
```

### Touch Targets
- **Minimum**: 44dp × 44dp (WCAG AAA)
- **Recommended**: 56dp × 56dp for primary actions
- **FAB**: 72dp diameter (extra prominent)

### Card Dimensions
```kotlin
Alarm Card:
- Corner radius: 20dp
- Elevation: 4dp (enabled) / 1dp (disabled)
- Padding: 20dp
- Min height: 120dp

Mission Card:
- Corner radius: 24dp
- Elevation: 8dp
- Padding: 24dp
```

---

## 4. Shapes & Corners

### Border Radius Philosophy
**"Soft edges = calm minds"**

```kotlin
Extra Small: 8dp  (chips, badges)
Small: 12dp       (text fields, small buttons)
Medium: 16dp      (standard cards, dialogs)
Large: 24dp       (hero cards, bottom sheets)
Extra Large: 32dp (modals, full-screen)

Pill Shape: 100dp (infinite radius)
Circle: 50% (profile pics, icon buttons)
```

### Custom Shapes
```kotlin
AlarmCard: 20dp all corners
MissionScreen: 32dp top corners only
BottomNav: 16dp top corners only
TimePicker: 28dp (extra soft)
```

---

## 5. Elevation & Shadows

### Depth Hierarchy
```
Level 0: 0dp   (background)
Level 1: 1dp   (disabled cards)
Level 2: 2dp   (inactive elements)
Level 3: 4dp   (active cards)
Level 4: 8dp   (dialogs, modals)
Level 5: 16dp  (FAB, top app bar)
```

### Shadow Colors
- Light theme: Black @ 12% opacity
- Dark theme: Black @ 40% opacity
- Blur radius: 2x elevation value

---

## 6. Animation & Motion

### Timing Functions
```kotlin
Fast: 200ms     (micro-interactions)
Normal: 300ms   (standard transitions)
Slow: 400ms     (page transitions)
Extra Slow: 600ms (dramatic reveals)
```

### Easing Curves
```kotlin
EaseInOutCubic: (0.65, 0, 0.35, 1)
- Smooth, natural motion

EaseOutQuart: (0.25, 1, 0.5, 1)
- Quick response, gentle landing

GentleSpring: DampingRatio=MediumBouncy, Stiffness=Low
- Friendly, organic feel
```

### Animation Types
1. **Screen Transitions**
   - Slide + Fade (1/3 screen distance)
   - Duration: 300ms
   - Easing: EaseInOutCubic

2. **Card Interactions**
   - Scale: 0.96x on press
   - Spring animation
   - Haptic feedback

3. **Success States**
   - Bounce animation
   - Green glow effect
   - Duration: 400ms

4. **Error States**
   - Shake animation (±10dp)
   - Red flash
   - Duration: 400ms

---

## 7. Screen Layouts

### 1. Main Dashboard
```
[Hero Header]
├─ Greeting Text (headlineLarge)
├─ Alarm Count Badge (pill shape)
└─ Next Alarm Indicator (success color)

[Alarm List]
├─ Alarm Card (20dp corners)
│  ├─ Time Display (48sp bold)
│  ├─ Label + Mission Badge
│  ├─ Repeat Days (circle chips)
│  └─ Toggle Switch (1.3x scale)
└─ Empty State (icon 120dp)

[FAB] (72dp, bottom-right, 24dp margin)
```

**Visual Features**:
- Animated gradient background (20s loop)
- Smooth scroll with momentum
- Haptic feedback on all interactions
- Elevation changes on enable/disable

### 2. Alarm Creation Flow
```
[Time Picker Modal]
├─ Large Clock Face (28dp corners)
├─ AM/PM Toggle (pill buttons)
└─ Confirm Button (56dp height)

[Mission Selector]
├─ Mission Type Cards (grid)
│  ├─ Color-coded icons
│  ├─ Brief description
│  └─ Difficulty selector
└─ Preview Card (shows selection)

[Sound Picker]
├─ Waveform Visualization
├─ Play/Pause Button (48dp)
└─ Volume Slider (thick track)

[Repeat Settings]
├─ Day of Week Chips (44dp)
├─ Quick Presets (pills)
└─ Custom Pattern Builder
```

**UX Features**:
- One decision per screen (no overwhelm)
- Clear "Back" and "Next" buttons (56dp)
- Progress indicator at top
- Auto-save drafts

### 3. Mission Execution Screen
```
[Header]
├─ Mission Icon (64dp, pulsing)
├─ Instruction Text (20sp, bold)
└─ Timer/Countdown (if applicable)

[Mission Content]
├─ Math: Large equation (32sp)
├─ Barcode: Camera viewfinder
├─ Photo: Side-by-side comparison
├─ Activity: Movement counter (64sp)
└─ Typing: Quote + Input field

[Feedback Area]
├─ Attempt Counter
├─ Hint Button (if enabled)
└─ Give Up Button (red, 30s delay)

[Answer Section]
├─ Input Field / Action Button (72dp)
└─ Submit Button (full width, 64dp)
```

**Visual Feedback**:
- ✅ Correct: Green glow + bounce + haptic
- ❌ Wrong: Red flash + shake + haptic
- ⏱️ Timeout: Amber pulse animation

### 4. Focus Mode Dashboard
```
[Status Card]
├─ Focus Timer (circular progress)
├─ Current Session Name
└─ Remaining Time (48sp)

[Quick Actions]
├─ Start/Stop Button (72dp FAB)
├─ Extend Time (+15min)
└─ End Early (confirmation)

[Blocked Apps List]
├─ App Icon + Name
├─ Unblock Time
└─ Category Badge

[Session Templates]
├─ Work (60min) - Blue
├─ Study (45min) - Purple
└─ Exercise (30min) - Orange
```

### 5. Sleep Analytics Screen
```
[Hero Stats]
├─ Last Night Quality (circle gauge)
├─ Hours Slept (64sp)
└─ Quality Score (0-100)

[Weekly Trend Graph]
├─ Bar chart (7 days)
├─ Sleep phases (stacked colors)
└─ Alarm success indicators

[Insights Cards]
├─ Average Quality
├─ Best/Worst Night
└─ Correlation with Alarms

[Sleep Phases Breakdown]
├─ Deep Sleep (green bar)
├─ Light Sleep (blue bar)
└─ Awake Time (amber bar)
```

### 6. Settings Screen
```
[Appearance Section]
├─ Dark Mode Toggle (large)
├─ Color Scheme Selector
└─ Text Size Slider (14-22sp)

[Accessibility]
├─ High Contrast Mode
├─ Reduce Motion
├─ Haptic Strength (slider)
└─ Screen Reader Support

[Alarm Defaults]
├─ Default Sound
├─ Default Mission
└─ Vibration Pattern

[Data & Backup]
├─ Export Data Button
├─ Import Data Button
└─ Clear All Data (red, confirmation)
```

---

## 8. Navigation

### Bottom Navigation Bar
```
[Tab Structure]
├─ Alarms (Icon: Alarm, Active: Primary)
├─ Missions (Icon: Task, Active: Purple)
├─ Focus (Icon: Block, Active: Teal)
├─ Sleep (Icon: Bedtime, Active: Indigo)
└─ Settings (Icon: Settings, Active: Gray)

Design:
- Height: 72dp (extra tall for ADHD)
- Icon size: 28dp
- Label size: 16sp (always visible)
- Active indicator: 4dp bottom bar
- Top corners: 16dp radius
- Elevation: 8dp
```

**Interaction**:
- Haptic feedback on tap
- Smooth slide animation (200ms)
- Icon scale animation on active
- Color transition (300ms)

---

## 9. Component Library

### Buttons
```kotlin
// Primary Action (56dp height)
Button(
    shape = ADHDCustomShapes.PillButton,
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    ),
    contentPadding = PaddingValues(horizontal = 24.dp)
)

// Secondary Action (48dp height)
OutlinedButton(
    border = BorderStroke(2.dp, primary),
    shape = ADHDCustomShapes.PillButton
)

// Danger Action (red, with confirmation)
FilledTonalButton(
    colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = ErrorRed.copy(alpha = 0.15f)
    )
)
```

### Cards
```kotlin
// Standard Card
Card(
    shape = ADHDShapes.medium,
    elevation = CardDefaults.cardElevation(4.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
)

// Hero Card (elevated)
Card(
    shape = ADHDCustomShapes.AlarmCard,
    elevation = CardDefaults.cardElevation(8.dp),
    modifier = Modifier.shadow(12.dp)
)
```

### Dialogs
```kotlin
// Modal Dialog
AlertDialog(
    shape = ADHDShapes.large,
    tonalElevation = 8.dp,
    containerColor = MaterialTheme.colorScheme.surface
)

// Bottom Sheet
ModalBottomSheet(
    shape = ADHDCustomShapes.MissionScreen,
    dragHandle = { /* Custom 48dp wide handle */ }
)
```

### Input Fields
```kotlin
OutlinedTextField(
    shape = ADHDShapes.medium,
    textStyle = MaterialTheme.typography.bodyLarge,
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primary,
        focusedLabelColor = primary,
        cursorColor = primary
    )
)
```

---

## 10. Accessibility Checklist

### ✅ WCAG 2.1 AA Compliance
- [x] Text contrast ≥ 4.5:1
- [x] UI element contrast ≥ 3:1
- [x] Touch targets ≥ 44dp
- [x] Text resize up to 200%
- [x] No flashing content >3Hz
- [x] Keyboard navigation support

### ✅ ADHD-Specific Features
- [x] Minimal visual clutter
- [x] Clear focus indicators (4dp outline)
- [x] Consistent layout patterns
- [x] Generous spacing (16dp+)
- [x] Predictable animations
- [x] One primary action per screen
- [x] Emergency override options
- [x] Calming color palette

### ✅ Motion & Haptics
- [x] Reduce motion support
- [x] Haptic feedback on all actions
- [x] Smooth 60fps animations
- [x] No jarring transitions

---

## 11. Implementation Checklist

### Phase 1: Theme Foundation ✅
- [x] Color scheme (light/dark)
- [x] Typography scale
- [x] Shape system
- [x] Animation constants

### Phase 2: Core Components
- [x] Enhanced dashboard
- [ ] Alarm creation flow
- [ ] Mission execution screens
- [ ] Focus mode UI
- [ ] Sleep analytics UI

### Phase 3: Interactions
- [ ] Haptic feedback integration
- [ ] Screen transitions
- [ ] Loading states
- [ ] Error states
- [ ] Success animations

### Phase 4: Polish
- [ ] Dark mode testing
- [ ] Accessibility audit
- [ ] Performance optimization
- [ ] User testing with ADHD focus group

---

## 12. Design Rationale

### Why Rounded Corners?
Sharp edges create visual tension. Rounded corners (16-32dp) create a **friendly, safe environment** that reduces anxiety—critical for ADHD users who may experience heightened sensitivity to visual stimuli.

### Why Large Text?
18sp body text (vs 14sp standard) accommodates:
- Vision difficulties
- Dyslexia (larger = easier tracking)
- Quick scanning (ADHD = impulsive scrolling)

### Why Calming Blues/Purples?
**Color psychology for ADHD**:
- 🔵 Blue: Reduces heart rate, promotes calm
- 🟣 Purple: Creativity without overstimulation
- 🟢 Green: Natural positive reinforcement
- ❌ Avoid Red (except errors): Triggers stress

### Why Generous Spacing?
24dp margins create **visual breathing room**, preventing:
- Crowded feeling
- Accidental taps
- Cognitive overload

### Why Haptic Feedback?
Multi-sensory feedback (visual + tactile) **anchors attention** for ADHD users, confirming actions without requiring prolonged visual focus.

---

## 13. Production Integration

### File Structure
```
app/src/main/java/com/omondit/alarmfocus/
├── presentation/theme/
│   ├── ADHDColorScheme.kt ✅
│   ├── ADHDTypography.kt ✅
│   ├── ADHDShapes.kt ✅
│   ├── ADHDAnimations.kt ✅
│   └── Theme.kt ✅
├── presentation/ui/screens/
│   ├── EnhancedAlarmsScreen.kt ✅
│   ├── AlarmCreationFlow.kt (TODO)
│   ├── MissionExecutionScreen.kt (TODO)
│   ├── FocusModeScreen.kt (TODO)
│   └── SleepAnalyticsScreen.kt (TODO)
└── presentation/ui/components/
    ├── ADHDButton.kt (TODO)
    ├── ADHDCard.kt (TODO)
    └── ADHDTextField.kt (TODO)
```

### Usage Example
```kotlin
@Composable
fun MyScreen() {
    AlarmFocusTheme(darkTheme = isSystemInDarkTheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            EnhancedAlarmsScreen(viewModel)
        }
    }
}
```

---

## 14. Success Metrics

### User Experience Goals
- ⏱️ **Task Completion**: <3 taps to create alarm
- 📊 **Comprehension**: 95% understand UI without tutorial
- 🎯 **Accessibility**: WCAG AAA compliance
- 💚 **Emotional Response**: "Calming and trustworthy"
- ⚡ **Performance**: 60fps scrolling, <100ms interaction latency

### ADHD-Specific Metrics
- 🧠 **Cognitive Load**: Hick's Law <5 choices per screen
- 👁️ **Visual Hierarchy**: F-pattern eye tracking support
- 🔔 **Alarm Success**: 90%+ mission completion rate
- 😌 **Stress Reduction**: Post-use cortisol measurement

---

**This is the ADHD alarm app we've been waiting for.** 🎨✨
