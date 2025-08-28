package com.viz.prodzen.ui.screens.appselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.model.AppInfo
import com.viz.prodzen.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _appList = MutableStateFlow<List<AppInfo>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

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

    private fun updateAppSetting(appInfo: AppInfo, updateAction: (AppInfo) -> AppInfo) {
        val updatedAppInfo = updateAction(appInfo)

        val currentList = _appList.value.toMutableList()
        val index = currentList.indexOfFirst { it.packageName == appInfo.packageName }
        if (index != -1) {
            currentList[index] = updatedAppInfo
            _appList.value = currentList
        }

        viewModelScope.launch {
            // FIXED: Use the single, correct update function.
            repository.updateAppSettings(updatedAppInfo)
        }
    }

    fun onPauseChanged(appInfo: AppInfo, isChecked: Boolean) {
        updateAppSetting(appInfo) { it.copy(isTracked = isChecked) }
    }

    fun onIntentionChanged(appInfo: AppInfo, isChecked: Boolean) {
        updateAppSetting(appInfo) { it.copy(hasIntention = isChecked) }
    }

    fun onLimitChanged(appInfo: AppInfo, newLimit: Int) {
        updateAppSetting(appInfo) { it.copy(timeLimitMinutes = newLimit) }
    }
}
