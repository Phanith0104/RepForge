package com.repforge.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object StepCounter : Screen("step_counter", "Steps", Icons.Default.DirectionsRun)
    object GymTracker : Screen("gym_tracker", "Gym", Icons.Default.FitnessCenter)
    object Calisthenics : Screen("calisthenics", "Calisthenics", Icons.Default.List)
}
