package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val BICEP_EXERCISES = listOf(
    // Long Head (Peak)
    Exercise(
        "Incline Dumbbell Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Incline+Curls",
        "Barbell Curls / Cable Curls",
        "Stretches the long head for a better peak.",
        MuscleGroup.BICEPS,
        "Long Head"
    ),
    Exercise(
        "Hammer Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Hammer+Curls",
        "Rope Curls / Reverse Curls",
        "Targets the brachialis and the long head.",
        MuscleGroup.BICEPS,
        "Long Head"
    ),
    Exercise(
        "Drag Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Drag+Curls",
        "Barbell Curls",
        "Minimizes shoulder involvement to isolate the long head.",
        MuscleGroup.BICEPS,
        "Long Head"
    ),
    Exercise(
        "Bayesian Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Bayesian+Curls",
        "Behind-the-Back Cable Curls",
        "Provides extreme stretch on the long head.",
        MuscleGroup.BICEPS,
        "Long Head"
    ),
    // Short Head (Inner thickness)
    Exercise(
        "Preacher Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Preacher+Curls",
        "Spider Curls",
        "Isolates the short head for inner bicep thickness.",
        MuscleGroup.BICEPS,
        "Short Head"
    ),
    Exercise(
        "Spider Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Spider+Curls",
        "Concentration Curls",
        "Provides a massive contraction for the short head.",
        MuscleGroup.BICEPS,
        "Short Head"
    ),
    Exercise(
        "Concentration Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Conc+Curls",
        "One Arm Cable Curls",
        "Classic isolation for the short head.",
        MuscleGroup.BICEPS,
        "Short Head"
    ),
    Exercise(
        "Wide Grip Barbell Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Wide+Curls",
        "Dumbbell Curls (Palms out)",
        "Puts more emphasis on the inner (short) head.",
        MuscleGroup.BICEPS,
        "Short Head"
    )
)

