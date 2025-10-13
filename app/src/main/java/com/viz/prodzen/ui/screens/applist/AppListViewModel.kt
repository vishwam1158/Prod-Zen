package com.viz.prodzen.ui.screens.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.model.AppInfo
import com.viz.prodzen.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
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

    fun onAppCheckedChanged(appInfo: AppInfo, isChecked: Boolean) {
        viewModelScope.launch {
            // FIXED: Use the new safe update function.
//            repository.updatePauseSetting(appInfo.packageName, appInfo.appName, isChecked)

            // Update UI state instantly for a responsive feel.
            val currentList = _appList.value.toMutableList()
            val index = currentList.indexOfFirst { it.packageName == appInfo.packageName }
            if (index != -1) {
                val updatedApp = currentList[index].copy(isTracked = isChecked)
                currentList[index] = updatedApp
                _appList.value = currentList
            }
        }
    }
}
