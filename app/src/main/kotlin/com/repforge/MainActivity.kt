package com.repforge

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.navigation.Screen
import com.repforge.stepcounter.StepCounterService
import com.repforge.ui.auth.AuthViewModel
import com.repforge.ui.auth.LoginScreen
import com.repforge.ui.profile.ProfileScreen
import com.repforge.ui.theme.RepForgeTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.repforge.ui.calisthenics.CalisthenicsScreen
import com.repforge.ui.gym.GymTrackerScreen
import com.repforge.ui.steps.StepScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepForgeTheme {
                val showSplash = rememberSaveable { mutableStateOf(true) }
                
                if (showSplash.value) {
                    SplashScreen { showSplash.value = false }
                } else {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.0f) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val iconSize = (screenWidth * 0.6f).coerceIn(180.dp, 350.dp)
    
    LaunchedEffect(Unit) {
        // Zoom in to zoom out animation (dramatic entrance)
        scale.animateTo(
            targetValue = 1.5f,
            animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .scale(scale.value)
        )
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
    val userState by authViewModel.user.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    var showPermissionRationale by remember { mutableStateOf(false) }

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
            try {
                context.startForegroundService(Intent(context, StepCounterService::class.java))
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to start StepCounterService", e)
            }
        }
    }

    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userState?.isProfileComplete) {
        if (userState?.isProfileComplete == true) {
            val missingPermissions = permissionsToRequest.filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                showPermissionRationale = true
            } else {
                try {
                    context.startForegroundService(Intent(context, StepCounterService::class.java))
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to start StepCounterService", e)
                }
            }
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Permissions Required") },
            text = { Text("RepForge needs Activity Recognition to track your steps and Notifications to keep you updated. These are essential for the fitness tracking features.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionRationale = false
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                }) { Text("Grant") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPermissionRationale = false 
                    showSettingsDialog = true
                }) { Text("Settings") }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Open Settings") },
            text = { Text("Permissions were denied. To enable step tracking, please go to App Settings and grant Activity Recognition and Notifications.") },
            confirmButton = {
                Button(onClick = {
                    showSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) { Text("Go to Settings") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (userState?.isProfileComplete == true) {
        MainScreen()
    } else {
        LoginScreen()
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
