package com.repforge.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.repforge.data.local.dao.CustomExerciseDao
import com.repforge.data.local.dao.WorkoutDao
import com.repforge.data.local.dao.WorkoutTemplateDao
import com.repforge.data.local.entities.CustomExerciseEntity
import com.repforge.data.local.entities.WorkoutEntity
import com.repforge.data.local.entities.WorkoutTemplateEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GymRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val customExerciseDao: CustomExerciseDao,
    private val templateDao: WorkoutTemplateDao,
    private val firebaseAuth: FirebaseAuth
) {
    private fun getCurrentUserId(): String = firebaseAuth.currentUser?.uid ?: "guest"

    fun getAllCompletedWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.getAllCompletedWorkouts(getCurrentUserId())

    fun getAllPlannedWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.getAllPlannedWorkouts(getCurrentUserId())

    fun getPlannedWorkoutsForDate(date: String): Flow<List<WorkoutEntity>> = workoutDao.getPlannedWorkoutsForDate(getCurrentUserId(), date)

    fun getCustomExercises(): Flow<List<CustomExerciseEntity>> = customExerciseDao.getCustomExercises(getCurrentUserId())

    fun getWorkoutTemplates(): Flow<List<WorkoutTemplateEntity>> = templateDao.getWorkoutTemplates(getCurrentUserId())

    suspend fun insertWorkoutLog(log: WorkoutEntity) {
        workoutDao.insertWorkoutLog(log.copy(userId = getCurrentUserId()))
    }

    suspend fun updateWorkoutLog(log: WorkoutEntity) {
        workoutDao.updateWorkoutLog(log.copy(userId = getCurrentUserId()))
    }

    suspend fun saveCustomExercise(exercise: CustomExerciseEntity) {
        customExerciseDao.insertCustomExercise(exercise.copy(userId = getCurrentUserId()))
    }

    suspend fun deleteCustomExercise(exercise: CustomExerciseEntity) {
        customExerciseDao.deleteCustomExercise(exercise)
    }

    suspend fun saveWorkoutTemplate(template: WorkoutTemplateEntity) {
        templateDao.insertTemplate(template.copy(userId = getCurrentUserId()))
    }

    suspend fun deleteWorkoutTemplate(template: WorkoutTemplateEntity) {
        templateDao.deleteTemplate(template)
    }
    
    suspend fun deleteWorkoutLog(log: WorkoutEntity) {
        workoutDao.deleteWorkoutLog(log)
    }

    suspend fun deleteWorkoutByGroup(date: String, workoutName: String) {
        workoutDao.deleteWorkoutByGroup(getCurrentUserId(), date, workoutName)
    }

    suspend fun deleteAllHistory() {
        workoutDao.deleteAllHistory(getCurrentUserId())
    }

    fun getTodayDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
