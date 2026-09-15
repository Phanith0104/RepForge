package com.repforge.data.local.dao

import androidx.room.*
import com.repforge.data.local.entities.WorkoutTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {
    @Query("SELECT * FROM workout_templates WHERE userId = :userId ORDER BY name ASC")
    fun getWorkoutTemplates(userId: String): Flow<List<WorkoutTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplateEntity)
}
