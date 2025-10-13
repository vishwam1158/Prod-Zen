package com.viz.prodzen.ui.navigation

sealed class Screen(val route: String) {
    object Permission : Screen("permission_screen")
    object Home : Screen("home_screen")
    object AppList : Screen("app_list_screen")
    object FocusSession : Screen("focus_session_screen")
    object AppLimits : Screen("app_limits_screen")
    object Intentions : Screen("intentions_screen")
}