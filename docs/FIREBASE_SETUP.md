# Firebase Crashlytics Setup Guide

This guide will walk you through setting up Firebase Crashlytics for the ADHD Focus Alarm app.

**Estimated Time**: 15-30 minutes

---

## Prerequisites

- Google account
- Access to Firebase Console
- Android Studio with the project open

---

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)

2. Click **"Add project"** or **"Create a project"**

3. Enter project details:
   - **Project name**: `ADHD Focus Alarm` (or your preferred name)
   - Click **Continue**

4. **Google Analytics** (Optional):
   - Toggle OFF if you don't need analytics
   - Or select **Default Account for Firebase**
   - Click **Continue**

5. Wait for project creation (30-60 seconds)

6. Click **Continue** when done

---

## Step 2: Add Android App to Firebase Project

1. In Firebase Console, click the **Android icon** to add an Android app

2. **Register app**:
   - **Android package name**: `com.omondit.alarmfocus`
   - **App nickname** (optional): `ADHD Focus Alarm`
   - **Debug signing certificate SHA-1** (optional for now): Leave blank
   - Click **Register app**

3. **Download google-services.json**:
   - Click **Download google-services.json**
   - **IMPORTANT**: Save this file, you'll need it in the next step

4. Click **Next**

5. Skip the SDK configuration steps (already done in code)
   - Click **Next**
   - Click **Continue to console**

---

## Step 3: Add google-services.json to Project

1. **Locate the file**:
   - Find the `google-services.json` you just downloaded

2. **Copy to project**:
   ```bash
   # From your Downloads folder, copy to app/ directory
   cp ~/Downloads/google-services.json /path/to/AlarmFocus/app/
   ```

   Or manually:
   - Open Android Studio
   - In Project view, navigate to `app/` directory
   - Drag and drop `google-services.json` into the `app/` folder
   - **Ensure it's in `app/` NOT in `app/src/`**

3. **Verify placement**:
   ```
   AlarmFocus/
   ├── app/
   │   ├── google-services.json  ← Should be here
   │   ├── build.gradle.kts
   │   └── src/
   ├── build.gradle.kts
   └── settings.gradle.kts
   ```

---

## Step 4: Enable Crashlytics in Firebase Console

1. In Firebase Console, click **Crashlytics** in left sidebar

2. Click **Enable Crashlytics**

3. Click **Set up Crashlytics** (if prompted)

4. Wait for initialization (this can take a few minutes)

---

## Step 5: Update Build Configuration

### 5.1 Update Root build.gradle.kts

Open `build.gradle.kts` (Project level) and ensure these plugins are present:

```kotlin
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}
```

**Already done in codebase**, but verify it matches.

### 5.2 Update App build.gradle.kts

Open `app/build.gradle.kts` and ensure these plugins are applied:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")  // ← Add this
    id("com.google.firebase.crashlytics") // ← Add this
}
```

**Already partially done**, just uncomment or add the last two lines.

---

## Step 6: Sync Project

1. In Android Studio, click **Sync Now** (banner at top)

2. Or: **File → Sync Project with Gradle Files**

3. Wait for sync to complete (1-2 minutes)

4. **Check for errors**:
   - If you see "google-services.json not found" → Check file location (Step 3)
   - If you see plugin version errors → Update plugin versions in Step 5

---

## Step 7: Test Crashlytics

### 7.1 Build and Run

```bash
./gradlew clean assembleDebug
./gradlew installDebug
```

Or in Android Studio:
- Click **Run** (green play button)

### 7.2 Trigger a Test Crash

Add this code to any screen (e.g., `MainActivity.kt`):

```kotlin
// Temporary test button
Button(onClick = {
    throw RuntimeException("Test crash for Crashlytics")
}) {
    Text("Test Crash")
}
```

**Or use Crashlytics API**:

```kotlin
import com.google.firebase.crashlytics.FirebaseCrashlytics

// Trigger test crash
Button(onClick = {
    FirebaseCrashlytics.getInstance().log("Testing Crashlytics")
    throw RuntimeException("Test crash")
}) {
    Text("Test Crash")
}
```

### 7.3 Verify Crash Appears in Console

1. Run the app on a device/emulator
2. Tap the test crash button
3. App will crash and close
4. Restart the app (crash report is sent on next launch)
5. Wait 5-10 minutes
6. Go to Firebase Console → Crashlytics
7. You should see the test crash appear

**Remove test crash code after verification!**

---

## Step 8: Configure Crashlytics in Code

### 8.1 Verify Application Class

Check `app/src/main/java/com/omondit/alarmfocus/AlarmFocusApplication.kt`:

```kotlin
class AlarmFocusApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
        }
    }
}
```

**Already implemented**, just verify it exists.

### 8.2 Verify AndroidManifest

Check `app/src/main/AndroidManifest.xml`:

```xml
<application
    android:name=".AlarmFocusApplication"  ← Verify this line
    ...>
