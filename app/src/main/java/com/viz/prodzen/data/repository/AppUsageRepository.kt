package com.viz.prodzen.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.viz.prodzen.data.local.HourlyUsageDao
import com.viz.prodzen.data.local.entities.HourlyAppUsage
import com.viz.prodzen.data.models.AppUsage
import com.viz.prodzen.data.models.HourlyUsagePoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUsageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hourlyUsageDao: HourlyUsageDao
) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    suspend fun refreshUsageData(date: Long) {
        withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance().apply { timeInMillis = date }
            val startOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = calendar.apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis

            // Query UsageStatsManager for events
            val events = usageStatsManager.queryEvents(startOfDay, endOfDay)
            val usageMap = mutableMapOf<String, MutableMap<Long, HourlyData>>()

            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)

                // Bucket into hours
                val hourTimestamp = getHourTimestamp(event.timeStamp)
                val packageName = event.packageName

                val appMap = usageMap.getOrPut(packageName) { mutableMapOf() }
                val hourlyData = appMap.getOrPut(hourTimestamp) { HourlyData() }

                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        hourlyData.lastForegroundTime = event.timeStamp
                        hourlyData.openCount++
                    }
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        if (hourlyData.lastForegroundTime != 0L) {
                            hourlyData.usageDuration += (event.timeStamp - hourlyData.lastForegroundTime)
                            hourlyData.lastForegroundTime = 0L
                        }
                    }
                }
            }

            // Convert to entities and save
            val entities = mutableListOf<HourlyAppUsage>()
            usageMap.forEach { (pkg, hourlyMap) ->
                hourlyMap.forEach { (hour, data) ->
                    entities.add(
                        HourlyAppUsage(
                            packageName = pkg,
                            hourTimestamp = hour,
                            usageDuration = data.usageDuration,
                            openCount = data.openCount
                        )
                    )
                }
            }
            hourlyUsageDao.insertAll(entities)
        }
    }

    fun getUsageForDate(date: Long): Flow<List<AppUsage>> {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = calendar.apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        return hourlyUsageDao.getUsageBetween(startOfDay, endOfDay).map { entities ->
            val grouped = entities.groupBy { it.packageName }
            grouped.map { (pkg, list) ->
                val appName = try {
                    packageManager.getApplicationInfo(pkg, 0).loadLabel(packageManager).toString()
                } catch (e: Exception) {
                    pkg
                }

                AppUsage(
                    packageName = pkg,
                    appName = appName,
                    totalUsageInMillis = list.sumOf { it.usageDuration },
                    openCount = list.sumOf { it.openCount },
                    hourlyUsage = list.map {
                        HourlyUsagePoint(it.hourTimestamp, it.usageDuration, it.openCount)
                    }.sortedBy { it.hourTimestamp }
                )
            }.sortedByDescending { it.totalUsageInMillis }
        }
    }

    private fun getHourTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private data class HourlyData(
        var usageDuration: Long = 0,
        var openCount: Int = 0,
        var lastForegroundTime: Long = 0
    )
}

