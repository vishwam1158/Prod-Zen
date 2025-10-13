package com.viz.prodzen.data.local

import androidx.room.Entity

@Entity(tableName = "daily_usage", primaryKeys = ["packageName", "date"])
data class DailyUsage(
    val packageName: String,
    val date: Long, // Start of the day in millis
    val usageInMillis: Long
)