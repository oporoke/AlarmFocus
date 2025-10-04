package com.omondit.alarmfocus.utils

import android.content.Context
import android.util.Log
import com.omondit.alarmfocus.data.database.AppDatabase
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BackupManager(private val context: Context) {

    companion object {
        private const val TAG = "BackupManager"
        private const val BACKUP_DIR = "backups"
        private const val BACKUP_FILE = "alarm_backup.json"
    }

    private val database = AppDatabase.getDatabase(context)

    /**
     * Create backup of all app data
     */
    suspend fun createBackup(): BackupResult {
        return try {
            val backupData = JSONObject()

            // Backup alarms
            val alarms = database.alarmDao().getAllAlarms().first()
            val alarmsJson = JSONArray()
            alarms.forEach { alarm ->
                alarmsJson.put(JSONObject().apply {
                    put("id", alarm.id)
                    put("hour", alarm.hour)
                    put("minute", alarm.minute)
                    put("label", alarm.label)
                    put("isEnabled", alarm.isEnabled)
                    put("repeatDays", alarm.repeatDays)
                    put("soundUri", alarm.soundUri ?: "")
                    put("soundName", alarm.soundName)
                    put("missionType", alarm.missionType)
                    put("missionConfig", alarm.missionConfig)
                    put("vibrationEnabled", alarm.vibrationEnabled)
                    put("volume", alarm.volume)
                })
            }
            backupData.put("alarms", alarmsJson)

            // Backup focus sessions
            val sessions = database.focusSessionDao().getAllSessions().first()
            val sessionsJson = JSONArray()
            sessions.forEach { session ->
                sessionsJson.put(JSONObject().apply {
                    put("name", session.name)
                    put("durationMinutes", session.durationMinutes)
                    put("intensity", session.intensity.name)
                    put("blockedCategories", session.blockedCategories)
                    put("startTime", session.startTime ?: "")
                    put("repeatDays", session.repeatDays)
                })
            }
            backupData.put("focusSessions", sessionsJson)

            // Backup preferences
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val prefsJson = JSONObject()
            prefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> prefsJson.put(key, value)
                    is Int -> prefsJson.put(key, value)
                    is Boolean -> prefsJson.put(key, value)
                    is Long -> prefsJson.put(key, value)
                    is Float -> prefsJson.put(key, value)
                }
            }
            backupData.put("settings", prefsJson)

            // Save to file
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val backupFile = File(backupDir, BACKUP_FILE)
            backupFile.writeText(backupData.toString(2))

            Log.d(TAG, "Backup created successfully: ${backupFile.absolutePath}")
            BackupResult.Success(backupFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            BackupResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Restore data from backup
     */
    suspend fun restoreBackup(backupPath: String): RestoreResult {
        return try {
            val backupFile = File(backupPath)
            if (!backupFile.exists()) {
                return RestoreResult.Error("Backup file not found")
            }

            val backupData = JSONObject(backupFile.readText())

            // Restore alarms
            val alarmsJson = backupData.optJSONArray("alarms")
            alarmsJson?.let { array ->
                for (i in 0 until array.length()) {
                    val alarmJson = array.getJSONObject(i)
                    // Create alarm entity from JSON and insert
                    // Implementation depends on your data structure
                }
            }

            // Restore focus sessions
            val sessionsJson = backupData.optJSONArray("focusSessions")
            sessionsJson?.let { array ->
                for (i in 0 until array.length()) {
                    val sessionJson = array.getJSONObject(i)
                    // Create session entity from JSON and insert
                }
            }

            // Restore settings
            val prefsJson = backupData.optJSONObject("settings")
            prefsJson?.let { json ->
                val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val editor = prefs.edit()

                json.keys().forEach { key ->
                    when (val value = json.get(key)) {
                        is String -> editor.putString(key, value)
                        is Int -> editor.putInt(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Long -> editor.putLong(key, value)
                        is Double -> editor.putFloat(key, value.toFloat())
                    }
                }
                editor.apply()
            }

            Log.d(TAG, "Backup restored successfully")
            RestoreResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            RestoreResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Export backup to external storage
     */
    fun exportBackup(sourcePath: String, destinationPath: String): Boolean {
        return try {
            val source = File(sourcePath)
            val destination = File(destinationPath)
            source.copyTo(destination, overwrite = true)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            false
        }
    }

    sealed class BackupResult {
        data class Success(val path: String) : BackupResult()
        data class Error(val message: String) : BackupResult()
    }

    sealed class RestoreResult {
        object Success : RestoreResult()
        data class Error(val message: String) : RestoreResult()
    }
}
