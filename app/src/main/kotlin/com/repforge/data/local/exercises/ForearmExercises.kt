package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val FOREARM_EXERCISES = listOf(
    // Flexors
    Exercise(
        "Seated Barbell Wrist Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Wrist+Curls",
        "Dumbbell Wrist Curls / Behind the Back Wrist Curls",
        "Builds the bulk of the inner forearm.",
        MuscleGroup.FOREARMS,
        "Flexors"
    ),
    Exercise(
        "Behind the Back Wrist Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Behind+Wrist+Curls",
        "Seated Curls",
        "Allows for heavy loading to build flexor strength.",
        MuscleGroup.FOREARMS,
        "Flexors"
    ),
    // Extensors
    Exercise(
        "Reverse Barbell Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Rev+Barbell+Curls",
        "Reverse Cable Curls / Zottman Curls",
        "Targets the brachioradialis and outer forearms.",
        MuscleGroup.FOREARMS,
        "Extensors"
    ),
    Exercise(
        "Reverse Wrist Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Rev+Wrist+Curls",
        "Plate Pinches / Farmer's Walks",
        "Isolates the forearm extensors for complete arm detail.",
        MuscleGroup.FOREARMS,
        "Extensors"
    )
)
