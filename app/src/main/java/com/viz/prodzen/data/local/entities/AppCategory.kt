package com.viz.prodzen.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_categories")
data class AppCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: String, // Hex color code
    val iconName: String, // Material icon name
    val dailyLimitMinutes: Int = 0
)

