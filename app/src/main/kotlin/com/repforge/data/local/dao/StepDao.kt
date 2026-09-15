package com.repforge.data.local.dao

import androidx.room.*
import com.repforge.data.local.entities.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM steps WHERE date = :date AND userId = :userId")
    suspend fun getStepsForDate(date: String, userId: String): StepEntity?

    @Query("SELECT * FROM steps WHERE date = :date AND userId = :userId")
    fun getStepsForDateFlow(date: String, userId: String): Flow<StepEntity?>

    @Query("SELECT * FROM steps WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    fun getLatestSteps(userId: String): Flow<StepEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSteps(stepEntity: StepEntity)
    
    @Query("SELECT * FROM steps WHERE userId = :userId ORDER BY date DESC")
    fun getAllSteps(userId: String): Flow<List<StepEntity>>

    @Query("SELECT * FROM steps WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllStepsList(userId: String): List<StepEntity>

    @Query("SELECT MAX(count) FROM steps WHERE date != :today AND userId = :userId")
    suspend fun getPersonalBestExcludingToday(today: String, userId: String): Int?

    @Query("DELETE FROM steps WHERE date = :date AND userId = :userId")
    suspend fun deleteStepsForDate(date: String, userId: String)
}
