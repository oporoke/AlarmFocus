# Privacy Policy - ADHD Focus Alarm App

**Effective Date**: October 4, 2025
**Last Updated**: October 4, 2025

## Introduction

ADHD Focus Alarm ("we," "our," or "the app") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and safeguard your information when you use our Android application.

**Important**: All data is stored locally on your device. We do not transmit, sell, or share your personal data with any third parties.

---

## Data We Collect

### 1. Alarm Data (Stored Locally)
- Alarm times, labels, and repeat schedules
- Mission type and difficulty settings
- Custom alarm sounds (URI references only)
- Snooze and dismissal history
- Alarm success/failure statistics

**Purpose**: To provide alarm functionality and track your wake-up success patterns.

**Storage**: Encrypted locally in app database (AES-256 encryption).

### 2. App Usage Data (Stored Locally)
- Time spent in other apps (via Android UsageStatsManager API)
- App category classifications (e.g., Social Media, Productivity)
- Focus session history
- Blocked app interactions

**Purpose**: To enable focus mode and app blocking features designed to help you maintain productivity.

**Storage**: Encrypted locally in app database.

**Permission Required**: Usage Stats Access (PACKAGE_USAGE_STATS)

### 3. Sleep Tracking Data (Stored Locally)
- Accelerometer movement data during sleep
- Sleep phase classification (deep sleep, light sleep, awake)
- Sleep quality scores
- Correlation between sleep quality and alarm dismissal success

**Purpose**: To provide insights into your sleep patterns and help optimize alarm timing.

**Storage**: Encrypted locally in app database.

**Permission Required**: Activity Recognition (for sensor access)

### 4. Mission Data (Stored Locally)
- **Reference Photos**: Encrypted photos for photo comparison missions
- **Barcode Strings**: Encrypted barcode data for scan missions
- Math mission completion times and accuracy
- Activity tracking step counts
- Typing mission statistics

**Purpose**: To provide interactive wake-up challenges that ensure you're fully alert.

**Storage**: All sensitive data encrypted with AES-256 using AndroidX Security library.

### 5. Device Information (For Crash Reports Only)
- Device model and manufacturer
- Android OS version
- App version and build number
- Crash stack traces (no personal data)

**Purpose**: To identify and fix bugs that affect app stability.

**Third Party**: Firebase Crashlytics (Google)

**Anonymous**: No personally identifiable information is included in crash reports.

---

## Data Security

### Encryption
We take your data security seriously:

- **AES-256 Encryption**: All sensitive data (photos, barcodes, sound URIs, preferences) is encrypted using industry-standard AES-256-GCM encryption.
- **Android Keystore**: Encryption keys are managed by Android's secure hardware-backed Keystore.
- **No Cloud Storage**: Your data never leaves your device.

### Local Storage
All app data is stored in your device's private app directory (`/data/data/com.omondit.alarmfocus/`), which is:
- Inaccessible to other apps
- Protected by Android's app sandboxing
- Automatically deleted when you uninstall the app

---

## Permissions We Request

### Required Permissions

1. **Schedule Exact Alarm** (`SCHEDULE_EXACT_ALARM`)
   - **Why**: To trigger alarms at the precise time you set
   - **Usage**: Core functionality for alarm system

2. **Post Notifications** (`POST_NOTIFICATIONS`)
   - **Why**: To display alarm notifications and reminders
   - **Usage**: Required for Android 13+ devices

3. **Wake Lock** (`WAKE_LOCK`)
   - **Why**: To ensure alarms trigger even when device is in deep sleep
   - **Usage**: Critical for alarm reliability

4. **Vibrate** (`VIBRATE`)
   - **Why**: To provide vibration feedback for alarms
   - **Usage**: Alarm tactile notification

5. **Foreground Service** (`FOREGROUND_SERVICE`)
   - **Why**: To keep alarm service running reliably
   - **Usage**: Prevents Android from killing alarm process

### Optional Permissions

6. **Camera** (`CAMERA`)
   - **Why**: For photo comparison and barcode scan missions
   - **Usage**: Only accessed when you enable these mission types
   - **You can decline**: App works without these missions

7. **Usage Stats Access** (`PACKAGE_USAGE_STATS`)
   - **Why**: To monitor app usage for focus mode blocking features
   - **Usage**: Only accessed when you enable app blocking
   - **You can decline**: App works without focus mode features

8. **Accessibility Service** (`BIND_ACCESSIBILITY_SERVICE`)
   - **Why**: To detect and block distracting apps during focus modes
   - **Usage**: Only when focus mode is active
   - **You can disable**: This is an optional productivity feature

9. **Device Admin** (Optional)
   - **Why**: To prevent accidental uninstall during impulsive moments
   - **Usage**: Optional protection against late-night app deletion
   - **24-hour cooldown**: Deactivation requires waiting period
   - **You can decline**: App works fully without device admin

### How to Revoke Permissions
You can revoke any permission at any time:
1. Go to Android Settings → Apps → ADHD Focus Alarm → Permissions
2. Toggle off any permission
3. App features requiring that permission will be disabled

---

## Data Sharing

### We DO NOT Share Your Data