```

**Already done**, just verify.

---

## Step 9: Production Configuration

### 9.1 Disable Crashlytics in Debug Builds (Optional)

To avoid test crashes polluting production data:

```kotlin
// In AlarmFocusApplication.kt
override fun onCreate() {
    super.onCreate()

    val crashlytics = FirebaseCrashlytics.getInstance()

    // Disable in debug builds
    crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
}
```

### 9.2 Add ProGuard Rules

Create/update `app/proguard-rules.pro`:

```proguard
# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Keep crash reporting classes
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
```

---

## Step 10: Logging Custom Events

### Usage in Code

```kotlin
// Get application instance
val app = application as AlarmFocusApplication

// Log non-fatal exception
try {
    // Some risky operation
} catch (e: Exception) {
    app.logException(e, "Failed to save alarm")
}

// Log custom event
app.logEvent("alarm_created", mapOf(
    "mission_type" to "MATH",
    "difficulty" to "HARD"
))

// Set user ID (for tracking across sessions)
app.setUserId("user_${Random.nextInt()}")
```

---

## Troubleshooting

### Issue: "google-services.json not found"

**Solution**:
- Ensure file is in `app/` directory, not `app/src/`
- File name is exactly `google-services.json` (lowercase)
- Sync project again

### Issue: "No crashes appearing in console"

**Solution**:
- Wait 10-15 minutes after crash (processing delay)
- Ensure app was restarted after crash (reports sent on next launch)
- Check you're looking at correct project in Firebase Console
- Verify internet connection on test device

### Issue: "Plugin version conflicts"

**Solution**:
```kotlin
// Use these specific versions
id("com.google.gms.google-services") version "4.4.0"
id("com.google.firebase.crashlytics") version "2.9.9"
```

### Issue: "Duplicate class errors"

**Solution**:
- Clean project: `./gradlew clean`
- Invalidate caches: File → Invalidate Caches → Invalidate and Restart

---

## Verification Checklist

- [ ] Firebase project created
- [ ] Android app added to Firebase project
- [ ] `google-services.json` downloaded
- [ ] `google-services.json` placed in `app/` directory
- [ ] Crashlytics enabled in Firebase Console
- [ ] Plugins added to build files
- [ ] Project synced successfully
- [ ] Test crash appears in Firebase Console
- [ ] Test crash code removed
- [ ] Production configuration applied

---

## Optional: Analytics Integration

If you enabled Google Analytics during Firebase setup:

```kotlin
// In AlarmFocusApplication.kt
import com.google.firebase.analytics.FirebaseAnalytics

private lateinit var analytics: FirebaseAnalytics

override fun onCreate() {
    super.onCreate()

    // Initialize Analytics
    analytics = FirebaseAnalytics.getInstance(this)

    // Log app open
    analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
}
```

**Note**: Analytics is optional and not required for Crashlytics.

---

## Security Note

**Never commit google-services.json to public repositories!**

Add to `.gitignore`:

```gitignore
# Firebase
google-services.json
```

For team collaboration, share via:
- Secure cloud storage (Google Drive with restricted access)
- Team password manager
- Environment variables in CI/CD

---

## CI/CD Integration

For GitHub Actions, add Firebase secrets:

1. **Encode google-services.json**:
   ```bash
   cat app/google-services.json | base64
   ```

2. **Add to GitHub Secrets**:
   - Go to GitHub repo → Settings → Secrets → Actions
   - Add secret: `GOOGLE_SERVICES_JSON`
   - Paste base64 output

3. **Decode in workflow** (already in `.github/workflows/android.yml`):
   ```yaml
   - name: Create google-services.json
     run: echo "${{ secrets.GOOGLE_SERVICES_JSON }}" | base64 -d > app/google-services.json
   ```

---

## Support

**Firebase Documentation**: https://firebase.google.com/docs/crashlytics/get-started?platform=android

**Common Issues**: https://firebase.google.com/docs/crashlytics/troubleshooting

**Firebase Support**: https://firebase.google.com/support

---

**Setup Complete!** 🎉

Your app now has production-grade crash reporting. All crashes will be automatically reported to Firebase Crashlytics with:
- Stack traces
- Device information
- Custom logs
- User identifiers (if set)

Next steps:
1. Monitor crashes in Firebase Console
2. Fix critical issues
3. Deploy updates
4. Track crash-free users percentage

---

**Last Updated**: October 4, 2025
**Version**: 1.0
