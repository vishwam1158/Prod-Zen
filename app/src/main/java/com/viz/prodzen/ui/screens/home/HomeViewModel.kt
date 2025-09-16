package com.viz.prodzen.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.model.AppInfo
import com.viz.prodzen.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val topApps: List<AppInfo> = emptyList(),
    val totalUsage: Long = 0L
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun loadUsageStats() {
        viewModelScope.launch {
            val allApps = repository.getInstalledApps()
            _uiState.update {
                it.copy(
                    topApps = allApps.sortedByDescending { app -> app.usageTodayMillis },
                    totalUsage = allApps.sumOf { app -> app.usageTodayMillis }
                )
            }
        }
    }
}