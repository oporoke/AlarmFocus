package com.omondit.alarmfocus.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omondit.alarmfocus.data.database.entities.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedApp: BlockedAppEntity): Long

    @Update
    suspend fun update(blockedApp: BlockedAppEntity)

    @Delete
    suspend fun delete(blockedApp: BlockedAppEntity)

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getAllBlocked(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): BlockedAppEntity?

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1 AND (blockUntil IS NULL OR blockUntil > :currentTime)")
    fun getActivelyBlocked(currentTime: Long = System.currentTimeMillis()): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE blockType = :blockType AND isBlocked = 1")
    fun getBlockedByType(blockType: BlockedAppEntity.BlockType): Flow<List<BlockedAppEntity>>

    @Query("UPDATE blocked_apps SET isBlocked = 0 WHERE blockUntil <= :currentTime")
    suspend fun expireTimedBlocks(currentTime: Long = System.currentTimeMillis())

    @Query("UPDATE blocked_apps SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun setBlockStatus(packageName: String, isBlocked: Boolean)

    @Query("DELETE FROM blocked_apps WHERE blockType = :blockType")
    suspend fun deleteByType(blockType: BlockedAppEntity.BlockType)
}
