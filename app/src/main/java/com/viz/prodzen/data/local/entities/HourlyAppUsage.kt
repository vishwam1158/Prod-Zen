package com.viz.prodzen.data.local.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "hourly_app_usage",
    primaryKeys = ["packageName", "hourTimestamp"],
    indices = [Index(value = ["hourTimestamp"])]
)
data class HourlyAppUsage(
    val packageName: String,
    val hourTimestamp: Long, // Start of the hour in millis
    val usageDuration: Long, // Duration in millis
    val openCount: Int
)

