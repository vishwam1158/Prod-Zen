package com.viz.prodzen.workers

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.viz.prodzen.data.local.DailyUsage
import com.viz.prodzen.data.local.UsageDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*

@HiltWorker
class UsageTrackingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageDao: UsageDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "UsageTrackingWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(WORK_NAME, "Starting daily usage tracking.")
            val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            // Get stats for yesterday
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val endTime = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis

            val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

            if (usageStats.isNotEmpty()) {
                val dailyUsageList = usageStats
                    .filter { it.totalTimeInForeground > 0 }
                    .map {
                        DailyUsage(
                            packageName = it.packageName,
                            date = startTime,
                            usageInMillis = it.totalTimeInForeground
                        )
                    }
                usageDao.insertDailyUsage(dailyUsageList)
                Log.d(WORK_NAME, "Successfully inserted ${dailyUsageList.size} daily usage records.")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(WORK_NAME, "Daily usage tracking failed", e)
            Result.failure()
        }
    }
}
