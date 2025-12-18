package com.viz.prodzen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.viz.prodzen.data.local.entities.HourlyAppUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface HourlyUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(usage: HourlyAppUsage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usages: List<HourlyAppUsage>)

    @Query("SELECT * FROM hourly_app_usage WHERE hourTimestamp >= :startTime AND hourTimestamp < :endTime")
    fun getUsageBetween(startTime: Long, endTime: Long): Flow<List<HourlyAppUsage>>

    @Query("SELECT * FROM hourly_app_usage WHERE packageName = :packageName AND hourTimestamp >= :startTime AND hourTimestamp < :endTime ORDER BY hourTimestamp ASC")
    fun getAppUsageBetween(packageName: String, startTime: Long, endTime: Long): Flow<List<HourlyAppUsage>>

    @Query("DELETE FROM hourly_app_usage WHERE hourTimestamp < :threshold")
    suspend fun deleteOldUsage(threshold: Long)
}

