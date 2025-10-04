package com.omondit.alarmfocus.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omondit.alarmfocus.data.database.entities.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appUsage: AppUsageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appUsages: List<AppUsageEntity>)

    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY usageTimeMillis DESC")
    fun getUsageByDate(date: String): Flow<List<AppUsageEntity>>

    @Query(
        """
        SELECT * FROM app_usage
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, usageTimeMillis DESC
    """
    )
    fun getUsageByDateRange(startDate: String, endDate: String): Flow<List<AppUsageEntity>>

    @Query(
        """
        SELECT SUM(usageTimeMillis) as totalTime
        FROM app_usage
        WHERE date = :date AND category = :category
    """
    )
    suspend fun getTotalUsageByCategory(date: String, category: AppUsageEntity.AppCategory): Long?

    @Query(
        """
        SELECT * FROM app_usage
        WHERE date = :date AND category = :category
        ORDER BY usageTimeMillis DESC
    """
    )
    fun getUsageByCategory(date: String, category: AppUsageEntity.AppCategory): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName AND date = :date")
    suspend fun getAppUsageForDate(packageName: String, date: String): AppUsageEntity?

    @Query("DELETE FROM app_usage WHERE date < :beforeDate")
    suspend fun deleteOldData(beforeDate: String)

    @Query(
        """
        SELECT category, SUM(usageTimeMillis) as totalTime
        FROM app_usage
        WHERE date = :date
        GROUP BY category
        ORDER BY totalTime DESC
    """
    )
    suspend fun getCategoryUsageForDate(date: String): List<CategoryUsage>

    data class CategoryUsage(
        val category: AppUsageEntity.AppCategory,
        val totalTime: Long
    )
}
