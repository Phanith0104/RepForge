package com.repforge.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object StepCounter : Screen("step_counter", "Steps", Icons.AutoMirrored.Filled.DirectionsRun)
    object Training : Screen("training", "Training", Icons.Default.FitnessCenter)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Habits : Screen("habits", "Habits", Icons.Default.Home)
}
