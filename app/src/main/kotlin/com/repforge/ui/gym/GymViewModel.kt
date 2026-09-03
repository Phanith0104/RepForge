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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GymViewModel @Inject constructor(
    private val workoutDao: WorkoutDao
) : ViewModel() {

    private val allExercises = CHEST_EXERCISES + BACK_EXERCISES + ARM_EXERCISES + SHOULDER_LEG_ABS_EXERCISES

    private val _selectedCategory = MutableStateFlow(MuscleGroup.CHEST)
    val selectedCategory: StateFlow<MuscleGroup> = _selectedCategory

    val exercises: StateFlow<List<Exercise>> = _selectedCategory.combine(MutableStateFlow(allExercises)) { category, exercises ->
        exercises.filter { it.muscleGroup == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutEntity>> = workoutDao.getAllWorkoutLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCategory(category: MuscleGroup) {
        _selectedCategory.value = category
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
