package com.viz.prodzen.workers
/*

import android.app.usage.UsageStatsManager
import android.content.Context
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
            val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            // Get stats for yesterday
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val endTime = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            val startTime = calendar.timeInMillis

            val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

            if (usageStats.isNotEmpty()) {
                val dailyUsageList = usageStats.map {
                    DailyUsage(
                        packageName = it.packageName,
                        date = startTime,
                        usageInMillis = it.totalTimeInForeground
                    )
                }
                usageDao.insertDailyUsage(dailyUsageList)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
*/
