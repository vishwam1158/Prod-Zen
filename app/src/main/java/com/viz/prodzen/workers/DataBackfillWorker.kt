package com.viz.prodzen.workers

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.viz.prodzen.data.local.DailyUsage
import com.viz.prodzen.data.local.UsageDao
import com.viz.prodzen.util.PreferenceManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*

@HiltWorker
class DataBackfillWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageDao: UsageDao,
    private val preferenceManager: PreferenceManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "DataBackfillWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(WORK_NAME, "Starting historical data backfill.")
            val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val allUsageRecords = mutableListOf<DailyUsage>()

            // Backfill data for the last 30 days
            for (i in 1..30) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
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
                    allUsageRecords.addAll(dailyUsageList)
                }
            }

            if (allUsageRecords.isNotEmpty()) {
                usageDao.insertDailyUsage(allUsageRecords)
                Log.d(WORK_NAME, "Successfully inserted ${allUsageRecords.size} historical records.")
            }

            // Mark the backfill as complete to prevent it from running again.
            preferenceManager.setDataBackfilled(true)
            Result.success()
        } catch (e: Exception) {
            Log.e(WORK_NAME, "Data backfill failed", e)
            Result.failure()
        }
    }
}