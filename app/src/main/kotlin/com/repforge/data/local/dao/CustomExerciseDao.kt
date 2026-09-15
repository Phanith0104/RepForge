package com.repforge.data.local.dao

import androidx.room.*
import com.repforge.data.local.entities.CustomExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomExerciseDao {
    @Query("SELECT * FROM custom_exercises WHERE userId = :userId ORDER BY name ASC")
    fun getCustomExercises(userId: String): Flow<List<CustomExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomExercise(exercise: CustomExerciseEntity)

    @Delete
    suspend fun deleteCustomExercise(exercise: CustomExerciseEntity)
}
