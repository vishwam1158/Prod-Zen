package com.viz.prodzen.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.models.AnalyticsState
import com.viz.prodzen.data.models.HourlyUsagePoint
import com.viz.prodzen.data.repository.AppUsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: AppUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsState())
    val uiState: StateFlow<AnalyticsState> = _uiState.asStateFlow()

    init {
        loadDailyOverview(System.currentTimeMillis())
    }

    fun loadDailyOverview(date: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, date = date)
            repository.refreshUsageData(date)
            repository.getUsageForDate(date).collectLatest { apps ->
                val totalUsage = apps.sumOf { it.totalUsageInMillis }
                val totalOpens = apps.sumOf { it.openCount }

                // Aggregate hourly heatmap
                val heatmap = mutableMapOf<Long, HourlyUsagePoint>()
                apps.forEach { app ->
                    app.hourlyUsage.forEach { point ->
                        val existing = heatmap[point.hourTimestamp]
                        if (existing == null) {
                            heatmap[point.hourTimestamp] = point
                        } else {
                            heatmap[point.hourTimestamp] = existing.copy(
                                usageInMillis = existing.usageInMillis + point.usageInMillis,
                                openCount = existing.openCount + point.openCount
                            )
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalUsageToday = totalUsage,
                    totalOpensToday = totalOpens,
                    apps = apps,
                    hourlyHeatmap = heatmap.values.sortedBy { it.hourTimestamp }
                )
            }
        }
    }

    fun refresh() {
        loadDailyOverview(_uiState.value.date)
    }
}

