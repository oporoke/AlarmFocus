package com.omondit.alarmfocus

import android.app.Application
import android.util.Log
import com.omondit.alarmfocus.di.AppModule


/**
 * Application class for AlarmFocus
 */
class AlarmFocusApplication : Application() {

    lateinit var appModule: AppModule
        private set

    override fun onCreate() {
        super.onCreate()
        appModule = AppModule(this)
        Log.d(TAG, "AlarmFocus application started")
    }

    /**
     * Log non-fatal exception
     */
    fun logException(throwable: Throwable, message: String? = null) {
        Log.e(TAG, message ?: "Exception occurred", throwable)
    }

    /**
     * Set user identifier for logging
     */
    fun setUserId(userId: String) {
        Log.d(TAG, "User ID set: $userId")
    }

    /**
     * Log custom event
     */
    fun logEvent(event: String, details: Map<String, String> = emptyMap()) {
        Log.d(TAG, "Event: $event, Details: $details")
    }

    companion object {
        private const val TAG = "AlarmFocus"
    }
}
