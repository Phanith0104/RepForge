package com.repforge.ui.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.data.local.exercises.*
import com.repforge.data.repository.GymRepository
import com.repforge.data.repository.UserRepository
import com.repforge.data.local.entities.WorkoutEntity
import com.repforge.data.local.entities.CustomExerciseEntity
import com.repforge.data.local.entities.WorkoutTemplateEntity
import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

import com.repforge.data.local.entities.TemplateExercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@HiltViewModel
class GymViewModel @Inject constructor(
    private val gymRepository: GymRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val gson = Gson()

    enum class GymTab { HISTORY, WORKOUT, CALENDAR }

    private val _selectedTab = MutableStateFlow(GymTab.HISTORY)
    val selectedTab: StateFlow<GymTab> = _selectedTab

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _selectedDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate

    sealed class GymNavState {
        object MuscleGroups : GymNavState()
        data class SubCategories(val muscleGroup: MuscleGroup) : GymNavState()
        data class Exercises(val muscleGroup: MuscleGroup, val subCategory: String) : GymNavState()
    }

    private val defaultExercises = CHEST_EXERCISES + BACK_EXERCISES + BICEP_EXERCISES + 
            TRICEP_EXERCISES + FOREARM_EXERCISES + SHOULDER_EXERCISES + LEG_EXERCISES + ABS_EXERCISES

    private val _navState = MutableStateFlow<GymNavState>(GymNavState.MuscleGroups)
    val navState: StateFlow<GymNavState> = _navState

    private val _activeWorkoutExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val activeWorkoutExercises: StateFlow<List<Exercise>> = _activeWorkoutExercises

    fun addExerciseToActiveWorkout(exercise: Exercise) {
        _activeWorkoutExercises.value += exercise
    }

    fun removeExerciseFromActiveWorkout(exercise: Exercise) {
        _activeWorkoutExercises.value -= exercise
    }

    fun clearActiveWorkout() {
        _activeWorkoutExercises.value = emptyList()
    }

    val customExercises = gymRepository.getCustomExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutTemplates = gymRepository.getWorkoutTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedWorkoutLogs: StateFlow<List<WorkoutEntity>> = gymRepository.getAllCompletedWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plannedWorkouts: StateFlow<List<WorkoutEntity>> = gymRepository.getAllPlannedWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayPlan: StateFlow<List<WorkoutEntity>> = gymRepository.getPlannedWorkoutsForDate(gymRepository.getTodayDate())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plannedWorkoutsForSelectedDate: StateFlow<List<WorkoutEntity>> = _selectedDate.flatMapLatest { date ->
        gymRepository.getPlannedWorkoutsForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentExercises: StateFlow<List<Exercise>> = combine(_navState, customExercises) { state, custom ->
        val filteredDefault = when (state) {
            is GymNavState.Exercises -> defaultExercises.filter { 
                it.muscleGroup == state.muscleGroup && it.subCategory == state.subCategory 
            }
            else -> emptyList()
        }
        
        val filteredCustom = when (state) {
            is GymNavState.Exercises -> custom.filter { 
                it.muscleGroup == state.muscleGroup && it.musclePart == state.subCategory 
            }.map { 
                Exercise(it.name, "", "None", it.description, it.muscleGroup, it.musclePart)
            }
            else -> emptyList()
        }
        
        filteredDefault + filteredCustom
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subCategories: StateFlow<List<String>> = combine(_navState, customExercises) { state, custom ->
        if (state is GymNavState.SubCategories) {
            val defaultSubs = defaultExercises
                .filter { it.muscleGroup == state.muscleGroup }
                .map { it.subCategory }
            
            val customSubs = custom
                .filter { it.muscleGroup == state.muscleGroup }
                .map { it.musclePart }
            
            (defaultSubs + customSubs).distinct()
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: GymTab) {
        _selectedTab.value = tab
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

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

    fun logWorkout(exerciseName: String, sets: Int, reps: Int, weight: Float?, workoutName: String = "Quick Workout", date: String? = null, isCompleted: Boolean = true) {
        viewModelScope.launch {
            gymRepository.insertWorkoutLog(
                WorkoutEntity(
                    userId = "", 
                    workoutName = workoutName,
                    exerciseName = exerciseName,
                    date = date ?: gymRepository.getTodayDate(),
                    sets = sets,
                    reps = reps,
                    weight = weight,
                    isCompleted = isCompleted
                )
            )
        }
    }

    fun updateWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            gymRepository.updateWorkoutLog(workout)
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            gymRepository.deleteWorkoutLog(workout)
        }
    }

    fun deleteWorkoutByGroup(date: String, workoutName: String) {
        viewModelScope.launch {
            gymRepository.deleteWorkoutByGroup(date, workoutName)
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch {
            gymRepository.deleteAllHistory()
        }
    }

    fun saveCustomExercise(
        name: String, 
        muscleGroup: MuscleGroup, 
        musclePart: String, 
        equipment: String, 
        description: String,
        sets: Int,
        reps: Int,
        weight: Float,
        duration: Int,
        notes: String
    ) {
        viewModelScope.launch {
            gymRepository.saveCustomExercise(
                CustomExerciseEntity(
                    userId = "",
                    name = name,
                    muscleGroup = muscleGroup,
                    musclePart = musclePart,
                    equipment = equipment,
                    description = description,
                    defaultSets = sets,
                    defaultReps = reps,
                    defaultWeight = weight,
                    defaultDurationMinutes = duration,
                    notes = notes
                )
            )
        }
    }

    fun deleteCustomExercise(exercise: CustomExerciseEntity) {
        viewModelScope.launch {
            gymRepository.deleteCustomExercise(exercise)
        }
    }

    fun saveWorkoutTemplate(name: String, workouts: List<WorkoutEntity>) {
        viewModelScope.launch {
            val templateExercises = workouts.map {
                TemplateExercise(it.exerciseName, it.sets, it.reps, it.weight)
            }
            gymRepository.saveWorkoutTemplate(
                WorkoutTemplateEntity(
                    userId = "",
                    name = name,
                    exercisesJson = gson.toJson(templateExercises)
                )
            )
        }
    }

    fun applyTemplateToDate(template: WorkoutTemplateEntity, date: String) {
        viewModelScope.launch {
            val listType = object : TypeToken<List<TemplateExercise>>() {}.type
            val exercises: List<TemplateExercise> = gson.fromJson(template.exercisesJson, listType)
            
            exercises.forEach {
                gymRepository.insertWorkoutLog(
                    WorkoutEntity(
                        userId = "",
                        workoutName = template.name,
                        exerciseName = it.name,
                        date = date,
                        sets = it.sets,
                        reps = it.reps,
                        weight = it.weight,
                        isCompleted = false
                    )
                )
            }
        }
    }

    fun deleteTemplate(template: WorkoutTemplateEntity) {
        viewModelScope.launch {
            gymRepository.deleteWorkoutTemplate(template)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                userRepository.syncUserFromFirebase()
            } catch (e: Exception) {
                // Log error
            }
            delay(1000)
            _isRefreshing.value = false
        }
    }
}
