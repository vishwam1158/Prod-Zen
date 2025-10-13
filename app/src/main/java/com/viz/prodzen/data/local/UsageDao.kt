package com.viz.prodzen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyUsage(dailyUsage: List<DailyUsage>)

    @Query("SELECT * FROM daily_usage WHERE date >= :startDate")
    suspend fun getUsageSince(startDate: Long): List<DailyUsage>
}
