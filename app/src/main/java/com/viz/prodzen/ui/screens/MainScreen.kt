package com.viz.prodzen.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.viz.prodzen.ui.navigation.BottomNavItem
import com.viz.prodzen.ui.navigation.Screen
import com.viz.prodzen.ui.screens.appselection.AppSelectionScreen
import com.viz.prodzen.ui.screens.categories.CategoriesScreen
import com.viz.prodzen.ui.screens.focus.FocusSessionScreen
import com.viz.prodzen.ui.screens.goals.GoalsScreen
import com.viz.prodzen.ui.screens.home.HomeScreen
import com.viz.prodzen.ui.screens.settings.SettingsScreen
import com.viz.prodzen.ui.screens.analytics.AnalyticsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: androidx.navigation.NavController) { // Changed signature
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val items = listOf(
                    BottomNavItem.Statistics,
                    BottomNavItem.Focus,
                    BottomNavItem.AppSettings,
                    BottomNavItem.Settings,
                )
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = screen.icon),
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
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
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController = navController) } // Pass main navController
            composable(Screen.Focus.route) { FocusSessionScreen(navController = navController) }
            composable(Screen.AppSelection.route) { AppSelectionScreen(navController = navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController = navController) }
            composable(Screen.Goals.route) { GoalsScreen(navController = navController) }
            composable(Screen.Categories.route) { CategoriesScreen(navController = navController) }
            composable("analytics_screen") { AnalyticsScreen(navController = navController) }
        }
    }
}
