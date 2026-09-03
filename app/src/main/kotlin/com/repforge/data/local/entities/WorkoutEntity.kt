package com.repforge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workout_logs")
data class WorkoutEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val exerciseName: String,
    val date: String, // YYYY-MM-DD
    val sets: Int,
    val reps: Int,
    val weight: Float? = null
)
