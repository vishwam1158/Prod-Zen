package com.viz.prodzen.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.repository.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _dailyGoalMinutes = MutableStateFlow(120)
    val dailyGoalMinutes = _dailyGoalMinutes.asStateFlow()

    init {
        loadCurrentGoal()
    }

    private fun loadCurrentGoal() {
        viewModelScope.launch {
            val stats = userStatsRepository.getUserStats()
            _dailyGoalMinutes.value = stats.dailyGoalMinutes
        }
    }

    fun setGoal(minutes: Int) {
        _dailyGoalMinutes.value = minutes
    }

    fun saveGoal() {
        viewModelScope.launch {
            userStatsRepository.setDailyGoal(_dailyGoalMinutes.value)
        }
    }
}

