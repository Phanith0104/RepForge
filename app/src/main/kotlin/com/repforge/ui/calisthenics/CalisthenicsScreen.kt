package com.repforge.ui.calisthenics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.data.local.entities.WorkoutEntity
import com.repforge.domain.model.CALISTHENICS_LEVELS
import com.repforge.domain.model.CalisthenicsProgression
import com.repforge.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalisthenicsScreen(
    viewModel: CalisthenicsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val screenMode by viewModel.screenMode.collectAsState()
    val isRefreshing by authViewModel.isLoading.collectAsState()
    val calisthenicsHistory by viewModel.calisthenicsHistory.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 300
        }
    }

    var historyItemDetails by remember { mutableStateOf<WorkoutEntity?>(null) }
    var historyItemToEdit by remember { mutableStateOf<WorkoutEntity?>(null) }
    var historyItemToDelete by remember { mutableStateOf<WorkoutEntity?>(null) }

    if (selectedExercise == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    authViewModel.refresh()
                    viewModel.refresh()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Text(
                            text = "Calisthenics Mastery",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(CALISTHENICS_LEVELS) { progression ->
                        ProgressionCard(progression, onExerciseClick = { exerciseName ->
                            viewModel.selectExercise(exerciseName)
                        })
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Workout History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (calisthenicsHistory.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No Calisthenics workout history yet.",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Select an exercise above and complete a session to see your progress recorded here!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(calisthenicsHistory, key = { it.id }) { log ->
                            CalisthenicsHistoryCard(
                                log = log,
                                onClick = { historyItemDetails = log },
                                onDelete = { historyItemToDelete = log }
                            )
                        }
                    }
                }
            }

            // Scroll To Top Floating Action Button
            AnimatedVisibility(
                visible = showScrollToTop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
                }
            }
        }
    } else {
        val exerciseName = selectedExercise!!
        when (screenMode) {
            CalisthenicsScreenMode.CONFIG -> {
                CalisthenicsConfigView(exerciseName, viewModel)
            }
            CalisthenicsScreenMode.WORKOUT -> {
                CalisthenicsWorkoutView(exerciseName, viewModel)
            }
            CalisthenicsScreenMode.SUMMARY -> {
                CalisthenicsSummaryView(exerciseName, viewModel)
            }
        }
    }

    // Detail Dialog
    if (historyItemDetails != null) {
        val log = historyItemDetails!!
        AlertDialog(
            onDismissRequest = { historyItemDetails = null },
            title = { Text(log.exerciseName, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Date: ${log.date}", style = MaterialTheme.typography.bodyMedium)
                    Text("Sets Completed: ${log.sets}", style = MaterialTheme.typography.bodyMedium)
                    Text("Total Repetitions: ${log.reps}", style = MaterialTheme.typography.bodyMedium)
                    if (log.durationMinutes != null) {
                        Text("Exercise Duration: ${log.durationMinutes} min", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (log.restTimeSeconds != null && log.restTimeSeconds > 0) {
                        Text("Rest Duration: ${log.restTimeSeconds / 60}m ${log.restTimeSeconds % 60}s", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Status: ", style = MaterialTheme.typography.bodyMedium)
                        Badge(containerColor = Color(0xFF4CAF50)) {
                            Text("Completed", color = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                    if (!log.notes.isNullOrBlank()) {
                        Text("Notes: ${log.notes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    historyItemToEdit = log
                    historyItemDetails = null
                }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { historyItemDetails = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Edit Dialog
    if (historyItemToEdit != null) {
        val log = historyItemToEdit!!
        var setsEdit by remember { mutableStateOf(log.sets.toString()) }
        var repsEdit by remember { mutableStateOf(log.reps.toString()) }
        var durationEdit by remember { mutableStateOf((log.durationMinutes ?: 1).toString()) }

        AlertDialog(
            onDismissRequest = { historyItemToEdit = null },
            title = { Text("Edit ${log.exerciseName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = setsEdit,
                        onValueChange = { if (it.all { c -> c.isDigit() }) setsEdit = it },
                        label = { Text("Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = repsEdit,
                        onValueChange = { if (it.all { c -> c.isDigit() }) repsEdit = it },
                        label = { Text("Total Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = durationEdit,
                        onValueChange = { if (it.all { c -> c.isDigit() }) durationEdit = it },
                        label = { Text("Duration (Minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updatedLog = log.copy(
                        sets = setsEdit.toIntOrNull() ?: log.sets,
                        reps = repsEdit.toIntOrNull() ?: log.reps,
                        durationMinutes = durationEdit.toIntOrNull() ?: log.durationMinutes
                    )
                    viewModel.updateWorkoutLog(updatedLog)
                    historyItemToEdit = null
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { historyItemToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (historyItemToDelete != null) {
        val log = historyItemToDelete!!
        AlertDialog(
            onDismissRequest = { historyItemToDelete = null },
            title = { Text("Delete Workout Log?") },
            text = { Text("Are you sure you want to delete this workout history record for ${log.exerciseName} on ${log.date}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWorkoutLog(log)
                        historyItemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { historyItemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}



@Composable
fun CalisthenicsHistoryCard(
    log: WorkoutEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.exerciseName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(containerColor = Color(0xFF4CAF50)) {
                        Text("Completed", color = Color.White, modifier = Modifier.padding(2.dp), fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${log.sets} sets • ${log.reps} reps" +
                            (if (log.durationMinutes != null) " • ${log.durationMinutes} min" else "") +
                            (if (log.restTimeSeconds != null && log.restTimeSeconds > 0) " • ${log.restTimeSeconds}s rest" else ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = log.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete record",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProgressionCard(
    progression: CalisthenicsProgression,
    onExerciseClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (progression.levelName) {
                "Basics" -> Color(0xFFE8F5E9)
                "Intermediate" -> Color(0xFFFFF3E0)
                "Advanced" -> Color(0xFFFFEBEE)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = progression.levelName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = progression.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap an exercise to configure or start:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            progression.exercises.forEach { exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onExerciseClick(exercise) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = exercise,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Configure",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalisthenicsConfigView(
    exerciseName: String,
    viewModel: CalisthenicsViewModel
) {
    val sets by viewModel.setsInput.collectAsState()
    val reps by viewModel.repsInput.collectAsState()
    val durMin by viewModel.durationMinutesInput.collectAsState()
    val durSec by viewModel.durationSecondsInput.collectAsState()
    val restMin by viewModel.restMinutesInput.collectAsState()
    val restSec by viewModel.restSecondsInput.collectAsState()
    val saveStatusMessage by viewModel.saveStatusMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.closeExercise() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (saveStatusMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(saveStatusMessage!!, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sets Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Number of Sets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.updateSets(sets - 1) },
                        colors = IconButtonDefaults.filledIconButtonColors()
                    ) {
                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "$sets Sets",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { viewModel.updateSets(sets + 1) },
                        colors = IconButtonDefaults.filledIconButtonColors()
                    ) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reps per Set Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Target Repetitions Per Set", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.updateReps(reps - 1) },
                        colors = IconButtonDefaults.filledIconButtonColors()
                    ) {
                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "$reps Reps",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { viewModel.updateReps(reps + 1) },
                        colors = IconButtonDefaults.filledIconButtonColors()
                    ) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Exercise Target Duration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Exercise Completion Target Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = if (durMin == 0) "" else durMin.toString(),
                        onValueChange = { viewModel.updateDuration(it.toIntOrNull() ?: 0, durSec) },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = if (durSec == 0) "" else durSec.toString(),
                        onValueChange = { viewModel.updateDuration(durMin, it.toIntOrNull() ?: 0) },
                        label = { Text("Seconds") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Rest Time Between Sets Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rest Duration Between Sets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = if (restMin == 0) "" else restMin.toString(),
                        onValueChange = { viewModel.updateRestTime(it.toIntOrNull() ?: 0, restSec) },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = if (restSec == 0) "" else restSec.toString(),
                        onValueChange = { viewModel.updateRestTime(restMin, it.toIntOrNull() ?: 0) },
                        label = { Text("Seconds") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = { viewModel.startWorkout() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Workout Session")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.saveConfig() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Changes")
            }

            OutlinedButton(
                onClick = { viewModel.closeExercise() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun CalisthenicsWorkoutView(
    exerciseName: String,
    viewModel: CalisthenicsViewModel
) {
    val setReps by viewModel.setReps.collectAsState()
    val setCompleted by viewModel.setCompleted.collectAsState()
    val restSecRemaining by viewModel.restTimeRemainingSeconds.collectAsState()
    val isRestTimerRunning by viewModel.isRestTimerRunning.collectAsState()
    val isRestTimerPaused by viewModel.isRestTimerPaused.collectAsState()
    val workoutDurationSec by viewModel.workoutDurationSeconds.collectAsState()

    val formattedWorkoutTime = remember(workoutDurationSec) {
        val m = workoutDurationSec / 60
        val s = workoutDurationSec % 60
        "%02d:%02d".format(m, s)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.closeExercise() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedWorkoutTime,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rest Timer Component (When active)
        AnimatedVisibility(visible = isRestTimerRunning) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Rest Timer", style = MaterialTheme.typography.labelLarge, color = Color(0xFFE65100))
                    Text(
                        text = "%02d:%02d".format(restSecRemaining / 60, restSecRemaining % 60),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRestTimerPaused) {
                            Button(
                                onClick = { viewModel.resumeRestTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                            ) { Text("Resume") }
                        } else {
                            OutlinedButton(onClick = { viewModel.pauseRestTimer() }) { Text("Pause") }
                        }
                        TextButton(onClick = { viewModel.skipRestTimer() }) { Text("Skip Rest") }
                    }
                }
            }
        }

        Text(
            text = "Individual Sets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Set list
        setReps.forEachIndexed { index, reps ->
            val isDone = setCompleted.getOrElse(index) { false }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDone) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Set ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = reps.toString(),
                            onValueChange = { viewModel.updateSetReps(index, it.toIntOrNull() ?: 0) },
                            label = { Text("Reps") },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !isDone
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { viewModel.toggleSetCompletion(index) }
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Complete",
                                tint = if (isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.showWorkoutSummary() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.DoneAll, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Complete Exercise & View Summary")
        }
    }
}

@Composable
fun CalisthenicsSummaryView(
    exerciseName: String,
    viewModel: CalisthenicsViewModel
) {
    val setReps by viewModel.setReps.collectAsState()
    val setCompleted by viewModel.setCompleted.collectAsState()
    val workoutDurationSec by viewModel.workoutDurationSeconds.collectAsState()
    val restMin by viewModel.restMinutesInput.collectAsState()
    val restSec by viewModel.restSecondsInput.collectAsState()

    val totalCompletedSets = setCompleted.count { it }
    val totalRepsPerformed = setReps.filterIndexed { index, _ -> setCompleted.getOrElse(index) { false } }.sum()
    val timestamp = remember { SimpleDateFormat("EEEE, dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFFFB300)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Exercise Complete!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = exerciseName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow("Total Sets Completed", "$totalCompletedSets of ${setReps.size}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SummaryRow("Total Repetitions", "$totalRepsPerformed Reps")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SummaryRow("Exercise Duration", "%02d min %02d sec".format(workoutDurationSec / 60, workoutDurationSec % 60))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SummaryRow("Configured Rest / Set", "%02d min %02d sec".format(restMin, restSec))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SummaryRow("Completion Timestamp", timestamp)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { viewModel.finishWorkout() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Finish & Save to Workout History")
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
