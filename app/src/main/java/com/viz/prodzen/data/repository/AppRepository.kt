package com.viz.prodzen.data.repository

import com.viz.prodzen.data.local.DailyUsage
import com.viz.prodzen.data.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun getInstalledApps(): List<AppInfo>
    suspend fun getAppByPackageName(packageName: String): AppInfo?
    suspend fun getAppUsageToday(packageName: String): Long

    // NEW: A single, safe function to update any setting for an app.
    suspend fun updateAppSettings(appInfo: AppInfo)

    // Historical usage data
    suspend fun getUsageSince(startDate: Long): List<DailyUsage>
    suspend fun getAppUsageSince(packageName: String, startDate: Long): List<DailyUsage>
    fun getUsageSinceFlow(startDate: Long): Flow<List<DailyUsage>>
}