**No Third-Party Sharing**: We do not sell, trade, or transfer your data to any third parties.

**No Analytics Services**: We do not use Google Analytics, Facebook SDK, or any user tracking services.

**No Advertising**: This app contains no advertisements and no ad tracking.

### Limited Exception: Crash Reports

The only data that leaves your device is **anonymous crash reports** sent to Firebase Crashlytics:

- **What's sent**: Device model, OS version, crash stack trace
- **What's NOT sent**: Your alarms, photos, barcodes, app usage data, or any personal information
- **Purpose**: To fix bugs and improve app stability
- **Provider**: Google Firebase (Privacy Policy: https://firebase.google.com/support/privacy)

**You can opt out**: Crash reporting can be disabled in Settings.

---

## Data Retention

### Local Data
- **Alarms**: Stored until you delete them
- **App Usage**: Automatically deleted after 30 days
- **Sleep Sessions**: Stored indefinitely (you can manually delete)
- **Mission Data**: Stored until associated alarm is deleted

### Cloud Data
We store **zero data in the cloud**. Everything is local.

---

## Data Deletion

### Partial Deletion
You can delete specific data:
- Individual alarms: Swipe to delete or use menu
- Barcode registrations: Manage in Barcode Settings
- Reference photos: Delete in Mission Settings
- App usage history: Clear in Focus Mode Settings

### Complete Deletion
To delete all app data:

**Option 1**: In-App Reset
1. Go to Settings → Advanced → Reset App
2. Confirm deletion
3. All data permanently erased

**Option 2**: Uninstall App
1. Uninstall ADHD Focus Alarm from your device
2. Android automatically deletes all app data
3. Data is irrecoverable

---

## Children's Privacy

This app is **not directed at children under 13 years of age**.

We do not knowingly collect personal information from children under 13. If you are a parent or guardian and believe your child has provided us with personal information, please contact us so we can delete it.

**Age Requirement**: Users must be 13 years or older.

---

## Changes to This Privacy Policy

We may update this Privacy Policy periodically to reflect:
- Changes in legal requirements
- New features added to the app
- Improvements to data security

**Notification**: We will notify you of significant changes via:
- In-app notification
- Update to this document with new "Last Updated" date

**Your Responsibility**: Please review this policy periodically.

---

## Your Rights

Under applicable data protection laws (including GDPR, CCPA), you have the right to:

1. **Access**: View all data stored by the app (accessible via Settings → Data Export)
2. **Rectification**: Edit or correct your alarms and settings
3. **Erasure**: Delete all data (Settings → Reset App or uninstall)
4. **Portability**: Export data to JSON format (Settings → Backup Data)
5. **Objection**: Disable specific features (e.g., sleep tracking, app blocking)

---

## Contact Us

If you have questions or concerns about this Privacy Policy or our data practices:

**Email**: privacy@alarmfocus.app
**Website**: https://alarmfocus.app/privacy
**Response Time**: Within 7 business days

For data deletion requests, security concerns, or privacy inquiries, please email us with:
- Subject: "Privacy Inquiry"
- Your device model and app version (helps us assist you)

---

## Legal Compliance

This app complies with:
- **GDPR** (General Data Protection Regulation - EU)
- **CCPA** (California Consumer Privacy Act - USA)
- **Google Play Data Safety** requirements
- **Android Privacy Best Practices**

---

## Third-Party Services

### Firebase Crashlytics (Google)
- **Purpose**: Anonymous crash reporting
- **Data Collected**: Device model, OS version, stack traces
- **Privacy Policy**: https://firebase.google.com/support/privacy
- **Opt-Out**: Available in app Settings

### AndroidX Security Library
- **Purpose**: Local data encryption
- **Data Collected**: None (library runs locally)
- **Provider**: Google/Android Open Source Project

### ML Kit (Google)
- **Purpose**: On-device photo comparison and barcode scanning
- **Data Collected**: None (all processing happens locally on your device)
- **Privacy Policy**: https://developers.google.com/ml-kit/terms

**Important**: ML Kit processes images **entirely on your device**. No photos are sent to Google servers.

---

## Transparency Commitment

We believe in radical transparency:

- ✅ **Open Source Friendly**: Our privacy practices are auditable
- ✅ **No Hidden Trackers**: We use no third-party SDKs except Firebase Crashlytics (for crashes only)
- ✅ **No Secret Data Collection**: This policy describes 100% of data we access
- ✅ **No Dark Patterns**: All permissions are optional and clearly explained

**If you find any privacy concern not covered here, please report it to privacy@alarmfocus.app**

---

## Summary (TL;DR)

✅ **All your data stays on your device**
✅ **Sensitive data encrypted with AES-256**
✅ **No selling or sharing of personal data**
✅ **Only anonymous crash reports sent to Google (optional)**
✅ **You can delete all data anytime**
✅ **No ads, no tracking, no analytics**

**Your privacy is our priority. We built this app to help you, not to monetize your data.**

---

**Privacy Policy Version**: 1.0
**Effective Date**: October 4, 2025
**Policy URL**: https://alarmfocus.app/privacy

---

© 2025 ADHD Focus Alarm. All rights reserved.
