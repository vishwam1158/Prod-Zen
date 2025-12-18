package com.viz.prodzen.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.viz.prodzen.data.model.AppInfo
import com.viz.prodzen.data.local.entities.UserStats
import com.viz.prodzen.data.local.entities.FocusSession
import com.viz.prodzen.data.local.entities.Achievement
import com.viz.prodzen.data.local.entities.AppCategory
import com.viz.prodzen.data.local.entities.AppCategoryMapping
import com.viz.prodzen.data.local.entities.HourlyAppUsage

@Database(
    entities = [
        AppInfo::class,
        DailyUsage::class,
        UserStats::class,
        FocusSession::class,
        Achievement::class,
        AppCategory::class,
        AppCategoryMapping::class,
        HourlyAppUsage::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun usageDao(): UsageDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun appCategoryDao(): AppCategoryDao
    abstract fun appCategoryMappingDao(): AppCategoryMappingDao
    abstract fun hourlyUsageDao(): HourlyUsageDao
}