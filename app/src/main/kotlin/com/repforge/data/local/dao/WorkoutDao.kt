package com.repforge.data.local.dao

import androidx.room.*
import com.repforge.data.local.entities.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_logs WHERE userId = :userId AND isCompleted = 1 ORDER BY date DESC")
    fun getAllCompletedWorkouts(userId: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_logs WHERE userId = :userId AND isCompleted = 0 ORDER BY date ASC")
    fun getAllPlannedWorkouts(userId: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_logs WHERE userId = :userId AND date = :date AND isCompleted = 0")
    fun getPlannedWorkoutsForDate(userId: String, date: String): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(workoutLog: WorkoutEntity)

    @Update
    suspend fun updateWorkoutLog(workoutLog: WorkoutEntity)

    @Delete
    suspend fun deleteWorkoutLog(workoutLog: WorkoutEntity)

    @Query("DELETE FROM workout_logs WHERE userId = :userId AND isCompleted = 1")
    suspend fun deleteAllHistory(userId: String)

    @Query("DELETE FROM workout_logs WHERE userId = :userId AND date = :date AND workoutName = :workoutName")
    suspend fun deleteWorkoutByGroup(userId: String, date: String, workoutName: String)
}
