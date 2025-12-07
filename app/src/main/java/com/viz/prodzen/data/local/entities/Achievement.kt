package com.viz.prodzen.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String, // e.g., "first_focus", "week_warrior"
    val name: String,
    val description: String,
    val iconRes: Int,
    val pointsReward: Int,
    val unlockedAt: Long? = null
)

