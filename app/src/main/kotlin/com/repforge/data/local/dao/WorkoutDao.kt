package com.repforge.data.local.dao

import androidx.room.*
import com.repforge.data.local.entities.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_logs ORDER BY date DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(workoutLog: WorkoutEntity)

    @Delete
    suspend fun deleteWorkoutLog(workoutLog: WorkoutEntity)
}
