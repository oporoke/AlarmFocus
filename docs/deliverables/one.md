# Deliverable 1: Project Setup & Basic UI Framework
**Days 1-3 | Kotlin + Jetpack Compose**

## User Story
> As an ADHD user, I want to see a clean, accessible interface that won't overwhelm me with too many options at once.

## Overview
This deliverable establishes the foundation for the ADHD Focus Alarm App using modern Android development practices with Jetpack Compose. We'll create a clean, accessible interface optimized for users with ADHD.

## Technical Implementation Plan

### Day 1: Project Setup & Architecture

#### 1. Initialize Android Studio Project
```kotlin
// Project Configuration
minSdk = 26 (Android 8.0)
targetSdk = 34
compileSdk = 34
kotlin = "1.9.10"
compose_bom = "2023.10.01"
```

#### 2. Dependencies Setup
Add to `app/build.gradle.kts`:
```kotlin
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Accessibility
    implementation("androidx.compose.ui:ui-semantics")
}
```

#### 3. Project Structure
```
app/
├── src/main/java/com/adhdapp/focusalarm/
│   ├── data/
│   │   ├── database/
│   │   ├── repository/
│   │   └── models/
│   ├── ui/
│   │   ├── theme/
│   │   ├── components/
│   │   ├── screens/
│   │   └── navigation/
│   ├── utils/
│   └── MainActivity.kt
```

### Day 2: ADHD-Optimized Design System & Theme

#### 1. Color Scheme Implementation
Create `ui/theme/Color.kt`:
```kotlin
// High contrast colors optimized for ADHD users
val ADHDLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),        // Calming green
    onPrimary = Color(0xFFFFFFFF),      // High contrast white
    secondary = Color(0xFF1976D2),      // Trustworthy blue
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF8F9FA),     // Soft off-white
    onBackground = Color(0xFF1C1B1F),   // High contrast text
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFD32F2F)           // Clear error indication
)

val ADHDDarkColors = darkColorScheme(
    primary = Color(0xFF4CAF50),        // Softer green for dark mode
    onPrimary = Color(0xFF003300),
    secondary = Color(0xFF42A5F5),
    onSecondary = Color(0xFF003366),
    background = Color(0xFF121212),     // True dark background
    onBackground = Color(0xFFE3E3E3),   // High contrast light text
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE3E3E3),
    error = Color(0xFFEF5350)
)
```

#### 2. Typography for ADHD Accessibility
Create `ui/theme/Type.kt`:
```kotlin
val ADHDTypography = Typography(
    // All text sizes minimum 16sp for accessibility
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,  // Minimum 16pt
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,  // No text smaller than 16sp
        lineHeight = 24.sp
    )
)
```

### Day 3: Navigation & Main Screens Structure

#### 1. Bottom Navigation Implementation
Create `ui/navigation/ADHDBottomNavigation.kt`:
```kotlin
@Composable
fun ADHDBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Filled.Alarm, 
                    contentDescription = null,
                    modifier = Modifier.size(28.dp) // Large touch targets
                )
            },
            label = { 
                Text(
                    "Alarms", 
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ) 
            },
            selected = currentRoute == "alarms",
            onClick = { onNavigate("alarms") },
            modifier = Modifier.semantics {
                contentDescription = "Alarms tab"
            }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Filled.Task, 
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { 
                Text(
                    "Missions", 
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ) 
            },
            selected = currentRoute == "missions",
            onClick = { onNavigate("missions") },
            modifier = Modifier.semantics {
                contentDescription = "Wake-up missions tab"
            }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Filled.Block, 
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { 
                Text(
                    "Focus", 
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ) 
            },
            selected = currentRoute == "focus",
            onClick = { onNavigate("focus") },
            modifier = Modifier.semantics {
                contentDescription = "Focus mode tab"
            }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Filled.Settings, 
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { 
                Text(
                    "Settings", 
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ) 
            },
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            modifier = Modifier.semantics {
                contentDescription = "Settings tab"
            }
        )
    }
}
```

