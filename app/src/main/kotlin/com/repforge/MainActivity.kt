package com.repforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.repforge.navigation.Screen
import com.repforge.ui.calisthenics.CalisthenicsScreen
import com.repforge.ui.gym.GymTrackerScreen
import com.repforge.ui.steps.StepScreen
import com.repforge.ui.theme.RepForgeTheme
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.os.Build
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.stepcounter.StepCounterService
import com.repforge.ui.auth.AuthViewModel
import com.repforge.ui.auth.LoginScreen
import com.repforge.ui.profile.ProfileScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepForgeTheme {
                val context = LocalContext.current
                val authViewModel: AuthViewModel = hiltViewModel()
                val userState by authViewModel.user.collectAsState()

                val permissionsToRequest = mutableListOf<String>().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        add(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                ) { permissions ->
                    val activityGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissions[Manifest.permission.ACTIVITY_RECOGNITION] == true
                    } else true
                    
                    if (activityGranted) {
                        context.startForegroundService(Intent(context, StepCounterService::class.java))
                    }
                }

                LaunchedEffect(userState?.isProfileComplete) {
                    if (userState?.isProfileComplete == true) {
                        val needsRequest = permissionsToRequest.any {
                            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                        }

                        if (needsRequest) {
                            permissionLauncher.launch(permissionsToRequest.toTypedArray())
                        } else {
                            context.startForegroundService(Intent(context, StepCounterService::class.java))
                        }
                    }
                }

                if (userState?.isProfileComplete == true) {
                    MainScreen()
                } else {
                    LoginScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val screens = listOf(Screen.StepCounter, Screen.GymTracker, Screen.Calisthenics, Screen.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.StepCounter.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.StepCounter.route) { 
                StepScreen() 
            }
            composable(Screen.GymTracker.route) { 
                GymTrackerScreen() 
            }
            composable(Screen.Calisthenics.route) { 
                CalisthenicsScreen() 
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
