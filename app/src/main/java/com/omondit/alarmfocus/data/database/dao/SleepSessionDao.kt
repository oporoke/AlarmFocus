package com.omondit.alarmfocus.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omondit.alarmfocus.data.database.entities.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SleepSessionEntity): Long

    @Update
    suspend fun update(session: SleepSessionEntity)

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SleepSessionEntity?

    @Query("SELECT * FROM sleep_sessions WHERE date = :date")
    suspend fun getSessionByDate(date: String): SleepSessionEntity?

    @Query("SELECT * FROM sleep_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSleepSession(): SleepSessionEntity?

    @Query("SELECT * FROM sleep_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSessionsByDateRange(startDate: String, endDate: String): Flow<List<SleepSessionEntity>>

    @Query("SELECT AVG(qualityScore) FROM sleep_sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageQuality(startDate: String, endDate: String): Float?

    @Query("SELECT AVG(durationMinutes) FROM sleep_sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageDuration(startDate: String, endDate: String): Float?

    @Query("SELECT * FROM sleep_sessions ORDER BY date DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 7): Flow<List<SleepSessionEntity>>

    @Query("DELETE FROM sleep_sessions WHERE date < :beforeDate")
    suspend fun deleteOldSessions(beforeDate: String)
}
