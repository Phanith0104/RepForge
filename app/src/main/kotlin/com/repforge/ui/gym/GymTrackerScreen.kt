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

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymTrackerScreen(viewModel: GymViewModel = hiltViewModel()) {
    var showLogDialog by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    
    val navState by viewModel.navState.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val exercises by viewModel.currentExercises.collectAsState()
    val workoutLogs by viewModel.workoutLogs.collectAsState()

    BackHandler(enabled = navState !is GymViewModel.GymNavState.MuscleGroups) {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val state = navState) {
                            is GymViewModel.GymNavState.MuscleGroups -> "Gym Tracker"
                            is GymViewModel.GymNavState.SubCategories -> state.muscleGroup.displayName
                            is GymViewModel.GymNavState.Exercises -> state.subCategory
                        }
                    )
                },
                navigationIcon = {
                    if (navState !is GymViewModel.GymNavState.MuscleGroups) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = navState) {
                is GymViewModel.GymNavState.MuscleGroups -> {
                    MuscleGroupList(onCategoryClick = { viewModel.navigateToSubCategories(it) })
                }
                is GymViewModel.GymNavState.SubCategories -> {
                    FolderList(
                        items = subCategories,
                        onItemClick = { viewModel.navigateToExercises(state.muscleGroup, it) }
                    )
                }
                is GymViewModel.GymNavState.Exercises -> {
                    ExerciseList(
                        exercises = exercises,
                        onExerciseClick = {
                            selectedExercise = it
                            showLogDialog = true
                        }
                    )
                }
            }

            if (navState is GymViewModel.GymNavState.MuscleGroups && workoutLogs.isNotEmpty()) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(workoutLogs) { log ->
                        LogCard(log)
                    }
                }
            }
        }
    }

    if (showLogDialog && selectedExercise != null) {
        WorkoutEntryDialog(
            exercise = selectedExercise!!,
            onDismiss = { showLogDialog = false },
            onConfirm = { sets, reps, weight ->
                viewModel.logWorkout(selectedExercise!!.name, sets, reps, weight)
                showLogDialog = false
            }
        )
    }
}

@Composable
fun MuscleGroupList(onCategoryClick: (MuscleGroup) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(MuscleGroup.entries) { group ->
            FolderItem(
                title = group.displayName,
                onClick = { onCategoryClick(group) }
            )
        }
    }
}

@Composable
fun FolderList(items: List<String>, onItemClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items) { item ->
            FolderItem(
                title = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun FolderItem(title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Gray
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun ExerciseList(exercises: List<Exercise>, onExerciseClick: (Exercise) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(exercises) { exercise ->
            ExerciseCard(exercise = exercise, onLogClick = { onExerciseClick(exercise) })
        }
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
