package com.viz.prodzen.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.viz.prodzen.data.local.AppDao
import com.viz.prodzen.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val appDao: AppDao,
    private val context: Context
) : AppRepository {

    private val logTag = "ProdZenRepo"

    override suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val savedApps = appDao.getAllApps().associateBy { it.packageName }
        val usageStatsMap = getTodayUsageStats()

        return@withContext pm.queryIntentActivities(mainIntent, 0).mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val appName = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)

            val savedAppInfo = savedApps[packageName]
            val isTracked = savedAppInfo?.isTracked ?: false
            val timeLimit = savedAppInfo?.timeLimitMinutes ?: 0
            val hasIntention = savedAppInfo?.hasIntention ?: false
            val usage = usageStatsMap[packageName] ?: 0L

            AppInfo(packageName, appName, isTracked, timeLimit, hasIntention, icon, usage)
        }.sortedBy { it.appName }
    }

    private fun getTodayUsageStats(): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            .associate { it.packageName to it.totalTimeInForeground }
    }

    override suspend fun getAppUsageToday(packageName: String): Long = withContext(Dispatchers.IO) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        return@withContext usageStats.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
    }

    override suspend fun updateTrackedApp(appInfo: AppInfo) {
        withContext(Dispatchers.IO) {
            // FIXED: This new logic prevents race conditions.
            // 1. Fetch the most recent version of the app's settings from the database.
            val currentState = appDao.getAppByPackageName(appInfo.packageName)

            // 2. Create the updated object, preserving existing values and applying the new ones.
            val appToSave = if (currentState != null) {
                // If settings exist, merge them with the incoming changes.
                currentState.copy(
                    isTracked = appInfo.isTracked,
                    timeLimitMinutes = appInfo.timeLimitMinutes,
                    hasIntention = appInfo.hasIntention
                )
            } else {
                // If it's the first time we're saving this app, use the incoming info.
                appInfo
            }.copy(usageTodayMillis = 0, icon = null) // Always strip transient data before saving.

            Log.d(logTag, "[SAVE] Saving settings for ${appToSave.packageName}: isTracked=${appToSave.isTracked}, hasIntention=${appToSave.hasIntention}, limit=${appToSave.timeLimitMinutes}")
            appDao.insertOrUpdateApp(appToSave)
        }
    }

    override fun getTrackedApps(): Flow<List<AppInfo>> {
        return appDao.getTrackedApps()
    }

    override suspend fun getAppByPackageName(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        val appInfo = appDao.getAppByPackageName(packageName)
        Log.d(logTag, "[READ] Reading settings for $packageName. Found: ${appInfo != null}")
        return@withContext appInfo
    }
}

