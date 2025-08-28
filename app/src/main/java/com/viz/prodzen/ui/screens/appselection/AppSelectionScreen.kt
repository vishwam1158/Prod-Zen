package com.viz.prodzen.ui.screens.appselection

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.viz.prodzen.data.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    navController: NavController,
    viewModel: AppSelectionViewModel = hiltViewModel()
) {
    val appListState by viewModel.filteredAppList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("App Settings") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search apps...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(modifier = Modifier.padding(bottom = 16.dp)) {
                items(appListState) { appInfo ->
                    AppSettingItem(
                        appInfo = appInfo,
                        onPauseChanged = { isChecked -> viewModel.onPauseChanged(appInfo, isChecked) },
                        onIntentionChanged = { isChecked -> viewModel.onIntentionChanged(appInfo, isChecked) },
                        onLimitChanged = { newLimit -> viewModel.onLimitChanged(appInfo, newLimit) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppSettingItem(
    appInfo: AppInfo,
    onPauseChanged: (Boolean) -> Unit,
    onIntentionChanged: (Boolean) -> Unit,
    onLimitChanged: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(model = appInfo.icon),
                contentDescription = appInfo.appName,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(appInfo.appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Pause Exercise Toggle
        SettingRow("Mindful Pause", appInfo.isTracked, onPauseChanged)

        // Custom Intention Toggle
        SettingRow("Require Intention", appInfo.hasIntention, onIntentionChanged)

        // App Limit Slider
        Column {
            Text("Daily Limit", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = appInfo.timeLimitMinutes.toFloat(),
                    onValueChange = { onLimitChanged(it.toInt()) },
                    modifier = Modifier.weight(1f),
                    valueRange = 0f..180f,
                    steps = 179
                )
                Text(
                    text = if (appInfo.timeLimitMinutes > 0) "${appInfo.timeLimitMinutes}m" else "Off",
                    modifier = Modifier.width(50.dp)
                )
            }
        }
        Divider(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun SettingRow(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}
