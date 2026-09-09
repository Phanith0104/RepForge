package com.repforge.ui.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.data.local.exercises.*
import com.repforge.data.local.dao.WorkoutDao
import com.repforge.data.local.entities.WorkoutEntity
import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GymViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
) : ViewModel() {

    sealed class GymNavState {
        object MuscleGroups : GymNavState()
        data class SubCategories(val muscleGroup: MuscleGroup) : GymNavState()
        data class Exercises(val muscleGroup: MuscleGroup, val subCategory: String) : GymNavState()
    }

    private val allExercisesList = CHEST_EXERCISES + BACK_EXERCISES + BICEP_EXERCISES + 
            TRICEP_EXERCISES + FOREARM_EXERCISES + SHOULDER_EXERCISES + LEG_EXERCISES + ABS_EXERCISES

    private val _navState = MutableStateFlow<GymNavState>(GymNavState.MuscleGroups)
    val navState: StateFlow<GymNavState> = _navState

    val currentExercises: StateFlow<List<Exercise>> = _navState.map { state ->
        when (state) {
            is GymNavState.Exercises -> allExercisesList.filter { 
                it.muscleGroup == state.muscleGroup && it.subCategory == state.subCategory 
            }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subCategories: StateFlow<List<String>> = _navState.map { state ->
        when (state) {
            is GymNavState.SubCategories -> allExercisesList
                .filter { it.muscleGroup == state.muscleGroup }
                .map { it.subCategory }
                .distinct()
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutEntity>> = workoutDao.getAllWorkoutLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun navigateToSubCategories(muscleGroup: MuscleGroup) {
        _navState.value = GymNavState.SubCategories(muscleGroup)
    }

    fun navigateToExercises(muscleGroup: MuscleGroup, subCategory: String) {
        _navState.value = GymNavState.Exercises(muscleGroup, subCategory)
    }

    fun navigateBack() {
        _navState.value = when (val current = _navState.value) {
            is GymNavState.Exercises -> GymNavState.SubCategories(current.muscleGroup)
            is GymNavState.SubCategories -> GymNavState.MuscleGroups
            else -> GymNavState.MuscleGroups
        }
    }

    fun logWorkout(exerciseName: String, sets: Int, reps: Int, weight: Float?) {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            workoutDao.insertWorkoutLog(
                WorkoutEntity(
                    exerciseName = exerciseName,
                    date = date,
                    sets = sets,
                    reps = reps,
                    weight = weight
                )
            )
        }
    }
}
