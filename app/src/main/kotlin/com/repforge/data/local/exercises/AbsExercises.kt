package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val ABS_EXERCISES = listOf(
    // Lower Abs
    Exercise(
        "Hanging Leg Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Leg+Raises",
        "Reverse Crunches / Flutter Kicks",
        "The gold standard for targeting the lower abdominal region.",
        MuscleGroup.ABS,
        "Lower Abs"
    ),
    Exercise(
        "Mountain Climbers",
        "https://placehold.co/600x400/00A3FF/white?text=Mt+Climbers",
        "Plank / Knee-to-Chest",
        "Engages lower abs while providing a cardio boost.",
        MuscleGroup.ABS,
        "Lower Abs"
    ),
    // Upper Abs
    Exercise(
        "Cable Crunches",
        "https://placehold.co/600x400/00A3FF/white?text=Cable+Crunches",
        "Weighted Sit-ups / Ab Roller",
        "Allows for progressive overload of the rectus abdominis.",
        MuscleGroup.ABS,
        "Upper Abs"
    ),
    Exercise(
        "Standard Crunches",
        "https://placehold.co/600x400/00A3FF/white?text=Crunches",
        "Heel Touches / Dead Bug",
        "Classic exercise for isolating the upper abs.",
        MuscleGroup.ABS,
        "Upper Abs"
    ),
    // Obliques
    Exercise(
        "Russian Twists",
        "https://placehold.co/600x400/00A3FF/white?text=Russian+Twists",
        "Woodchoppers / Side Planks",
        "Builds rotational strength and defined obliques.",
        MuscleGroup.ABS,
        "Obliques"
    ),
    Exercise(
        "Side Planks",
        "https://placehold.co/600x400/00A3FF/white?text=Side+Plank",
        "Bicycle Crunches",
        "Great for core stability and side-ab definition.",
        MuscleGroup.ABS,
        "Obliques"
    )
)
