package com.omondit.alarmfocus.utils

import android.content.Context
import android.util.Log
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.database.entities.AppUsageEntity
import com.omondit.alarmfocus.data.database.entities.FocusSessionEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

class FocusModeManager(private val context: Context) {

    companion object {
        private const val TAG = "FocusModeManager"
    }

    private val database = AppDatabase.getDatabase(context)
    private val focusSessionDao = database.focusSessionDao()
    private val appBlockManager = AppBlockManager(context)

    /**
     * Start a focus session
     */
    suspend fun startFocusSession(sessionId: Long) {
        val session = focusSessionDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session not found")

        // Deactivate any active sessions
        focusSessionDao.deactivateAllSessions()

        // Activate this session
        focusSessionDao.activateSession(sessionId)

        // Get apps to block based on intensity and categories
        val appsToBlock = getAppsToBlock(session)

        // Start blocking
        appBlockManager.startFocusSessionBlocking(appsToBlock, session.durationMinutes)

        Log.d(TAG, "Started focus session: ${session.name} for ${session.durationMinutes} minutes")
    }

    /**
     * Stop current focus session
     */
    suspend fun stopFocusSession() {
        val activeSession = focusSessionDao.getActiveSession()
        if (activeSession != null) {
            focusSessionDao.deactivateAllSessions()
            appBlockManager.stopFocusSessionBlocking()
            Log.d(TAG, "Stopped focus session: ${activeSession.name}")
        }
    }

    /**
     * Create a new focus session
     */
    suspend fun createSession(session: FocusSessionEntity): Long {
        return focusSessionDao.insert(session)
    }

    /**
     * Get all focus sessions
     */
    fun getAllSessions(): Flow<List<FocusSessionEntity>> {
        return focusSessionDao.getAllSessions()
    }

    /**
     * Get active focus session
     */
    suspend fun getActiveSession(): FocusSessionEntity? {
        return focusSessionDao.getActiveSession()
    }

    /**
     * Delete a session
     */
    suspend fun deleteSession(session: FocusSessionEntity) {
        focusSessionDao.delete(session)
    }

    /**
     * Update a session
     */
    suspend fun updateSession(session: FocusSessionEntity) {
        focusSessionDao.update(session)
    }

    /**
     * Get apps to block based on session configuration
     */
    private fun getAppsToBlock(session: FocusSessionEntity): List<String> {
        val categories = try {
            val jsonArray = JSONArray(session.blockedCategories)
            List(jsonArray.length()) { i ->
                AppUsageEntity.AppCategory.valueOf(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing blocked categories", e)
            emptyList()
        }

        // Get all installed apps and filter by category
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(0)

        return installedApps.mapNotNull { appInfo ->
            val packageName = appInfo.packageName
            val category = AppUsageEntity.categorizeApp(packageName)

            if (categories.contains(category)) {
                packageName
            } else null
        }
    }

    /**
     * Get default templates
     */
    fun getDefaultTemplates(): List<FocusSessionEntity> {
        return FocusSessionEntity.TemplateType.values().map { type ->
            FocusSessionEntity.getDefaultTemplate(type)
        }
    }
}
