package com.viz.prodzen.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.viz.prodzen.R

sealed class BottomNavItem(val route: String, val icon: Int, val title: String) {
    object Statistics : BottomNavItem("home_screen", R.drawable.circular_target, "Stats") // Changed route to match Screen.Home
    object Focus : BottomNavItem("focus_session_screen", R.drawable.circular_target, "Focus") // Changed route to match Screen.Focus
    object AppSettings : BottomNavItem("app_selection_screen", R.drawable.setting, "App Settings") // Changed route to match Screen.AppSelection
    object Settings : BottomNavItem("settings_screen", R.drawable.setting, "Settings") // Changed route to match Screen.Settings
}