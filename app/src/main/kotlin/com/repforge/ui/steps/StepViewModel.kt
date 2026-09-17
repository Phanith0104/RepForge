package com.repforge.ui.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.data.local.entities.StepEntity
import com.repforge.data.repository.StepRepository
import com.repforge.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

import android.os.PowerManager
import androidx.core.content.getSystemService

@HiltViewModel
class StepViewModel @Inject constructor(
    private val repository: StepRepository,
    private val userRepository: UserRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    val isSensorAvailable: Boolean = repository.isStepCounterAvailable()
    
    val isBatteryOptimizationDisabled: Boolean = run {
        val pm = context.getSystemService<PowerManager>()
        pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }

    val todaySteps: StateFlow<StepEntity?> = repository.getTodaySteps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val stepHistory: StateFlow<List<StepEntity>> = repository.getAllStepHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weeklyAverage: StateFlow<Int> = stepHistory.map { history ->
        if (history.isEmpty()) return@map 0
        val last7Days = history.take(7)
        last7Days.sumOf { it.count } / last7Days.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyAverage: StateFlow<Int> = stepHistory.map { history ->
        if (history.isEmpty()) return@map 0
        val last30Days = history.take(30)
        last30Days.sumOf { it.count } / last30Days.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _stepGoal = MutableStateFlow(repository.getStepGoal())
    val stepGoal: StateFlow<Int> = _stepGoal

    val currentStreak: StateFlow<Int> = combine(todaySteps, stepHistory, stepGoal) { _, _, _ ->
        repository.calculateCurrentStreak()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun updateStepGoal(newGoal: Int) {
        repository.updateStepGoal(newGoal)
        _stepGoal.value = newGoal
    }

    fun deleteHistoryRecord(date: String) {
        viewModelScope.launch {
            repository.deleteHistoryRecord(date)
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                userRepository.syncUserFromFirebase()
                // repository.syncSteps() // If we add cloud sync for steps later
            } catch (e: Exception) {
                // Log or handle error
            }
            delay(1000)
            _isRefreshing.value = false
        }
    }
}
