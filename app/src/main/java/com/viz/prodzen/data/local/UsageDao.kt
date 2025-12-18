package com.viz.prodzen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyUsage(dailyUsage: List<DailyUsage>)

    @Query("SELECT * FROM daily_usage WHERE date >= :startDate ORDER BY date DESC")
    suspend fun getUsageSince(startDate: Long): List<DailyUsage>

    @Query("SELECT * FROM daily_usage WHERE packageName = :packageName AND date >= :startDate ORDER BY date DESC")
    suspend fun getAppUsageSince(packageName: String, startDate: Long): List<DailyUsage>

    @Query("SELECT * FROM daily_usage WHERE date >= :startDate ORDER BY date DESC")
    fun getUsageSinceFlow(startDate: Long): Flow<List<DailyUsage>>

    @Query("""
        UPDATE daily_usage 
        SET openCount = openCount + 1 
        WHERE packageName = :packageName AND date = :date
    """)
    suspend fun incrementOpenCount(packageName: String, date: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyUsageEntry(dailyUsage: DailyUsage): Long
}
