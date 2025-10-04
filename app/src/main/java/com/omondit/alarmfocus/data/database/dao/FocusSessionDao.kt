package com.omondit.alarmfocus.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omondit.alarmfocus.data.database.entities.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity): Long

    @Update
    suspend fun update(session: FocusSessionEntity)

    @Delete
    suspend fun delete(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): FocusSessionEntity?

    @Query("UPDATE focus_sessions SET isActive = 0")
    suspend fun deactivateAllSessions()

    @Query("UPDATE focus_sessions SET isActive = 1 WHERE id = :id")
    suspend fun activateSession(id: Long)

    @Query("SELECT * FROM focus_sessions WHERE startTime IS NOT NULL AND isActive = 0")
    fun getScheduledSessions(): Flow<List<FocusSessionEntity>>
}
