package com.viz.prodzen.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.viz.prodzen.R

sealed class BottomNavItem(val route: String, val icon: Int, val title: String) {
    object Statistics : BottomNavItem("statistics", R.drawable.circular_target, "Stats")
    object Focus : BottomNavItem("focus", R.drawable.circular_target, "Focus")
    object AppSettings : BottomNavItem("app_settings", R.drawable.setting, "App Settings") // NEW
    object Settings : BottomNavItem("settings", R.drawable.setting, "Settings") // NEW
}