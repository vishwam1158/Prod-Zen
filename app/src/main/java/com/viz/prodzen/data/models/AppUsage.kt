package com.viz.prodzen.data.models

data class AppUsage(
    val packageName: String,
    val appName: String,
    val totalUsageInMillis: Long,
    val openCount: Int,
    val hourlyUsage: List<HourlyUsagePoint> = emptyList()
)

data class HourlyUsagePoint(
    val hourTimestamp: Long,
    val usageInMillis: Long,
    val openCount: Int
)

data class AnalyticsState(
    val isLoading: Boolean = false,
    val date: Long = System.currentTimeMillis(),
    val totalUsageToday: Long = 0,
    val totalOpensToday: Int = 0,
    val apps: List<AppUsage> = emptyList(),
    val hourlyHeatmap: List<HourlyUsagePoint> = emptyList() // Aggregated across all apps
)

