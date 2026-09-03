package com.repforge.domain.model

data class Exercise(
    val name: String,
    val imageUrl: String,
    val alternative: String,
    val description: String,
    val muscleGroup: MuscleGroup,
    val subCategory: String
)

