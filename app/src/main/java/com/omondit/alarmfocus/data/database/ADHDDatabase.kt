package com.omondit.alarmfocus.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

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