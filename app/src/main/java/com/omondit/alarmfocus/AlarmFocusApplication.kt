package com.omondit.alarmfocus

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Application class for AlarmFocus with Crashlytics integration
 */
class AlarmFocusApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase Crashlytics
        initializeCrashlytics()
    }

    private fun initializeCrashlytics() {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()

            // Enable crash reporting
            crashlytics.setCrashlyticsCollectionEnabled(true)

            // Set custom keys for debugging
            crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)

            // Log application start
            crashlytics.log("Application started")

        } catch (e: Exception) {
            // Crashlytics not available (likely missing google-services.json)
            android.util.Log.w("AlarmFocus", "Crashlytics initialization failed: ${e.message}")
        }
    }

    /**
     * Log non-fatal exception to Crashlytics
     */
    fun logException(throwable: Throwable, message: String? = null) {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            message?.let { crashlytics.log(it) }
            crashlytics.recordException(throwable)
        } catch (e: Exception) {
            android.util.Log.e("AlarmFocus", "Failed to log exception: ${throwable.message}", throwable)
        }
    }

    /**
     * Set user identifier for crash reports
     */
    fun setUserId(userId: String) {
        try {
            FirebaseCrashlytics.getInstance().setUserId(userId)
        } catch (e: Exception) {
            android.util.Log.w("AlarmFocus", "Failed to set user ID")
        }
    }

    /**
     * Log custom event to Crashlytics
     */
    fun logEvent(event: String, details: Map<String, String> = emptyMap()) {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log("Event: $event")
            details.forEach { (key, value) ->
                crashlytics.setCustomKey(key, value)
            }
        } catch (e: Exception) {
            android.util.Log.w("AlarmFocus", "Failed to log event: $event")
        }
    }
}
