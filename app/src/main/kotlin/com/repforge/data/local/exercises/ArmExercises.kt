package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val ARM_EXERCISES = listOf(
    // Biceps - Long Head
    Exercise(
        "Barbell Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Barbell+Curl",
        "Dumbbell Curls / EZ Bar Curls",
        "Mass builder for overall biceps, emphasizes long head.",
        MuscleGroup.BICEPS,
        "Long Head"
    ),
    Exercise(
        "Hammer Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Hammer+Curl",
        "Rope Cable Curls",
        "Targets the brachialis and the long head.",
        MuscleGroup.BICEPS,
        "Long Head"
    ),
    // Biceps - Short Head
    Exercise(
        "Spider Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Spider+Curl",
        "Preacher Curls",
        "Puts the bicep in a shortened position to target the inner head.",
        MuscleGroup.BICEPS,
        "Short Head"
    ),
    Exercise(
        "Concentration Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Conc+Curl",
        "One Arm Cable Curls",
        "Excellent isolation for the bicep peak.",
        MuscleGroup.BICEPS,
        "Short Head"
    ),
    // Triceps
    Exercise(
        "Skull Crushers",
        "https://placehold.co/600x400/00A3FF/white?text=Skull+Crush",
        "Overhead DB Extension / JM Press",
        "Massive long head builder.",
        MuscleGroup.TRICEPS,
        "Long Head"
    ),
    Exercise(
        "Tricep Rope Pushdowns",
        "https://placehold.co/600x400/00A3FF/white?text=Rope+Pushdown",
        "Straight Bar Pushdowns",
        "Isolates the lateral head for tricep width.",
        MuscleGroup.TRICEPS,
        "Lateral Head"
    ),
    Exercise(
        "Bench Dips",
        "https://placehold.co/600x400/00A3FF/white?text=Bench+Dips",
        "Close Grip Pushups",
        "Targets the medial head and overall tricep mass.",
        MuscleGroup.TRICEPS,
        "Medial Head"
    ),
    // Forearms
    Exercise(
        "Behind the Back Wrist Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Wrist+Curl",
        "Seated Wrist Curls",
        "Isolates the forearm flexors.",
        MuscleGroup.FOREARMS,
        "Flexors"
    ),
    Exercise(
        "Reverse Barbell Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Rev+Curl",
        "Reverse Wrist Curls",
        "Targets the brachioradialis and extensors.",
        MuscleGroup.FOREARMS,
        "Extensors"
    )
)
