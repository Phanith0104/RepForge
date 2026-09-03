package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val BACK_EXERCISES = listOf(
    // Lats (Width)
    Exercise(
        "Lat Pulldowns",
        "https://placehold.co/600x400/00A3FF/white?text=Lat+Pulldown",
        "Pull Ups / Chin Ups",
        "Excellent for targeting the latissimus dorsi width.",
        MuscleGroup.BACK,
        "Lats"
    ),
    Exercise(
        "Straight Arm Pulldowns",
        "https://placehold.co/600x400/00A3FF/white?text=Straight+Arm",
        "Dumbbell Pullovers",
        "Isolates the lats without involving the biceps.",
        MuscleGroup.BACK,
        "Lats"
    ),
    // Middle Back (Thickness)
    Exercise(
        "One Arm Dumbbell Rows",
        "https://placehold.co/600x400/00A3FF/white?text=DB+Rows",
        "Seated Cable Rows / T-Bar Rows",
        "Allows for a greater range of motion and core stability.",
        MuscleGroup.BACK,
        "Middle Back"
    ),
    Exercise(
        "Seated Cable Rows",
        "https://placehold.co/600x400/00A3FF/white?text=Cable+Rows",
        "Bent Over Barbell Rows",
        "Builds thick middle back and rhomboids.",
        MuscleGroup.BACK,
        "Middle Back"
    ),
    // Upper Back
    Exercise(
        "Face Pulls",
        "https://placehold.co/600x400/00A3FF/white?text=Face+Pull",
        "Rear Delt Flys / Band Pull-aparts",
        "Crucial for shoulder health and upper back detail.",
        MuscleGroup.BACK,
        "Upper Back"
    ),
    Exercise(
        "Shrugs",
        "https://placehold.co/600x400/00A3FF/white?text=Shrugs",
        "Farmer's Walks / Upright Rows",
        "Develops the upper trapezius muscles.",
        MuscleGroup.BACK,
        "Upper Back"
    ),
    // Lower Back
    Exercise(
        "Deadlift (Conventional)",
        "https://placehold.co/600x400/00A3FF/white?text=Deadlift",
        "Rack Pulls / Summo Deadlift",
        "The ultimate posterior chain and lower back builder.",
        MuscleGroup.BACK,
        "Lower Back"
    ),
    Exercise(
        "Hyperextensions",
        "https://placehold.co/600x400/00A3FF/white?text=Hyperextension",
        "Good Mornings / Bird Dogs",
        "Isolates the spinal erectors safely.",
        MuscleGroup.BACK,
        "Lower Back"
    )
)
