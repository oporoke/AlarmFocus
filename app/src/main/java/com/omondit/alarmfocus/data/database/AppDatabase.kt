package com.omondit.alarmfocus.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.omondit.alarmfocus.data.database.dao.AlarmDao
import com.omondit.alarmfocus.data.database.entities.AlarmEntity


@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

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
