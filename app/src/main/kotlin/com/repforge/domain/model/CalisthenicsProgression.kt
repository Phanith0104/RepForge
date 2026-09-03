package com.repforge.domain.model

data class CalisthenicsProgression(
    val levelName: String,
    val exercises: List<String>,
    val description: String
)

val CALISTHENICS_LEVELS = listOf(
    CalisthenicsProgression(
        levelName = "Basics",
        exercises = listOf("Wall Pushups", "Plank", "Squats", "Assisted Pullups"),
        description = "Building the foundation of strength and endurance."
    ),
    CalisthenicsProgression(
        levelName = "Intermediate",
        exercises = listOf("Standard Pushups", "Dips", "Pullups", "Leg Raises"),
        description = "Mastering bodyweight control and compound movements."
    ),
    CalisthenicsProgression(
        levelName = "Advanced",
        exercises = listOf("Muscle Ups", "Handstand Pushups", "Pistol Squats", "Front Lever"),
        description = "Elite level bodyweight strength and skills."
    )
)
