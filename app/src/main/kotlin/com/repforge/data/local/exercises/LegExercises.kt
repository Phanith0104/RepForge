package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val LEG_EXERCISES = listOf(
    // Quads (Front)
    Exercise(
        "Barbell Squats",
        "https://placehold.co/600x400/00A3FF/white?text=Squats",
        "Leg Press / Hack Squat / Goblet Squat",
        "The king of lower body exercises, primary quad focus.",
        MuscleGroup.LEGS,
        "Quads"
    ),
    Exercise(
        "Leg Extensions",
        "https://placehold.co/600x400/00A3FF/white?text=Extensions",
        "Sissy Squat / Bulgarian Split Squat",
        "Pure isolation for the quadriceps.",
        MuscleGroup.LEGS,
        "Quads"
    ),
    Exercise(
        "Bulgarian Split Squats",
        "https://placehold.co/600x400/00A3FF/white?text=Split+Squats",
        "Lunges / Single Leg Press",
        "Incredible for quad mass and stability.",
        MuscleGroup.LEGS,
        "Quads"
    ),
    Exercise(
        "Hack Squats",
        "https://placehold.co/600x400/00A3FF/white?text=Hack+Squat",
        "V-Squat / Leg Press",
        "Allows for heavy loading while minimizing lower back strain.",
        MuscleGroup.LEGS,
        "Quads"
    ),
    // Hamstrings (Back)
    Exercise(
        "Lying Leg Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Leg+Curls",
        "Seated Leg Curls / Nordic Curls",
        "Isolates the hamstrings for better knee stability and mass.",
        MuscleGroup.LEGS,
        "Hamstrings"
    ),
    Exercise(
        "Romanian Deadlift",
        "https://placehold.co/600x400/00A3FF/white?text=RDL",
        "Stiff Leg Deadlift / Glute Ham Raise",
        "Builds explosive hamstring and glute power.",
        MuscleGroup.LEGS,
        "Hamstrings"
    ),
    Exercise(
        "Seated Leg Curls",
        "https://placehold.co/600x400/00A3FF/white?text=Seated+Curl",
        "Lying Curls / Glute Bridges",
        "Puts the hamstrings in a stretched position for better growth.",
        MuscleGroup.LEGS,
        "Hamstrings"
    ),
    // Calves
    Exercise(
        "Standing Calf Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Calf+Raises",
        "Seated Calf Raise / Donkey Calf Raise",
        "Targets the gastrocnemius (outer calf).",
        MuscleGroup.LEGS,
        "Calves"
    ),
    Exercise(
        "Seated Calf Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Seated+Calf+Raise",
        "Leg Press Calf Raise",
        "Emphasizes the soleus muscle for calf thickness.",
        MuscleGroup.LEGS,
        "Calves"
    ),
    Exercise(
        "Tibialis Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Tibialis",
        "Heel Walks",
        "Strengthens the front of the lower leg to prevent injury.",
        MuscleGroup.LEGS,
        "Calves"
    )
)

