package com.viz.prodzen.ui.navigation

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.viz.prodzen.ui.screens.MainScreen
import com.viz.prodzen.ui.screens.permission.PermissionScreen
import com.viz.prodzen.ui.screens.analytics.AnalyticsScreen
import com.viz.prodzen.utils.PermissionManager
import kotlinx.coroutines.delay

@Composable
fun AppNavigation(permissionCheckTrigger: Int = 0) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Check permissions dynamically on every trigger
    var hasAllPermissions by remember { mutableStateOf(PermissionManager.hasAllPermissions(context)) }

    // Update permission status when trigger changes
    LaunchedEffect(permissionCheckTrigger) {
        hasAllPermissions = PermissionManager.hasAllPermissions(context)
    }

    // Determine start destination based on current permission status
    val startDestination = if (hasAllPermissions) {
        "main_screen"
    } else {
        Screen.Permission.route
    }

    // Check permissions periodically while on main screen
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Check every 3 seconds
            val currentPermissionStatus = PermissionManager.hasAllPermissions(context)

            // If permissions were revoked, navigate back to permission screen
            if (!currentPermissionStatus && navController.currentDestination?.route == "main_screen") {
                navController.navigate(Screen.Permission.route) {
                    popUpTo("main_screen") { inclusive = true }
                }
            }

            hasAllPermissions = currentPermissionStatus
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Permission.route) {
            PermissionScreen(
                onPermissionsGranted = {
                    // This will be called when permissions are granted
                    if (PermissionManager.hasAllPermissions(context)) {
                        navController.navigate("main_screen") {
                            popUpTo(Screen.Permission.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("main_screen") {
            MainScreen(navController = navController)
        }
        composable("analytics_screen") {
            AnalyticsScreen(navController = navController)
        }
    }
}

fun hasUsageStatsPermission(context: Context): Boolean {
    return PermissionManager.hasUsageStatsPermission(context)
}
