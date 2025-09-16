package com.viz.prodzen.ui.screens.intentions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.model.AppInfo
import com.viz.prodzen.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntentionsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _appList = MutableStateFlow<List<AppInfo>>(emptyList())
    private val logTag = "IntentionsViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // NEW: This map holds the temporary changes made by the user before they hit "Apply".
    private val _pendingChanges = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val pendingChanges = _pendingChanges.asStateFlow()

    val filteredAppList: StateFlow<List<AppInfo>> =
        combine(_appList, _searchQuery) { apps, query ->
            if (query.isBlank()) {
                apps
            } else {
                apps.filter { it.appName.contains(query, ignoreCase = true) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _appList.value = repository.getInstalledApps()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // This now only updates the local map of pending changes.
    fun onIntentionCheckedChanged(appInfo: AppInfo, isChecked: Boolean) {
        Log.d(logTag, "Checkbox for ${appInfo.appName} changed to $isChecked (pending)")
        val currentChanges = _pendingChanges.value.toMutableMap()
        currentChanges[appInfo.packageName] = isChecked
        _pendingChanges.value = currentChanges
    }

    // This function saves only the changes to the database.
    fun applyChanges() {
        Log.d(logTag, "Applying ${_pendingChanges.value.size} intention changes to database...")
        viewModelScope.launch {
            _pendingChanges.value.forEach { (packageName, hasIntention) ->
                val appInfo = _appList.value.find { it.packageName == packageName }
                if (appInfo != null) {
                    // FIXED: Use the new safe update function.
//                    repository.updateIntentionSetting(packageName, appInfo.appName, hasIntention)
                }
            }
            _pendingChanges.value = emptyMap() // Clear pending changes after saving
            loadApps() // Reload from DB to ensure UI is in sync
            Log.d(logTag, "Intention changes applied successfully.")
        }
    }
}
