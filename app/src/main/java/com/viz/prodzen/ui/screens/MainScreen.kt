package com.viz.prodzen.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.viz.prodzen.ui.navigation.BottomNavItem
import com.viz.prodzen.ui.screens.appselection.AppSelectionScreen
import com.viz.prodzen.ui.screens.focus.FocusSessionScreen
import com.viz.prodzen.ui.screens.home.HomeScreen
import com.viz.prodzen.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val items = listOf(
                    BottomNavItem.Statistics,
                    BottomNavItem.Focus,
                    BottomNavItem.AppSettings,
                    BottomNavItem.Settings,
                )
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Statistics.route,
            Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Statistics.route) { HomeScreen(navController) }
            composable(BottomNavItem.Focus.route) { FocusSessionScreen(navController) }
            composable(BottomNavItem.AppSettings.route) { AppSelectionScreen(navController) }
            composable(BottomNavItem.Settings.route) { SettingsScreen(navController) }
        }
    }
}
