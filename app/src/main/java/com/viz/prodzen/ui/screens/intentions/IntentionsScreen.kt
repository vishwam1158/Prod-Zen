package com.viz.prodzen.ui.screens.intentions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.viz.prodzen.ui.components.AppListItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentionsScreen(
    navController: NavController,
    viewModel: IntentionsViewModel = hiltViewModel()
) {
    val appListState by viewModel.filteredAppList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pendingChanges by viewModel.pendingChanges.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Set App Intentions") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.applyChanges()
                    scope.launch {
                        snackbarHostState.showSnackbar("Intentions saved successfully!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Apply Changes")
            }
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
            LazyColumn {
                items(appListState) { appInfo ->
                    // FIXED: The checkbox now reflects the temporary, unsaved state.
                    val isChecked = pendingChanges[appInfo.packageName] ?: appInfo.hasIntention
                    AppListItem(
                        appInfo = appInfo,
                        isChecked = isChecked,
                        onCheckedChange = { newCheckedState ->
                            viewModel.onIntentionCheckedChanged(appInfo, newCheckedState)
                        }
                    )
                }
            }
        }
    }
}

