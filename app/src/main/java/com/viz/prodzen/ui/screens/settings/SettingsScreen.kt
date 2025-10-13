package com.viz.prodzen.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.viz.prodzen.ui.navigation.hasUsageStatsPermission

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

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/.service.ProdZenAccessibilityService"
    return try {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains(service) ?: false
    } catch (e: Exception) {
        false
    }
}
