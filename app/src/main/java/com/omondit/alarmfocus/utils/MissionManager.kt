package com.omondit.alarmfocus.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.omondit.alarmfocus.domain.model.*
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.presentation.MissionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages mission states and coordinates between alarm service and mission UI
 */
class MissionManager(
    private val context: Context,
    private val alarmRepository: AlarmRepository
) {
    companion object {
        private const val TAG = "MissionManager"
        private const val PREFS_NAME = "mission_manager"
        private const val KEY_ACTIVE_MISSION = "active_mission_id"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var activeMissionSession: MissionSession? = null

    /**
     * Start a mission for the given alarm
     */
    suspend fun startMission(alarmId: Long): Boolean {
        return try {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm == null) {
                Log.e(TAG, "Alarm not found: $alarmId")
                return false
            }

            val missionConfig = MissionConfig.fromJson(alarm.missionConfig)

            // If no mission configured, allow immediate dismissal
            if (missionConfig.type == Mission.MissionType.NONE) {
                Log.d(TAG, "No mission configured for alarm $alarmId")
                return completeMission(alarmId, MissionResult(true, 0, 0, 1.0f))
            }

            val mission = MissionFactory.createMission(missionConfig, context)
            activeMissionSession = MissionSession(alarmId, mission)

            // Save active mission state
            prefs.edit().putLong(KEY_ACTIVE_MISSION, alarmId).apply()

            // Launch mission activity
            val missionIntent = Intent(context, MissionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("alarm_id", alarmId)
                putExtra("mission_config", missionConfig.toJson())
            }

            context.startActivity(missionIntent)

            Log.d(TAG, "Mission started for alarm $alarmId, type: ${missionConfig.type}")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start mission for alarm $alarmId", e)
            false
        }
    }

    /**
     * Complete a mission with the given result
     */
    suspend fun completeMission(alarmId: Long, result: MissionResult): Boolean {
        return try {
            if (result.success) {
                // Mark alarm as successfully dismissed
                alarmRepository.markAlarmDismissed(alarmId)

                // Log successful wake-up
                logMissionResult(alarmId, result)

                // Stop alarm service
                val stopIntent = Intent(context, com.omondit.alarmfocus.services.AlarmService::class.java).apply {
                    action = com.omondit.alarmfocus.services.AlarmService.ACTION_STOP_ALARM
                    putExtra(com.omondit.alarmfocus.services.AlarmService.EXTRA_ALARM_ID, alarmId)
                }
                context.startService(stopIntent)

                Log.i(TAG, "Mission completed successfully for alarm $alarmId. Score: ${result.score}")
            } else {
                Log.w(TAG, "Mission failed for alarm $alarmId")
                // Alarm continues ringing
            }

            // Clear active mission
            activeMissionSession = null
            prefs.edit().remove(KEY_ACTIVE_MISSION).apply()

            result.success

        } catch (e: Exception) {
            Log.e(TAG, "Error completing mission for alarm $alarmId", e)
            false
        }
    }

    /**
     * Fail a mission (emergency dismiss or timeout)
     */
    suspend fun failMission(alarmId: Long, reason: String = "Failed") {
        Log.w(TAG, "Mission failed for alarm $alarmId: $reason")

        val result = MissionResult(
            success = false,
            completionTime = System.currentTimeMillis() - (activeMissionSession?.startTime ?: 0),
            attempts = activeMissionSession?.attempts ?: 0,
            score = 0f
        )

        logMissionResult(alarmId, result)

        // For failed missions, we might want to keep alarm ringing or show alternatives
        // For now, we'll allow dismissal but log it as a failure
        completeMission(alarmId, result.copy(success = true))
    }

    /**
     * Check if there's an active mission
     */
    fun hasActiveMission(): Boolean {
        val activeMissionId = prefs.getLong(KEY_ACTIVE_MISSION, -1)
        return activeMissionId != -1L && activeMissionSession != null
    }

    /**
     * Get the current active mission session
     */
    fun getActiveMissionSession(): MissionSession? = activeMissionSession

    /**
     * Handle mission timeout
     */
    suspend fun handleMissionTimeout(alarmId: Long) {
        Log.w(TAG, "Mission timeout for alarm $alarmId")
        failMission(alarmId, "Timeout")
    }

    /**
     * Restore mission state after app restart
     */
    suspend fun restoreMissionState() {
        val activeMissionId = prefs.getLong(KEY_ACTIVE_MISSION, -1)
        if (activeMissionId != -1L) {
            try {
                val alarm = alarmRepository.getAlarmById(activeMissionId)
                if (alarm != null && alarm.isActive) {
                    Log.d(TAG, "Restoring mission state for alarm $activeMissionId")
                    startMission(activeMissionId)
                } else {
                    // Clean up stale mission state
                    prefs.edit().remove(KEY_ACTIVE_MISSION).apply()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring mission state", e)
                prefs.edit().remove(KEY_ACTIVE_MISSION).apply()
            }
        }
    }

    /**
     * Get mission statistics for an alarm
     */
    suspend fun getMissionStats(alarmId: Long): MissionStats {
        // This would typically query a mission results database
        // For now, return basic stats from the alarm entity
        val alarm = alarmRepository.getAlarmById(alarmId)
        return if (alarm != null) {
            MissionStats(
                totalAttempts = alarm.snoozeCount + alarm.successfulWakeups,
                successfulCompletions = alarm.successfulWakeups,
                averageCompletionTime = 0L, // Would be calculated from stored results
                bestScore = 0f // Would be calculated from stored results
            )
        } else {
            MissionStats()
        }
    }

    private fun logMissionResult(alarmId: Long, result: MissionResult) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Log to diagnostic system
                val diagnostics = AlarmDiagnostics(context, alarmRepository)
                val resultDetails = "Success: ${result.success}, " +
                    "Time: ${result.completionTime}ms, " +
                    "Attempts: ${result.attempts}, " +
                    "Score: ${result.score}"

                diagnostics.logEvent(
                    if (result.success) "MISSION_COMPLETED" else "MISSION_FAILED",
                    "Alarm $alarmId - $resultDetails"
                )

                Log.d(TAG, "Mission result logged for alarm $alarmId")

            } catch (e: Exception) {
                Log.e(TAG, "Error logging mission result", e)
            }
        }
    }

    data class MissionStats(
        val totalAttempts: Int = 0,
        val successfulCompletions: Int = 0,
        val averageCompletionTime: Long = 0L,
        val bestScore: Float = 0f
    ) {
        val successRate: Float get() = if (totalAttempts > 0) successfulCompletions.toFloat() / totalAttempts else 0f
    }
}

