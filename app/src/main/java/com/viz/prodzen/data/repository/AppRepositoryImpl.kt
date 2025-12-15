package com.viz.prodzen.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.viz.prodzen.data.local.AppDao
import com.viz.prodzen.data.local.DailyUsage
import com.viz.prodzen.data.local.UsageDao
import com.viz.prodzen.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val appDao: AppDao,
    private val usageDao: UsageDao,
    private val context: Context,
    private val categoryRepository: CategoryRepository
) : AppRepository {

    private val logTag = "ProdZenRepo"
    private var hasAutoCategorized = false

    override suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val savedApps = appDao.getAllApps().associateBy { it.packageName }
        val usageStatsMap = getTodayUsageStats()

        val apps = pm.queryIntentActivities(mainIntent, 0).mapNotNull { resolveInfo ->
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

        // Auto-categorize apps on first load
        if (!hasAutoCategorized && apps.isNotEmpty()) {
            try {
                categoryRepository.autoCategorizeApps(apps)
                hasAutoCategorized = true
                Log.d(logTag, "Auto-categorized ${apps.size} apps")
            } catch (e: Exception) {
                Log.e(logTag, "Error auto-categorizing apps", e)
            }
        }

        return@withContext apps
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

    // NEW: A single, safe function to handle all updates.
    override suspend fun updateAppSettings(appInfo: AppInfo) {
        withContext(Dispatchers.IO) {
            val currentState = appDao.getAppByPackageName(appInfo.packageName)
            val appToSave = (currentState ?: appInfo).copy(
                isTracked = appInfo.isTracked,
                timeLimitMinutes = appInfo.timeLimitMinutes,
                hasIntention = appInfo.hasIntention
            ).copy(usageTodayMillis = 0, icon = null)

            Log.d(logTag, "[SAVE] Saving for ${appToSave.packageName}: isTracked=${appToSave.isTracked}, hasIntention=${appToSave.hasIntention}, limit=${appToSave.timeLimitMinutes}")
            appDao.insertOrUpdateApp(appToSave)
        }
    }

    override suspend fun getAppByPackageName(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        val appInfo = appDao.getAppByPackageName(packageName)
        Log.d(logTag, "[READ] Reading settings for $packageName. Found: ${appInfo != null}")
        return@withContext appInfo
    }

    // Historical usage data methods
    override suspend fun getUsageSince(startDate: Long): List<DailyUsage> = withContext(Dispatchers.IO) {
        return@withContext usageDao.getUsageSince(startDate)
    }

    override suspend fun getAppUsageSince(packageName: String, startDate: Long): List<DailyUsage> = withContext(Dispatchers.IO) {
        return@withContext usageDao.getAppUsageSince(packageName, startDate)
    }

    override fun getUsageSinceFlow(startDate: Long): Flow<List<DailyUsage>> {
        return usageDao.getUsageSinceFlow(startDate)
    }
}
