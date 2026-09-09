package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val CHEST_EXERCISES = listOf(
    // Upper Chest
    Exercise(
        "Incline Barbell Bench Press",
        "https://placehold.co/600x400/00A3FF/white?text=Incline+Barbell",
        "Incline Dumbbell Press / Low-to-High Cable Flys",
        "The primary compound movement for the upper chest.",
        MuscleGroup.CHEST,
        "Upper Chest"
    ),
    Exercise(
        "Incline Dumbbell Flys",
        "https://placehold.co/600x400/00A3FF/white?text=Incline+Fly",
        "Incline Cable Flys / Pec Deck (High Seat)",
        "Isolates the upper chest while providing a deep stretch.",
        MuscleGroup.CHEST,
        "Upper Chest"
    ),
    Exercise(
        "Low-to-High Cable Flys",
        "https://placehold.co/600x400/00A3FF/white?text=Cable+Flys",
        "Incline Dumbbell Flys",
        "Targets the upper chest and inner pec line.",
        MuscleGroup.CHEST,
        "Upper Chest"
    ),
    // Middle Chest
    Exercise(
        "Flat Barbell Bench Press",
        "https://placehold.co/600x400/00A3FF/white?text=Flat+Barbell",
        "Dumbbell Bench Press / Push-ups",
        "The gold standard for middle pectorals and overall chest mass.",
        MuscleGroup.CHEST,
        "Middle Chest"
    ),
    Exercise(
        "Machine Chest Press",
        "https://placehold.co/600x400/00A3FF/white?text=Machine+Press",
        "Hammer Strength Press / Floor Press",
        "Provides stability to focus purely on the chest contraction.",
        MuscleGroup.CHEST,
        "Middle Chest"
    ),
    Exercise(
        "Dumbbell Bench Press",
        "https://placehold.co/600x400/00A3FF/white?text=DB+Bench",
        "Flat Barbell Press",
        "Allows for a greater range of motion than the barbell.",
        MuscleGroup.CHEST,
        "Middle Chest"
    ),
    Exercise(
        "Floor Press",
        "https://placehold.co/600x400/00A3FF/white?text=Floor+Press",
        "Board Press",
        "Builds explosive power and hits the middle chest/triceps.",
        MuscleGroup.CHEST,
        "Middle Chest"
    ),
    // Lower Chest
    Exercise(
        "Decline Dumbbell Press",
        "https://placehold.co/600x400/00A3FF/white?text=Decline+Press",
        "Decline Barbell Press / High-to-Low Cable Flys",
        "Targets the costal head of the pectoralis major.",
        MuscleGroup.CHEST,
        "Lower Chest"
    ),
    Exercise(
        "Dips (Chest Focus)",
        "https://placehold.co/600x400/00A3FF/white?text=Chest+Dips",
        "Decline Press / Push-ups with Elevated Feet",
        "Lean forward to emphasize the lower chest fibers.",
        MuscleGroup.CHEST,
        "Lower Chest"
    ),
    Exercise(
        "Dumbbell Pullovers",
        "https://placehold.co/600x400/00A3FF/white?text=Pullovers",
        "Cable Pullovers",
        "Expands the ribcage and hits the lower chest/serratus.",
        MuscleGroup.CHEST,
        "Lower Chest"
    ),
    Exercise(
        "High-to-Low Cable Flys",
        "https://placehold.co/600x400/00A3FF/white?text=Lower+Cable+Fly",
        "Decline DB Flys",
        "Excellent isolation for the lower pectoral fibers.",
        MuscleGroup.CHEST,
        "Lower Chest"
    )
)

