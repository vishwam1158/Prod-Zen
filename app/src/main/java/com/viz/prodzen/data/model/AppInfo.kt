package com.viz.prodzen.data.model

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_apps")
data class AppInfo(
    @PrimaryKey val packageName: String,
    val appName: String,
    var isTracked: Boolean = false,
    var timeLimitMinutes: Int = 0,
    var hasIntention: Boolean = false, // NEW: For "Custom Intentions"
    @Ignore var icon: Drawable? = null,
    @Ignore var usageTodayMillis: Long = 0
) {
    constructor(packageName: String, appName: String, isTracked: Boolean, timeLimitMinutes: Int, hasIntention: Boolean) : this(packageName, appName, isTracked, timeLimitMinutes, hasIntention, null, 0)
}