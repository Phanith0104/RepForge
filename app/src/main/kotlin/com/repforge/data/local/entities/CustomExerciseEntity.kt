package com.repforge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.repforge.domain.model.MuscleGroup
import java.util.UUID

@Entity(tableName = "custom_exercises")
data class CustomExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val musclePart: String,
    val equipment: String,
    val description: String = "",
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val defaultWeight: Float = 0f,
    val defaultDurationMinutes: Int = 0,
    val notes: String = ""
)
