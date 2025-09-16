package com.viz.prodzen.ui.screens.permission

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.viz.prodzen.ui.navigation.Screen
import com.viz.prodzen.ui.navigation.hasUsageStatsPermission

@Composable
fun PermissionScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permission Required",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To track app usage and help you build better habits, 'ProdZen' needs access to your phone's usage data.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            context.startActivity(intent)
        }) {
            Text("Grant Permission")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (hasUsageStatsPermission(context)) {
                navController.navigate("main_screen") {
                    popUpTo(Screen.Permission.route) { inclusive = true }
                }
            }
        }) {
            Text("I've Granted Permission")
        }
    }
}
