package com.omondit.alarmfocus.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.omondit.alarmfocus.data.database.dao.AlarmDao
import com.omondit.alarmfocus.data.database.dao.AppUsageDao
import com.omondit.alarmfocus.data.database.dao.BlockedAppDao
import com.omondit.alarmfocus.data.database.dao.FocusSessionDao
import com.omondit.alarmfocus.data.database.dao.SleepSessionDao
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.data.database.entities.AppUsageEntity
import com.omondit.alarmfocus.data.database.entities.BlockedAppEntity
import com.omondit.alarmfocus.data.database.entities.FocusSessionEntity
import com.omondit.alarmfocus.data.database.entities.SleepSessionEntity

@Database(
    entities = [
        AlarmEntity::class,
        AppUsageEntity::class,
        BlockedAppEntity::class,
        FocusSessionEntity::class,
        SleepSessionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun sleepSessionDao(): SleepSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "adhd_alarm_database"
                )
                    .fallbackToDestructiveMigration(false) // For development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
