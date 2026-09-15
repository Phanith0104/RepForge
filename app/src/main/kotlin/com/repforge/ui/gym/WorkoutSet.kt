package com.repforge.ui.gym

data class WorkoutSet(
    val reps: Int,
    val weight: Float?,
    val restTimeSeconds: Int? = null,
    val notes: String? = null
)
