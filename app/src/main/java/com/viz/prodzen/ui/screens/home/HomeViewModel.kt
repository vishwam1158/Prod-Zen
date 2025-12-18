package com.viz.prodzen.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.model.AppInfo
import com.viz.prodzen.data.repository.AppRepository
import com.viz.prodzen.data.repository.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val topApps: List<AppInfo> = emptyList(),
    val totalUsage: Long = 0L,
    val currentStreak: Int = 0,
    val dailyGoalMinutes: Int = 120,
    val goalProgress: Float = 0f // 0.0 to 1.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserStats()
    }

    private fun loadUserStats() {
        viewModelScope.launch {
            userStatsRepository.getUserStatsFlow().collect { stats ->
                val goalMinutes = stats?.dailyGoalMinutes ?: 120
                _uiState.update {
                    val usageMinutes = (it.totalUsage / 60000).toInt()
                    val progress = if (goalMinutes > 0) {
                        (usageMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1.5f)
                    } else 0f

                    it.copy(
                        currentStreak = stats?.currentStreak ?: 0,
                        dailyGoalMinutes = goalMinutes,
                        goalProgress = progress
                    )
                }
            }
        }
    }

    fun loadUsageStats() {
        viewModelScope.launch {
            val allApps = repository.getInstalledApps()
            val totalUsage = allApps.sumOf { app -> app.usageTodayMillis }
            val usageMinutes = (totalUsage / 60000).toInt()
            val goalMinutes = _uiState.value.dailyGoalMinutes
            val progress = if (goalMinutes > 0) {
                (usageMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1.5f)
            } else 0f

            _uiState.update {
                it.copy(
                    topApps = allApps.sortedByDescending { app -> app.usageTodayMillis },
                    totalUsage = totalUsage,
                    goalProgress = progress
                )
            }
        }
    }
}