package com.viz.prodzen.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val plannedDurationMinutes: Int,
    val actualDurationMinutes: Int = 0,
    val completed: Boolean = false
)

