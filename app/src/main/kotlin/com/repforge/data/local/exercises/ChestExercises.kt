package com.repforge.data.local.exercises

import com.repforge.R
import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val CHEST_EXERCISES = listOf(
    // Upper Chest
    Exercise(
        "Incline Barbell Bench Press",
        R.drawable.chest,
        "Incline Dumbbell Press / Low-to-High Cable Flys",
        "The primary compound movement for the upper chest.",
        MuscleGroup.CHEST,
        "Upper Chest"
    ),
    Exercise(
        "Incline Dumbbell Flys",
        R.drawable.chest,
        "Incline Cable Flys / Pec Deck (High Seat)",
        "Isolates the upper chest while providing a deep stretch.",
        MuscleGroup.CHEST,
        "Upper Chest"
    ),
    Exercise(
        "Low-to-High Cable Flys",
        R.drawable.chest,
        "Incline Dumbbell Flys",
        "Targets the upper chest and inner pec line.",
        MuscleGroup.CHEST,
        "Upper Chest"
    ),
    // Middle Chest
    Exercise(
        "Flat Barbell Bench Press",
        R.drawable.chest,
        "Dumbbell Bench Press / Push-ups",
        "The gold standard for middle pectorals and overall chest mass.",
        MuscleGroup.CHEST,
        "Middle Chest"
    ),
    Exercise(
        "Machine Chest Press",
        R.drawable.chest,
        "Hammer Strength Press / Floor Press",
        "Provides stability to focus purely on the chest contraction.",
        MuscleGroup.CHEST,
        "Middle Chest"
    ),
    Exercise(
        "Dumbbell Bench Press",
        R.drawable.chest,
        "Flat Barbell Press",
        "Allows for a greater range of motion than the barbell.",
        MuscleGroup.CHEST,
        "Middle Chest"
    ),
    Exercise(
        "Floor Press",
        R.drawable.chest,
        "Board Press",
        "Builds explosive power and hits the middle chest/triceps.",
        MuscleGroup.CHEST,
        "Middle Chest"
    ),
    // Lower Chest
    Exercise(
        "Decline Dumbbell Press",
        R.drawable.chest,
        "Decline Barbell Press / High-to-Low Cable Flys",
        "Targets the costal head of the pectoralis major.",
        MuscleGroup.CHEST,
        "Lower Chest"
    ),
    Exercise(
        "Dips (Chest Focus)",
        R.drawable.chest,
        "Decline Press / Push-ups with Elevated Feet",
        "Lean forward to emphasize the lower chest fibers.",
        MuscleGroup.CHEST,
        "Lower Chest"
    ),
    Exercise(
        "Dumbbell Pullovers",
        R.drawable.chest,
        "Cable Pullovers",
        "Expands the ribcage and hits the lower chest/serratus.",
        MuscleGroup.CHEST,
        "Lower Chest"
    ),
    Exercise(
        "High-to-Low Cable Flys",
        R.drawable.chest,
        "Decline DB Flys",
        "Excellent isolation for the lower pectoral fibers.",
        MuscleGroup.CHEST,
        "Lower Chest"
    )
)
