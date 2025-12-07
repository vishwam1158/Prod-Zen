package com.viz.prodzen.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "app_category_mapping",
    foreignKeys = [
        ForeignKey(
            entity = AppCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AppCategoryMapping(
    @PrimaryKey val packageName: String,
    val categoryId: Int
)

