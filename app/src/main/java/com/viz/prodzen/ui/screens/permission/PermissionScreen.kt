package com.viz.prodzen.ui.screens.permission

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.viz.prodzen.utils.PermissionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PermissionScreen(
    onPermissionsGranted: () -> Unit = {}
) {
    val context = LocalContext.current
    var usageStatsGranted by remember { mutableStateOf(false) }
    var accessibilityGranted by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }

    // Function to check permissions
    fun checkPermissions() {
        android.util.Log.d("PermissionScreen", "=== Checking Permissions ===")

        val usageStats = PermissionManager.hasUsageStatsPermission(context)
        android.util.Log.d("PermissionScreen", "Usage Stats Result: $usageStats")

        val accessibility = PermissionManager.isAccessibilityServiceEnabled(context)
        android.util.Log.d("PermissionScreen", "Accessibility Result: $accessibility")

        usageStatsGranted = usageStats
        accessibilityGranted = accessibility

        android.util.Log.d("PermissionScreen", "UI States - Usage: $usageStatsGranted, Accessibility: $accessibilityGranted")

        // Auto-navigate if both are granted
        if (usageStats && accessibility) {
            android.util.Log.d("PermissionScreen", "Both permissions granted! Navigating...")
            onPermissionsGranted()
        } else {
            android.util.Log.d("PermissionScreen", "Permissions not fully granted yet")
        }
    }

    // Check permissions on initial composition
    LaunchedEffect(Unit) {
        checkPermissions()
    }

    // Check permissions when screen resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    delay(300) // Small delay to ensure settings are applied
                    checkPermissions()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Periodically check permissions while on this screen (every 2 seconds)
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            checkPermissions()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to ProdZen",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "To help you build better phone habits, we need a couple of permissions",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Usage Stats Permission Card
        PermissionCard(
            title = "Usage Statistics",
            description = "Track your app usage time and help you understand your phone habits",
            isGranted = usageStatsGranted,
            onGrantClick = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Accessibility Permission Card
        PermissionCard(
            title = "Accessibility Service",
            description = "Show mindful interventions when you open distracting apps and enable focus mode blocking",
            isGranted = accessibilityGranted,
            onGrantClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Status indicator
        if (usageStatsGranted && accessibilityGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "All permissions granted! Loading app...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            // Manual check button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Grant both permissions above to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isChecking = true
                        android.widget.Toast.makeText(context, "Checking permissions...", android.widget.Toast.LENGTH_SHORT).show()
                        android.util.Log.d("PermissionScreen", "Check button clicked!")
                        checkPermissions()
                        coroutineScope.launch {
                            delay(500)
                            isChecking = false
                            // Show result
                            val message = when {
                                usageStatsGranted && accessibilityGranted -> "All permissions granted! ✓"
                                usageStatsGranted -> "Still need Accessibility permission"
                                accessibilityGranted -> "Still need Usage Stats permission"
                                else -> "Both permissions needed"
                            }
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    enabled = !isChecking
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isChecking) "Checking..." else "Check Permissions")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap this button after granting permissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (isGranted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Not granted",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isGranted) {
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permission")
                }
            } else {
                OutlinedButton(
                    onClick = onGrantClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("✓ Granted - Tap to change")
                }
            }
        }
    }
}
