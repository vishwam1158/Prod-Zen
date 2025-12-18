package com.viz.prodzen

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.viz.prodzen.util.PreferenceManager
import com.viz.prodzen.workers.DataBackfillWorker
import com.viz.prodzen.workers.GoalCheckWorker
import com.viz.prodzen.workers.UsageTrackingWorker
import com.viz.prodzen.data.repository.CategoryRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ProdZenApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var categoryRepository: CategoryRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        initializeApp()
        scheduleWorkers()
    }

    private fun initializeApp() {
        applicationScope.launch {
            // Initialize default categories on first launch
            if (!preferenceManager.areCategoriesInitialized()) {
                categoryRepository.initializeDefaultCategories()
                preferenceManager.setCategoriesInitialized(true)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun scheduleWorkers() {
        val workManager = WorkManager.getInstance(this)

        // Schedule daily usage tracking worker (runs once per day at midnight)
        val dailyConstraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<UsageTrackingWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(dailyConstraints)
            .setInitialDelay(calculateDelayUntilMidnight(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            UsageTrackingWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )

        // Schedule goal check worker (runs once per day at 11:59 PM)
        val goalCheckRequest = PeriodicWorkRequestBuilder<GoalCheckWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(dailyConstraints)
            .setInitialDelay(calculateDelayUntilEndOfDay(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            GoalCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            goalCheckRequest
        )

        // Schedule one-time backfill if not already done
        if (!preferenceManager.isDataBackfilled()) {
            val backfillConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val backfillWorkRequest = OneTimeWorkRequestBuilder<DataBackfillWorker>()
                .setConstraints(backfillConstraints)
                .build()

            workManager.enqueueUniqueWork(
                DataBackfillWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                backfillWorkRequest
            )
        }
    }

    private fun calculateDelayUntilMidnight(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis - currentTime
    }

    private fun calculateDelayUntilEndOfDay(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        // If we're already past 11:59 PM today, schedule for tomorrow
        if (calendar.timeInMillis <= currentTime) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis - currentTime
    }
}
