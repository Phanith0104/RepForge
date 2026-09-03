package com.repforge.ui.gym

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup

@Composable
fun GymTrackerScreen(viewModel: GymViewModel = hiltViewModel()) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    val workoutLogs by viewModel.workoutLogs.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Gym Tracker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        ScrollableTabRow(
            selectedTabIndex = MuscleGroup.entries.indexOf(selectedCategory),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            MuscleGroup.entries.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { viewModel.selectCategory(category) },
                    text = { Text(category.displayName) }
                )
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text(
                    text = "${selectedCategory.displayName} Exercises",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Group by sub-category
            val groupedExercises = exercises.groupBy { it.subCategory }
            
            groupedExercises.forEach { (subCategory, subExercises) ->
                item {
                    Text(
                        text = subCategory,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                items(subExercises) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onLogClick = {
                            selectedExercise = exercise
                            showDialog = true
                        }
                    )
                }
            }

            if (workoutLogs.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(workoutLogs) { log ->
                    LogCard(log)
                }
            }
        }
    }

    if (showDialog && selectedExercise != null) {
        WorkoutEntryDialog(
            exercise = selectedExercise!!,
            onDismiss = { showDialog = false },
            onConfirm = { sets, reps, weight ->
                viewModel.logWorkout(selectedExercise!!.name, sets, reps, weight)
                showDialog = false
            }
        )
    }
}

@Composable
fun LogCard(log: com.repforge.data.local.entities.WorkoutEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = log.exerciseName, style = MaterialTheme.typography.titleMedium)
                Text(text = log.date, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${log.sets} sets x ${log.reps} reps${log.weight?.let { " @ ${it}kg" } ?: ""}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onLogClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = exercise.imageUrl,
                contentDescription = exercise.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = exercise.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Alternative: ${exercise.alternative}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onLogClick, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Set")
                }
            }
        }
    }
}

@Composable
fun WorkoutEntryDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Float?) -> Unit
) {
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val isValid = sets.toIntOrNull() != null && sets.toInt() > 0 &&
            reps.toIntOrNull() != null && reps.toInt() > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log ${exercise.name}") },
        text = {
            Column {
                OutlinedTextField(
                    value = sets,
                    onValueChange = { if (it.all { char -> char.isDigit() }) sets = it },
                    label = { Text("Sets") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = sets.isNotEmpty() && sets.toIntOrNull() == null
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { if (it.all { char -> char.isDigit() }) reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = reps.isNotEmpty() && reps.toIntOrNull() == null
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) weight = it },
                    label = { Text("Weight (kg) - Optional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = sets.toInt()
                    val r = reps.toInt()
                    val w = weight.toFloatOrNull()
                    onConfirm(s, r, w)
                },
                enabled = isValid
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
