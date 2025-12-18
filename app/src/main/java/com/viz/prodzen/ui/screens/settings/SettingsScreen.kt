package com.viz.prodzen.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.viz.prodzen.ui.navigation.Screen
import com.viz.prodzen.ui.navigation.hasUsageStatsPermission
import com.viz.prodzen.utils.LocalThemePreference
import com.viz.prodzen.utils.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var hasUsagePermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    // This will refresh the permission status when the user returns to the app
    LaunchedEffect(Unit) {
        hasUsagePermission = hasUsageStatsPermission(context)
        isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings & Permissions") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Required Permissions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("For ProdZen to work correctly, the following permissions must be enabled.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            PermissionCard(
                title = "Usage Stats Access",
                description = "Allows the app to read your app usage to display statistics and enforce time limits.",
                isGranted = hasUsagePermission,
                onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionCard(
                title = "Accessibility Service",
                description = "Allows the app to detect when other apps are launched to show mindful pauses and block apps.",
                isGranted = isAccessibilityEnabled,
                onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Theme Section
            Text("Appearance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Choose your preferred theme for a calming experience", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            ThemeSelectorCard()

            Spacer(modifier = Modifier.height(32.dp))

            Text("App Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(Screen.Categories.route) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "App Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Manage category limits and view categorized apps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionCard(title: String, description: String, isGranted: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = "Status",
                    tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
            if (!isGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
                    Text("Enable")
                }
            }
        }
    }
}

@Composable
fun ThemeSelectorCard() {
    val themePreference = LocalThemePreference.current
    var selectedTheme by remember { mutableStateOf(themePreference.themeMode) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Theme",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Theme Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeOption(
                    modifier = Modifier.weight(1f),
                    label = "System",
                    icon = Icons.Default.Settings,
                    isSelected = selectedTheme == ThemePreference.MODE_SYSTEM,
                    onClick = {
                        selectedTheme = ThemePreference.MODE_SYSTEM
                        themePreference.themeMode = ThemePreference.MODE_SYSTEM
                    }
                )

                ThemeOption(
                    modifier = Modifier.weight(1f),
                    label = "Light",
                    icon = Icons.Default.LightMode,
                    isSelected = selectedTheme == ThemePreference.MODE_LIGHT,
                    onClick = {
                        selectedTheme = ThemePreference.MODE_LIGHT
                        themePreference.themeMode = ThemePreference.MODE_LIGHT
                    }
                )

                ThemeOption(
                    modifier = Modifier.weight(1f),
                    label = "Dark",
                    icon = Icons.Default.DarkMode,
                    isSelected = selectedTheme == ThemePreference.MODE_DARK,
                    onClick = {
                        selectedTheme = ThemePreference.MODE_DARK
                        themePreference.themeMode = ThemePreference.MODE_DARK
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeOption(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/.service.ProdZenAccessibilityService"
    return try {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains(service) ?: false
    } catch (e: Exception) {
        false
    }
}
