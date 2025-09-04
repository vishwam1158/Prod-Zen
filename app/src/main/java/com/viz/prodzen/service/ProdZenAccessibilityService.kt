package com.viz.prodzen.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.viz.prodzen.InterventionActivity
import com.viz.prodzen.data.repository.AppRepository
import com.viz.prodzen.ui.screens.focus.FocusSessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProdZenAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var repository: AppRepository

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
                    val trackedApp = repository.getAppByPackageName(packageName) ?: return@launch
                    val usageToday = repository.getAppUsageToday(packageName)

                    // Determine intervention type with priority
                    val interventionType = when {
                        FocusSessionViewModel.isSessionActive.value -> "FOCUS_SESSION"
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

    override fun onInterrupt() {
        Log.d("ProdZenAccessibility", "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

