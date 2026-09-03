package com.repforge.data.local.exercises

import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

val SHOULDER_LEG_ABS_EXERCISES = listOf(
    // Shoulders
    Exercise(
        "Military Press",
        "https://placehold.co/600x400/00A3FF/white?text=Military+Press",
        "Dumbbell Press / Smith Machine Press",
        "The best overall shoulder mass builder.",
        MuscleGroup.SHOULDERS,
        "Front Delt"
    ),
    Exercise(
        "Cable Lateral Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Cable+Lateral",
        "Dumbbell Lateral Raises",
        "Provides constant tension for the side delts.",
        MuscleGroup.SHOULDERS,
        "Side Delt"
    ),
    Exercise(
        "Bent Over Reverse Flys",
        "https://placehold.co/600x400/00A3FF/white?text=Rear+Fly",
        "Rear Delt Rows",
        "Targets the posterior head of the deltoid.",
        MuscleGroup.SHOULDERS,
        "Rear Delt"
    ),
    // Legs
    Exercise(
        "Leg Press",
        "https://placehold.co/600x400/00A3FF/white?text=Leg+Press",
        "Squats / Hack Squats",
        "Great for heavy loading and quad development.",
        MuscleGroup.LEGS,
        "Quads"
    ),
    Exercise(
        "Romanian Deadlift",
        "https://placehold.co/600x400/00A3FF/white?text=RDL",
        "Lying Leg Curls / Stiff Leg Deadlift",
        "The best exercise for hamstring and glute development.",
        MuscleGroup.LEGS,
        "Hamstrings"
    ),
    Exercise(
        "Standing Calf Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Calf+Raise",
        "Donkey Calf Raises",
        "Targets the gastrocnemius (outer calf).",
        MuscleGroup.LEGS,
        "Calves"
    ),
    // Abs
    Exercise(
        "Hanging Leg Raises",
        "https://placehold.co/600x400/00A3FF/white?text=Leg+Raise",
        "Reverse Crunches / Captain's Chair",
        "The premier lower ab and core exercise.",
        MuscleGroup.ABS,
        "Lower Abs"
    ),
    Exercise(
        "Cable Crunches",
        "https://placehold.co/600x400/00A3FF/white?text=Cable+Crunch",
        "Standard Crunches / Ab Roller",
        "Allows for weighted resistance training for the six-pack.",
        MuscleGroup.ABS,
        "Upper Abs"
    ),
    Exercise(
        "Russian Twists",
        "https://placehold.co/600x400/00A3FF/white?text=Twists",
        "Woodchoppers / Side Planks",
        "Builds strong obliques and rotational power.",
        MuscleGroup.ABS,
        "Obliques"
    )
)
