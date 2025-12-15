package com.viz.prodzen.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.viz.prodzen.data.local.UsageDao
import com.viz.prodzen.data.repository.UserStatsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*

@HiltWorker
class GoalCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageDao: UsageDao,
    private val userStatsRepository: UserStatsRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "GoalCheckWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(WORK_NAME, "Checking daily goal achievement")

            // Get today's start time
            val today = getTodayStartMillis()

            // Get today's total usage
            val todayUsage = usageDao.getUsageSince(today)
            val totalMinutes = todayUsage.sumOf { it.usageInMillis } / (60 * 1000)

            // Get user's daily goal
            val stats = userStatsRepository.getUserStats()
            val goalMinutes = stats.dailyGoalMinutes

            // Check if goal was met
            val goalMet = totalMinutes <= goalMinutes

            Log.d(WORK_NAME, "Usage: ${totalMinutes}m, Goal: ${goalMinutes}m, Met: $goalMet")

            // Update streak
            userStatsRepository.updateStreak(goalMet)

            // Add points if goal was met
            if (goalMet) {
                userStatsRepository.addPoints(100) // 100 points for meeting daily goal
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(WORK_NAME, "Goal check failed", e)
            Result.failure()
        }
    }

    private fun getTodayStartMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

