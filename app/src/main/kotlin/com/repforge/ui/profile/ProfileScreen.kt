package com.repforge.ui.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.repforge.ui.auth.AuthViewModel
import com.repforge.ui.steps.StepViewModel
import com.repforge.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    stepViewModel: StepViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) }

    val user by viewModel.user.collectAsState()
    val isRefreshing by viewModel.isLoading.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val avatarSize = (screenWidth * 0.28f).coerceIn(80.dp, 160.dp)

    var isEditing by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }

    // Settings States
    var fitnessGoal by remember { mutableStateOf(prefs.getString("fitness_goal", "Gain Muscle") ?: "Gain Muscle") }
    val stepGoal by stepViewModel.stepGoal.collectAsState()
    var unitsPref by remember { mutableStateOf(prefs.getString("units_pref", "Metric (kg, cm)") ?: "Metric (kg, cm)") }
    var notificationsOn by remember { mutableStateOf(prefs.getBoolean("notifications_on", true)) }

    // Dialog Control States
    var showGoalDialog by remember { mutableStateOf(false) }
    var showStepTargetDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user, isEditing) {
        user?.let { u ->
            if (!isEditing) {
                nameInput = u.name
                heightInput = if (u.height > 0) u.height.toString() else ""
                weightInput = if (u.weight > 0) u.weight.toString() else ""
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Profile" else "Profile",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                IconButton(onClick = { showAccountDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            user?.let { u ->
                // Avatar & User Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (u.photoUrl != null) {
                            AsyncImage(
                                model = u.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(avatarSize * 0.6f),
                                tint = GoldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (u.name.isNotBlank()) u.name else "User",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = u.uniqueUserId,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Fitness Enthusiast",
                            fontSize = 12.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                nameInput = u.name
                                heightInput = if (u.height > 0) u.height.toString() else ""
                                weightInput = if (u.weight > 0) u.weight.toString() else ""
                                isEditing = !isEditing
                            },
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(if (isEditing) "Cancel" else "Edit Profile", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isEditing) {
                    // Body Measurements Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val h = u.height
                            val w = u.weight
                            if (h > 0 && w > 0) {
                                val hM = h / 100f
                                val bmi = w / (hM * hM)
                                val category = when {
                                    bmi < 18.5f -> "Underweight"
                                    bmi < 25.0f -> "Healthy"
                                    bmi < 30.0f -> "Overweight"
                                    else -> "Obese"
                                }
                                MeasurementItem("Height", "%.0f cm".format(h))
                                VerticalDivider(modifier = Modifier.height(36.dp), color = Color.DarkGray)
                                MeasurementItem("Weight", "%.0f kg".format(w))
                                VerticalDivider(modifier = Modifier.height(36.dp), color = Color.DarkGray)
                                MeasurementItem("BMI", "%.1f\n($category)".format(bmi))
                            } else {
                                MeasurementItem("Height", "Not set")
                                VerticalDivider(modifier = Modifier.height(36.dp), color = Color.DarkGray)
                                MeasurementItem("Weight", "Not set")
                                VerticalDivider(modifier = Modifier.height(36.dp), color = Color.DarkGray)
                                MeasurementItem("BMI", "N/A")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Settings & Preferences List (Interactive)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ProfileSettingRow(
                                title = "Fitness Goal",
                                subtitle = fitnessGoal,
                                icon = Icons.Default.AddTask,
                                onClick = { showGoalDialog = true }
                            )
                            ProfileSettingRow(
                                title = "Daily Step Target",
                                subtitle = "%,d steps".format(stepGoal),
                                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                                onClick = { showStepTargetDialog = true }
                            )
                            ProfileSettingRow(
                                title = "Units",
                                subtitle = unitsPref,
                                icon = Icons.Default.Straighten,
                                onClick = {
                                    unitsPref = if (unitsPref.startsWith("Metric")) "Imperial (lbs, in)" else "Metric (kg, cm)"
                                    prefs.edit().putString("units_pref", unitsPref).apply()
                                    Toast.makeText(context, "Units updated: $unitsPref", Toast.LENGTH_SHORT).show()
                                }
                            )
                            ProfileSettingRow(
                                title = "Notifications",
                                subtitle = if (notificationsOn) "On" else "Off",
                                icon = Icons.Default.Notifications,
                                onClick = {
                                    notificationsOn = !notificationsOn
                                    prefs.edit().putBoolean("notifications_on", notificationsOn).apply()
                                    Toast.makeText(context, "Notifications: ${if (notificationsOn) "On" else "Off"}", Toast.LENGTH_SHORT).show()
                                }
                            )
                            ProfileSettingRow(
                                title = "Account & Security",
                                subtitle = "",
                                icon = Icons.Default.Shield,
                                onClick = { showAccountDialog = true }
                            )
                            ProfileSettingRow(
                                title = "Sync with Cloud",
                                subtitle = "Synced",
                                icon = Icons.Default.CloudDone,
                                onClick = {
                                    viewModel.refresh()
                                    Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                } else {
                    // Edit Profile Fields
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { if (it.all { c -> (c.isDigit() || c == '.') }) heightInput = it },
                                label = { Text("Height (cm)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { if (it.all { c -> (c.isDigit() || c == '.') }) weightInput = it },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    viewModel.updateProfile(
                                        name = nameInput.trim(),
                                        age = u.age,
                                        gender = u.gender,
                                        phoneNumber = u.phoneNumber,
                                        weight = weightInput.toFloatOrNull() ?: u.weight,
                                        height = heightInput.toFloatOrNull() ?: u.height
                                    )
                                    isEditing = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Changes", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                    border = BorderStroke(1.dp, GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Progress to Zero", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sign Out Button
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Dialog 1: Fitness Goal Selection
    if (showGoalDialog) {
        val goals = listOf("Gain Muscle", "Lose Weight", "Maintain Fitness", "Build Strength", "Endurance")
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Select Fitness Goal") },
            text = {
                Column {
                    goals.forEach { goalOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    fitnessGoal = goalOption
                                    prefs.edit().putString("fitness_goal", goalOption).apply()
                                    showGoalDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = fitnessGoal == goalOption,
                                onClick = {
                                    fitnessGoal = goalOption
                                    prefs.edit().putString("fitness_goal", goalOption).apply()
                                    showGoalDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(goalOption, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog 2: Daily Step Target Selection
    if (showStepTargetDialog) {
        val stepTargets = listOf(5000, 8000, 10000, 12000)
        AlertDialog(
            onDismissRequest = { showStepTargetDialog = false },
            title = { Text("Select Daily Step Target") },
            text = {
                Column {
                    stepTargets.forEach { targetOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    stepViewModel.updateStepGoal(targetOption)
                                    showStepTargetDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = stepGoal == targetOption,
                                onClick = {
                                    stepViewModel.updateStepGoal(targetOption)
                                    showStepTargetDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("%,d steps".format(targetOption), fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStepTargetDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog 3: Account & Security
    if (showAccountDialog && user != null) {
        val u = user!!
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("Account & Security") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Email: ${u.email}", fontSize = 14.sp)
                    Text("Unique ID: ${u.uniqueUserId}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                    Text("Login Provider: ${u.provider}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (u.email.isNotBlank() && u.email.contains("@")) {
                                viewModel.sendPasswordResetEmail(u.email)
                                Toast.makeText(context, "Password reset email sent to ${u.email}", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Invalid email address", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                    ) {
                        Text("Send Password Reset Email")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountDialog = false }) { Text("Close") }
            }
        )
    }

    // Dialog 4: Reset Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Progress?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to reset all your workout history, step totals, and fitness progress to zero?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetUserProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset to Zero") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun MeasurementItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldPrimary, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProfileSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (subtitle.isNotBlank()) {
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        }
    }
}
