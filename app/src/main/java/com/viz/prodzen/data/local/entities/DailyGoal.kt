package com.viz.prodzen.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goals")
data class DailyGoal(
    @PrimaryKey val date: Long,
    val screenTimeGoalMinutes: Int,
    val actualScreenTimeMinutes: Int = 0,
    val goalMet: Boolean = false
)