/**
 * Mission Activity that hosts the mission UI
 */
// This would be in a separate file: MissionActivity.kt
/*
package com.omondit.alarmfocus.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.repository.AlarmRepositoryImpl
import com.omondit.alarmfocus.domain.model.MissionConfig
import com.omondit.alarmfocus.presentation.theme.AlarmFocusTheme
import com.omondit.alarmfocus.presentation.ui.screens.MissionScreen
import com.omondit.alarmfocus.utils.MissionManager

class MissionActivity : ComponentActivity() {

    private lateinit var missionManager: MissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dependencies
        val database = AppDatabase.getDatabase(this)
        val repository = AlarmRepositoryImpl(database.alarmDao())
        missionManager = MissionManager(this, repository)

        val alarmId = intent.getLongExtra("alarm_id", -1L)
        val missionConfigJson = intent.getStringExtra("mission_config") ?: "{}"
        val missionConfig = MissionConfig.fromJson(missionConfigJson)

        if (alarmId == -1L) {
            finish()
            return
        }

        setContent {
            AlarmFocusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MissionScreen(
                        alarmId = alarmId,
                        missionConfig = missionConfig,
                        onMissionCompleted = { result ->
                            lifecycleScope.launch {
                                missionManager.completeMission(alarmId, result)
                                finish()
                            }
                        },
                        onMissionFailed = {
                            lifecycleScope.launch {
                                missionManager.failMission(alarmId, "User dismissed")
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        // Prevent back button from dismissing mission
        // Only allow emergency dismiss button
    }
}
*/
