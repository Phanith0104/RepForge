package com.repforge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val exercisesJson: String // Serialized List<TemplateExercise>
)

data class TemplateExercise(
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Float?
)
