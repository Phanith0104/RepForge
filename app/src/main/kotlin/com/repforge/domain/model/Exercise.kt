package com.repforge.domain.model

data class Exercise(
    val name: String,
    val imageUrl: Any, // Supports both String URL and Drawable Resource Int
    val alternative: String,
    val description: String,
    val muscleGroup: MuscleGroup,
    val subCategory: String
)
