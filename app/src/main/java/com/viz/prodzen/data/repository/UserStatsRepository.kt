package com.viz.prodzen.data.repository

import com.viz.prodzen.data.local.UserStatsDao
import com.viz.prodzen.data.local.entities.UserStats
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStatsRepository @Inject constructor(
    private val userStatsDao: UserStatsDao
) {
    suspend fun getUserStats(): UserStats {
        return userStatsDao.getUserStats() ?: UserStats()
    }

    fun getUserStatsFlow(): Flow<UserStats?> {
        return userStatsDao.getUserStatsFlow()
    }

    suspend fun updateStreak(goalMet: Boolean) {
        val stats = getUserStats()
        val today = getTodayStartMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val daysSinceLastCheck = (today - stats.lastGoalCheckDate) / oneDayMillis

        val updatedStats = if (goalMet) {
            // Increment streak
            val newStreak = if (daysSinceLastCheck <= 1) {
                stats.currentStreak + 1
            } else {
                1 // Reset if skipped a day
            }
            stats.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(newStreak, stats.longestStreak),
                lastGoalCheckDate = today
            )
        } else {
            // Reset streak
            stats.copy(
                currentStreak = 0,
                lastGoalCheckDate = today
            )
        }

        userStatsDao.updateUserStats(updatedStats)
    }

    suspend fun addPoints(points: Int) {
        val stats = getUserStats()
        userStatsDao.updateUserStats(stats.copy(totalPoints = stats.totalPoints + points))
    }

    suspend fun setDailyGoal(minutes: Int) {
        val stats = getUserStats()
        userStatsDao.updateUserStats(stats.copy(dailyGoalMinutes = minutes))
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

