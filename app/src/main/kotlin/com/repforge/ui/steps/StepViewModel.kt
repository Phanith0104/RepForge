package com.repforge.ui.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.data.local.entities.StepEntity
import com.repforge.data.repository.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StepViewModel @Inject constructor(
    private val repository: StepRepository
) : ViewModel() {

    val todaySteps: StateFlow<StepEntity?> = repository.getTodaySteps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
