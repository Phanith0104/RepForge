package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val SHOULDER_EXERCISES = listOf(
    // Front Delt
    Exercise(
        "Overhead Press (Military Press)",
        "https://placehold.co/600x400/00A3FF/white?text=OHP",
        "Dumbbell Shoulder Press / Front Raises",
        "The ultimate shoulder mass builder, primarily front delts.",
        MuscleGroup.SHOULDERS,
        "Front Delt"
    ),
    Exercise(
        "Front Plate Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Plate+Raises",
        "Alternating DB Front Raises",
        "Isolates the anterior deltoids.",
        MuscleGroup.SHOULDERS,
        "Front Delt"
    ),
    // Side Delt
    Exercise(
        "Dumbbell Lateral Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Lat+Raises",
        "Cable Lateral Raises / Upright Rows",
        "Essential for building shoulder width and the capped look.",
        MuscleGroup.SHOULDERS,
        "Side Delt"
    ),
    Exercise(
        "Cable Lateral Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Cable+Lat+Raises",
        "Machine Lateral Raises",
        "Provides constant tension across the entire range of motion.",
        MuscleGroup.SHOULDERS,
        "Side Delt"
    ),
    // Rear Delt
    Exercise(
        "Reverse Pec Deck",
        "https://placehold.co/600x400/00A3FF/white?text=Rev+Pec+Deck",
        "Bent Over Reverse Flys / Face Pulls",
        "Targets the rear deltoids for complete shoulder development.",
        MuscleGroup.SHOULDERS,
        "Rear Delt"
    ),
    Exercise(
        "Bent Over Dumbbell Flys",
        "https://placehold.co/600x400/00A3FF/white?text=Rear+Fly",
        "Face Pulls",
        "A great free-weight isolation for the rear delts.",
        MuscleGroup.SHOULDERS,
        "Rear Delt"
    )
)
