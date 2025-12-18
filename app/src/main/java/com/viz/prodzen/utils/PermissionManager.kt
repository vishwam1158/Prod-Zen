package com.viz.prodzen.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager

/**
 * Manages permission checks for Usage Stats and Accessibility Service
 */
object PermissionManager {

    private const val TAG = "PermissionManager"

    /**
     * Check if Usage Stats permission is granted
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        val hasPermission = mode == AppOpsManager.MODE_ALLOWED
        Log.d(TAG, "Usage Stats Permission: $hasPermission (mode: $mode)")
        return hasPermission
    }

    /**
     * Check if Accessibility Service is enabled using AccessibilityManager (most reliable)
     */
    private fun isAccessibilityServiceEnabledViaManager(context: Context): Boolean {
        return try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val enabledServices = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

            val packageName = context.packageName
            val serviceClassName = "com.viz.prodzen.service.ProdZenAccessibilityService"

            val isEnabled = enabledServices?.any { serviceInfo ->
                val id = serviceInfo.id
                Log.d(TAG, "Checking enabled service: $id")
                id.contains(packageName) && id.contains("ProdZenAccessibilityService")
            } ?: false

            Log.d(TAG, "AccessibilityManager check result: $isEnabled")
            isEnabled
        } catch (e: Exception) {
            Log.e(TAG, "Error checking via AccessibilityManager", e)
            false
        }
    }

    /**
     * Check if Accessibility Service is enabled
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val packageName = context.packageName

        // Method 1: Check via AccessibilityManager (most reliable)
        val managerCheck = isAccessibilityServiceEnabledViaManager(context)
        Log.d(TAG, "Manager check: $managerCheck")

        if (managerCheck) {
            return true
        }

        // Method 2: Check Settings.Secure with multiple formats
        try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""

            Log.d(TAG, "Package name: $packageName")
            Log.d(TAG, "Enabled services string: $enabledServices")

            // Try multiple service name formats
            val serviceNames = listOf(
                "$packageName/.service.ProdZenAccessibilityService",
                "$packageName/com.viz.prodzen.service.ProdZenAccessibilityService",
                "com.viz.prodzen/.service.ProdZenAccessibilityService",
                "com.viz.prodzen/com.viz.prodzen.service.ProdZenAccessibilityService"
            )

            // Check if any of the service name formats match
            val hasPermission = serviceNames.any { serviceName ->
                val found = enabledServices.contains(serviceName)
                Log.d(TAG, "Checking format: $serviceName - Found: $found")
                found
            }

            // Also check if the service class name appears anywhere in the enabled services
            val containsServiceClass = enabledServices.contains("ProdZenAccessibilityService")
            Log.d(TAG, "Contains service class name: $containsServiceClass")

            val result = hasPermission || containsServiceClass
            Log.d(TAG, "Final Accessibility Service Enabled: $result")

            return result
        } catch (e: Exception) {
            Log.e(TAG, "Error checking accessibility service", e)
            return false
        }
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasAllPermissions(context: Context): Boolean {
        return hasUsageStatsPermission(context) && isAccessibilityServiceEnabled(context)
    }

    /**
     * Get the current permission status
     */
    fun getPermissionStatus(context: Context): PermissionStatus {
        val usageStats = hasUsageStatsPermission(context)
        val accessibility = isAccessibilityServiceEnabled(context)

        return when {
            usageStats && accessibility -> PermissionStatus.ALL_GRANTED
            usageStats && !accessibility -> PermissionStatus.ACCESSIBILITY_NEEDED
            !usageStats && accessibility -> PermissionStatus.USAGE_STATS_NEEDED
            else -> PermissionStatus.BOTH_NEEDED
        }
    }

    enum class PermissionStatus {
        ALL_GRANTED,
        USAGE_STATS_NEEDED,
        ACCESSIBILITY_NEEDED,
        BOTH_NEEDED
    }
}

