package com.viz.prodzen.ui.navigation

sealed class Screen(val route: String) {
    object Permission : Screen("permission_screen")
    object Home : Screen("home_screen")
    object AppList : Screen("app_list_screen")
    object Focus : Screen("focus_session_screen") // Renamed from FocusSession
    object AppLimits : Screen("app_limits_screen")
    object Intentions : Screen("intentions_screen")
    object Goals : Screen("goals_screen")
    object Categories : Screen("categories_screen")
    object AppSelection : Screen("app_selection_screen") // NEW
    object Settings : Screen("settings_screen") // NEW
}