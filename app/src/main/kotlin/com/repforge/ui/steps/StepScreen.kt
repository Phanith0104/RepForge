package com.repforge.ui.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.data.local.entities.StepEntity

@Composable
fun StepScreen(viewModel: StepViewModel = hiltViewModel()) {
    val stepData by viewModel.todaySteps.collectAsState()
    val history by viewModel.stepHistory.collectAsState()
    val weeklyAvg by viewModel.weeklyAverage.collectAsState()
    val monthlyAvg by viewModel.monthlyAverage.collectAsState()
    val isSensorAvailable = viewModel.isSensorAvailable
    val isBatteryOptimizationDisabled = viewModel.isBatteryOptimizationDisabled
    val currentGoal by viewModel.stepGoal.collectAsState()

    var selectedHistoryRecord by remember { mutableStateOf<StepEntity?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    
    val steps = stepData?.count ?: 0
    val streak = stepData?.streak ?: 0
    val isAchievement = stepData?.isAchievement ?: false
    val distance = stepData?.distanceKm ?: 0f
    val calories = stepData?.caloriesBurned ?: 0
    val activeTime = stepData?.activeTimeMinutes ?: 0
    
    val progress = (steps.toFloat() / currentGoal).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        // Streak Display - Top Right
        if (streak > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streak",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                if (!isSensorAvailable) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⚠️ Hardware Step Counter not detected.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This device lacks a physical step sensor. RepForge will try to use the accelerometer as a fallback, but tracking may be less accurate.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (isSensorAvailable && !isBatteryOptimizationDisabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "💡 Optimization Required",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "RepForge needs to run in the background. Please disable battery optimization to prevent your phone from stopping the step counter.",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (isAchievement) {
                    Text(
                        text = "🏆 New Achievement!",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFFFD700), // Gold
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Daily Steps",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 12.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.LightGray
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showGoalDialog = true }
                    ) {
                        Text(
                            text = steps.toString(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "/ $currentGoal", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Edit, contentDescription = "Edit Goal", modifier = Modifier.size(14.dp), tint = Color.Gray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Distance", value = "${"%.2f".format(distance)} km")
                    StatItem(label = "Calories", value = "$calories kcal")
                    StatItem(label = "Time", value = "${activeTime}m")
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                if (streak > 0) {
                    Text(
                        text = "You're on fire! Keep it up.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF5722)
                    )
                } else {
                    Text(
                        text = "Reach $currentGoal steps for a streak!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Averages Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Weekly Avg", style = MaterialTheme.typography.labelMedium)
                            Text(weeklyAvg.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Monthly Avg", style = MaterialTheme.typography.labelMedium)
                            Text(monthlyAvg.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider()
                
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            items(history) { record ->
                HistoryItem(record, onClick = { selectedHistoryRecord = record })
            }
        }
    }

    if (showGoalDialog) {
        GoalEditDialog(
            currentGoal = currentGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                viewModel.updateStepGoal(newGoal)
                showGoalDialog = false
            }
        )
    }

    selectedHistoryRecord?.let { record ->
        HistoryDetailDialog(
            record = record,
            onDismiss = { selectedHistoryRecord = null }
        )
    }
}

@Composable
fun GoalEditDialog(currentGoal: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var textValue by remember { mutableStateOf(currentGoal.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Step Goal") },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { if (it.all { char -> char.isDigit() }) textValue = it },
                label = { Text("Daily Steps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val goal = textValue.toIntOrNull() ?: 10000
                onConfirm(goal)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun HistoryDetailDialog(record: StepEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Details for ${record.date}", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                DetailRow(label = "Total Steps", value = record.count.toString())
                DetailRow(label = "Distance", value = "%.2f km".format(record.distanceKm))
                DetailRow(label = "Calories Burned", value = "${record.caloriesBurned} kcal")
                DetailRow(label = "Active Time", value = "${record.activeTimeMinutes} minutes")
                DetailRow(label = "Streak", value = "${record.streak} days")
                if (record.isAchievement) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "🏆 This was a Personal Best!", color = Color(0xFFB8860B), fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun HistoryItem(record: StepEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = record.date, style = MaterialTheme.typography.bodyMedium)
                Row {
                    if (record.isAchievement) {
                        Text(text = "🏆 Achievement ", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8860B))
                    }
                    Text(
                        text = "${record.distanceKm}km • ${record.caloriesBurned}kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Text(
                text = "${record.count}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
