package com.repforge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workout_logs")
data class WorkoutEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val workoutName: String = "Quick Workout",
    val exerciseName: String,
    val date: String, // YYYY-MM-DD
    val sets: Int,
    val reps: Int,
    val weight: Float? = null,
    val durationMinutes: Int? = null,
    val restTimeSeconds: Int? = null,
    val notes: String? = null,
    val targetWeight: Float? = null,
    val targetReps: Int? = null,
    val displayOrder: Int = 0,
    val isCompleted: Boolean = true
)
