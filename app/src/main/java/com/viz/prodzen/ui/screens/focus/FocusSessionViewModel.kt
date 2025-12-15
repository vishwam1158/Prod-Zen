package com.viz.prodzen.ui.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.local.FocusSessionDao
import com.viz.prodzen.data.local.entities.FocusSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusSessionViewModel @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) : ViewModel() {

    private var timerJob: Job? = null
    private var currentSessionId: Long? = null
    private var sessionStartTime: Long = 0

    private val _durationMinutes = MutableStateFlow(25f)
    val durationMinutes = _durationMinutes.asStateFlow()

    private val _timeRemaining = MutableStateFlow("25:00")
    val timeRemaining = _timeRemaining.asStateFlow()

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive = _isSessionActive.asStateFlow()

    private val _progress = MutableStateFlow(1f)
    val progress = _progress.asStateFlow()

    private val _totalFocusMinutesToday = MutableStateFlow(0)
    val totalFocusMinutesToday = _totalFocusMinutesToday.asStateFlow()

    companion object {
        val isSessionActiveGlobal = MutableStateFlow(false)
    }

    init {
        loadTodaysFocusTime()
    }

    private fun loadTodaysFocusTime() {
        viewModelScope.launch {
            val today = getTodayStartMillis()
            val totalMinutes = focusSessionDao.getTotalFocusMinutesSince(today) ?: 0
            _totalFocusMinutesToday.value = totalMinutes
        }
    }

    fun setDuration(minutes: Float) {
        if (!_isSessionActive.value) {
            _durationMinutes.value = minutes
            _timeRemaining.value = String.format("%02d:00", minutes.toInt())
            _progress.value = 1f
        }
    }

    fun startSession() {
        _isSessionActive.value = true
        isSessionActiveGlobal.value = true

        sessionStartTime = System.currentTimeMillis()

        // Save session start to database
        viewModelScope.launch {
            val session = FocusSession(
                startTime = sessionStartTime,
                endTime = null,
                plannedDurationMinutes = _durationMinutes.value.toInt(),
                actualDurationMinutes = 0,
                completed = false
            )
            currentSessionId = focusSessionDao.insertSession(session)
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val totalSeconds = _durationMinutes.value.toInt() * 60
            for (remainingSeconds in totalSeconds downTo 0) {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                _timeRemaining.value = String.format("%02d:%02d", minutes, seconds)
                _progress.value = remainingSeconds.toFloat() / totalSeconds.toFloat()
                delay(1000)
            }
            // Session completed fully
            completeSession(true)
        }
    }

    fun stopSession() {
        timerJob?.cancel()
        // Session stopped early
        completeSession(false)
    }

    private fun completeSession(completed: Boolean) {
        val endTime = System.currentTimeMillis()
        val actualDurationMinutes = ((endTime - sessionStartTime) / 60000).toInt()

        // Update session in database
        viewModelScope.launch {
            currentSessionId?.let { id ->
                val session = FocusSession(
                    id = id.toInt(),
                    startTime = sessionStartTime,
                    endTime = endTime,
                    plannedDurationMinutes = _durationMinutes.value.toInt(),
                    actualDurationMinutes = actualDurationMinutes,
                    completed = completed
                )
                focusSessionDao.updateSession(session)

                // Reload today's total
                loadTodaysFocusTime()
            }
        }

        _isSessionActive.value = false
        isSessionActiveGlobal.value = false
        setDuration(_durationMinutes.value)
    }

    private fun getTodayStartMillis(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    override fun onCleared() {
        super.onCleared()
        if (_isSessionActive.value) {
            stopSession()
        }
    }
}
