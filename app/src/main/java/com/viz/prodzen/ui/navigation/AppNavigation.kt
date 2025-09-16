package com.viz.prodzen.ui.navigation

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.viz.prodzen.ui.screens.MainScreen
import com.viz.prodzen.ui.screens.permission.PermissionScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val startDestination = if (hasUsageStatsPermission(context)) {
        "main_screen"
    } else {
        Screen.Permission.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Permission.route) {
            PermissionScreen(navController = navController)
        }
        composable("main_screen") {
            MainScreen()
        }
    }
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