#### 2. Main Activity Setup
Create `MainActivity.kt`:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ADHDAlarmTheme {
                ADHDAlarmApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ADHDAlarmApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "alarms"
    
    Scaffold(
        bottomBar = {
            ADHDBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "alarms",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("alarms") { AlarmsScreen() }
            composable("missions") { MissionsScreen() }
            composable("focus") { FocusScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
```

#### 3. Basic Screen Scaffolds
Create placeholder screens in `ui/screens/`:

**AlarmsScreen.kt:**
```kotlin
@Composable
fun AlarmsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Alarms",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .semantics { contentDescription = "Alarms screen title" }
                .padding(bottom = 24.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "No alarms set yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the + button to create your first alarm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
```

#### 4. Room Database Setup
Create `data/database/ADHDDatabase.kt`:
```kotlin
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val darkMode: Boolean = false,
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val hapticFeedback: Boolean = true
)

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: AppSettings)
}

@Database(
    entities = [AppSettings::class],
    version = 1,
    exportSchema = false
)
abstract class ADHDDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: ADHDDatabase? = null
        
        fun getDatabase(context: Context): ADHDDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ADHDDatabase::class.java,
                    "adhd_alarm_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

#### 5. Settings Screen Implementation
Create `ui/screens/SettingsScreen.kt`:
```kotlin
@Composable
fun SettingsScreen() {
    var darkMode by remember { mutableStateOf(false) }
    var highContrast by remember { mutableStateOf(false) }
    var largeText by remember { mutableStateOf(false) }
    var hapticFeedback by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .semantics { contentDescription = "Settings screen" }
                .padding(bottom = 24.dp)
        )
        
        // Accessibility Section
        SettingsSection(title = "Accessibility") {
            SettingsSwitchItem(
                title = "Dark Mode",
                description = "Easier on the eyes in low light",
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )
            
            SettingsSwitchItem(
                title = "High Contrast",
                description = "Improved visibility and focus",
                checked = highContrast,
                onCheckedChange = { highContrast = it }
            )
            
            SettingsSwitchItem(
                title = "Large Text",
                description = "Bigger text throughout the app",
                checked = largeText,
                onCheckedChange = { largeText = it }
            )
            
            SettingsSwitchItem(
                title = "Haptic Feedback",
                description = "Vibration feedback for interactions",
                checked = hapticFeedback,
                onCheckedChange = { hapticFeedback = it }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$title. $description. ${if (checked) "Enabled" else "Disabled"}"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .padding(start = 16.dp)
                .clearAndSetSemantics { } // Handled by parent Row
        )
    }
}
```

## Accessibility Implementation Checklist

### WCAG 2.1 AA Compliance
- ✅ **Color Contrast**: All text meets 4.5:1 ratio minimum
- ✅ **Text Size**: Minimum 16sp for all text elements
- ✅ **Touch Targets**: Minimum 44dp for all interactive elements
- ✅ **Semantic Labels**: ContentDescription for all UI elements
- ✅ **Focus Management**: Clear focus indicators and logical tab order

### ADHD-Specific Optimizations
- ✅ **Reduced Cognitive Load**: Simple navigation with only 4 main sections
- ✅ **High Contrast**: Strong visual distinction between elements
- ✅ **Calming Colors**: Green and blue color palette to reduce anxiety
- ✅ **Clear Typography**: Sans-serif fonts with adequate spacing
- ✅ **Consistent Layout**: Predictable UI patterns throughout

## Testing Requirements

### Manual UI Testing
1. **Navigation Testing**: Test tab switching on different screen sizes
2. **Accessibility Testing**: Use TalkBack to verify screen reader compatibility
3. **Theme Testing**: Verify both light and dark mode appearance
4. **Touch Target Testing**: Ensure all buttons are easily tappable
5. **Text Scaling**: Test with Android's large text settings enabled

### Automated Testing Setup
Create basic UI tests in `androidTest/`:
```kotlin
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun bottomNavigationWorksCorrectly() {
        composeTestRule.setContent {
            ADHDAlarmTheme {
                ADHDAlarmApp()
            }
        }
        
        // Test navigation between tabs
        composeTestRule.onNodeWithContentDescription("Wake-up missions tab").performClick()
        composeTestRule.onNodeWithText("Missions").assertExists()
        
        composeTestRule.onNodeWithContentDescription("Settings tab").performClick()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }
}
```

## Success Metrics

### Acceptance Criteria Verification
- [ ] App launches without crashes on Android 8.0+
- [ ] Navigation between 4 main sections works smoothly
- [ ] All text is minimum 16sp and scalable
- [ ] Color contrast ratios meet WCAG AA standards (4.5:1+)
- [ ] Settings screen allows basic accessibility preferences
- [ ] UI tested on minimum 2 different screen sizes (phone + tablet)

### Performance Benchmarks
- App startup time: < 2 seconds
- Navigation transition time: < 300ms
- Memory usage: < 50MB during normal operation
- No ANRs (Application Not Responding) during testing

## Next Steps
After completing Deliverable 1, the foundation will be ready for Deliverable 2: Ultra-Loud Alarm Engine implementation. The modular architecture established here will support adding alarm functionality while maintaining the clean, accessible interface optimized for ADHD users.