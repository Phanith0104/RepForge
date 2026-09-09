package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val TRICEP_EXERCISES = listOf(
    // Long Head
    Exercise(
        "Overhead Dumbbell Extensions",
        "https://placehold.co/600x400/00A3FF/white?text=Overhead+Ext",
        "Skull Crushers / EZ Bar Overhead Press",
        "The best for stretching and building the long head.",
        MuscleGroup.TRICEPS,
        "Long Head"
    ),
    Exercise(
        "Skull Crushers",
        "https://placehold.co/600x400/00A3FF/white?text=Skull+Crushers",
        "JM Press / Tate Press",
        "A staple for long head mass.",
        MuscleGroup.TRICEPS,
        "Long Head"
    ),
    // Lateral Head
    Exercise(
        "Tricep Pushdowns (Straight Bar)",
        "https://placehold.co/600x400/00A3FF/white?text=Pushdowns",
        "Diamond Pushups / Dips",
        "Isolates the lateral head for the horseshoe look.",
        MuscleGroup.TRICEPS,
        "Lateral Head"
    ),
    Exercise(
        "Rope Pushdowns",
        "https://placehold.co/600x400/00A3FF/white?text=Rope+Pushdowns",
        "One Arm Cable Pushdowns",
        "Allows for a greater peak contraction in the lateral head.",
        MuscleGroup.TRICEPS,
        "Lateral Head"
    ),
    // Medial Head
    Exercise(
        "Reverse Grip Pushdowns",
        "https://placehold.co/600x400/00A3FF/white?text=Rev+Pushdown",
        "Underhand Cable Extensions",
        "Emphasizes the medial head of the tricep.",
        MuscleGroup.TRICEPS,
        "Medial Head"
    ),
    Exercise(
        "Close Grip Bench Press",
        "https://placehold.co/600x400/00A3FF/white?text=Close+Grip",
        "Weighted Dips / Diamond Pushups",
        "Compound movement that hits all heads, with medial focus.",
        MuscleGroup.TRICEPS,
        "Medial Head"
    )
)
