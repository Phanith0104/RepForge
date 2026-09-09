package com.repforge.data.local.dao

import androidx.room.*
import com.repforge.data.local.entities.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM steps WHERE date = :date")
    suspend fun getStepsForDate(date: String): StepEntity?

    @Query("SELECT * FROM steps WHERE date = :date")
    fun getStepsForDateFlow(date: String): Flow<StepEntity?>

    @Query("SELECT * FROM steps ORDER BY date DESC LIMIT 1")
    fun getLatestSteps(): Flow<StepEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSteps(stepEntity: StepEntity)
    
    @Query("SELECT * FROM steps ORDER BY date DESC")
    fun getAllSteps(): Flow<List<StepEntity>>

    @Query("SELECT MAX(count) FROM steps WHERE date != :today")
    suspend fun getPersonalBestExcludingToday(today: String): Int?
}
