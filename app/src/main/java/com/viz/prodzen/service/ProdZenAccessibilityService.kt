package com.viz.prodzen.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.viz.prodzen.InterventionActivity
import com.viz.prodzen.data.local.DailyUsage
import com.viz.prodzen.data.local.UsageDao
import com.viz.prodzen.data.repository.AppRepository
import com.viz.prodzen.ui.screens.focus.FocusSessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class ProdZenAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var repository: AppRepository

    @Inject
    lateinit var usageDao: UsageDao

    companion object {
        private var allowedPackageName: String? = null
        private var lastAllowedTime: Long = 0

        fun setAllowedPackage(packageName: String?) {
            allowedPackageName = packageName
            lastAllowedTime = System.currentTimeMillis()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (packageName != null && packageName != applicationContext.packageName) {

                val isRecentlyAllowed = (packageName == allowedPackageName) && (System.currentTimeMillis() - lastAllowedTime < 2000)
                if (isRecentlyAllowed) {
                    Log.d("ProdZenAccessibility", "ALLOWING (recently approved): $packageName")
                    // FIXED: Do not consume the pass immediately. Let the timestamp expire naturally.
                    return
                }

                scope.launch {
                    // Track app open count
                    trackAppOpen(packageName)

                    val trackedApp = repository.getAppByPackageName(packageName) ?: return@launch
                    val usageToday = repository.getAppUsageToday(packageName)

                    // Determine intervention type with priority
                    val interventionType = when {
                        FocusSessionViewModel.isSessionActiveGlobal.value -> "FOCUS_SESSION"
                        trackedApp.timeLimitMinutes > 0 && usageToday > trackedApp.timeLimitMinutes * 60 * 1000 -> "LIMIT_EXCEEDED"
                        trackedApp.hasIntention -> "REQUIRE_INTENTION"
                        trackedApp.isTracked -> "PAUSE_EXERCISE"
                        else -> null
                    }

                    if (interventionType != null) {
                        Log.d("ProdZenAccessibility", "BLOCKING ($interventionType): $packageName")
                        launchIntervention(packageName, interventionType)
                    }
                }
            }
        }
    }
    private fun launchIntervention(packageName: String, type: String) {
        val intent = Intent(this, InterventionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            putExtra("TARGET_PACKAGE_NAME", packageName)
            putExtra("INTERVENTION_TYPE", type)
        }
        startActivity(intent)
    }

    private suspend fun trackAppOpen(packageName: String) {
        try {
            val today = getTodayStartMillis()

            // Try to increment existing record
            val rowsUpdated = usageDao.incrementOpenCount(packageName, today)

            // If no rows were updated, create new entry
            if (rowsUpdated == 0) {
                val newEntry = DailyUsage(
                    packageName = packageName,
                    date = today,
                    usageInMillis = 0,
                    openCount = 1
                )
                usageDao.insertDailyUsageEntry(newEntry)
            }

            Log.d("ProdZenAccessibility", "Tracked open for $packageName")
        } catch (e: Exception) {
            Log.e("ProdZenAccessibility", "Error tracking app open", e)
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

    override fun onInterrupt() {
        Log.d("ProdZenAccessibility", "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

