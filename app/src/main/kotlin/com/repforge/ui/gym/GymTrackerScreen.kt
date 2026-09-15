package com.repforge.ui.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.repforge.data.local.entities.WorkoutEntity
import com.repforge.data.local.entities.WorkoutTemplateEntity
import com.repforge.domain.model.Exercise
import com.repforge.domain.model.MuscleGroup
import com.repforge.domain.model.MUSCLE_GROUPS_INFO
import androidx.activity.compose.BackHandler
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymTrackerScreen(viewModel: GymViewModel = hiltViewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val completedLogs by viewModel.completedWorkoutLogs.collectAsState()
    val plannedWorkouts by viewModel.plannedWorkouts.collectAsState()
    val plannedForDate by viewModel.plannedWorkoutsForSelectedDate.collectAsState()
    val todayPlan by viewModel.todayPlan.collectAsState()
    val navState by viewModel.navState.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val exercises by viewModel.currentExercises.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val workoutTemplates by viewModel.workoutTemplates.collectAsState()
    
    var workoutToEdit by remember { mutableStateOf<WorkoutEntity?>(null) }
    var workoutToDelete by remember { mutableStateOf<WorkoutEntity?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showApplyTemplateDialog by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf<List<WorkoutEntity>?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedTab == GymViewModel.GymTab.WORKOUT && navState !is GymViewModel.GymNavState.MuscleGroups) {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (selectedTab == GymViewModel.GymTab.CALENDAR) "Workout Planner" else "Gym Tracker") },
                    navigationIcon = {
                        if (selectedTab == GymViewModel.GymTab.CALENDAR) {
                            IconButton(onClick = { viewModel.selectTab(GymViewModel.GymTab.HISTORY) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (selectedTab != GymViewModel.GymTab.CALENDAR) {
                            IconButton(onClick = { viewModel.selectTab(GymViewModel.GymTab.CALENDAR) }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Open Calendar")
                            }
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Add Custom Exercise") },
                                    onClick = {
                                        showAddExerciseDialog = true
                                        menuExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Add, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Apply Template") },
                                    onClick = {
                                        showApplyTemplateDialog = true
                                        menuExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.FileCopy, null) }
                                )
                                if (selectedTab == GymViewModel.GymTab.HISTORY) {
                                    DropdownMenuItem(
                                        text = { Text("Delete All History", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showDeleteAllDialog = true
                                            menuExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                )
                if (selectedTab != GymViewModel.GymTab.CALENDAR) {
                    TabRow(selectedTabIndex = selectedTab.ordinal) {
                        Tab(
                            selected = selectedTab == GymViewModel.GymTab.HISTORY,
                            onClick = { viewModel.selectTab(GymViewModel.GymTab.HISTORY) },
                            text = { Text("HISTORY") }
                        )
                        Tab(
                            selected = selectedTab == GymViewModel.GymTab.WORKOUT,
                            onClick = { viewModel.selectTab(GymViewModel.GymTab.WORKOUT) },
                            text = { Text("WORKOUT") }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == GymViewModel.GymTab.WORKOUT && navState is GymViewModel.GymNavState.MuscleGroups) {
                FloatingActionButton(onClick = { showAddWorkoutDialog = true }) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = "Add Workout Plan")
                }
            } else if (selectedTab == GymViewModel.GymTab.HISTORY) {
                FloatingActionButton(onClick = { viewModel.selectTab(GymViewModel.GymTab.WORKOUT) }) {
                    Icon(Icons.Default.Add, contentDescription = "Start Workout")
                }
            } else if (selectedTab == GymViewModel.GymTab.CALENDAR) {
                FloatingActionButton(onClick = { showLogDialog = true; selectedExercise = null }) {
                    Icon(Icons.Default.EventNote, contentDescription = "Plan Workout")
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            when (selectedTab) {
                GymViewModel.GymTab.HISTORY -> {
                    HistorySection(
                        logs = completedLogs,
                        onEdit = { workoutToEdit = it },
                        onDelete = { workoutToDelete = it },
                        onSaveTemplate = { showSaveTemplateDialog = listOf(it) }
                    )
                }
                GymViewModel.GymTab.WORKOUT -> {
                    WorkoutSection(
                        navState = navState,
                        subCategories = subCategories,
                        exercises = exercises,
                        todayPlan = todayPlan,
                        onMuscleGroupClick = { viewModel.navigateToSubCategories(it) },
                        onSubCategoryClick = { group, sub -> viewModel.navigateToExercises(group, sub) },
                        onExerciseClick = {
                            selectedExercise = it
                            showLogDialog = true
                        },
                        onBackClick = { viewModel.navigateBack() },
                        onStartTodayPlan = {
                            // If today has plans, we could pre-fill them. 
                            // For simplicity, we just navigate to Exercises of those muscle groups if needed.
                        }
                    )
                }
                GymViewModel.GymTab.CALENDAR -> {
                    CalendarSection(
                        selectedDate = selectedDate,
                        plannedWorkouts = plannedWorkouts,
                        completedLogs = completedLogs,
                        plannedForDate = plannedForDate,
                        onDateSelected = { viewModel.selectDate(it) },
                        onEdit = { workoutToEdit = it },
                        onDelete = { workoutToDelete = it },
                        onComplete = { viewModel.updateWorkout(it.copy(isCompleted = true)) },
                        onSaveTemplate = { showSaveTemplateDialog = plannedForDate }
                    )
                }
            }
        }
    }

    if (workoutToEdit != null) {
        EditWorkoutDialog(
            workout = workoutToEdit!!,
            onDismiss = { workoutToEdit = null },
            onSave = { updated ->
                viewModel.updateWorkout(updated)
                workoutToEdit = null
            }
        )
    }

    if (workoutToDelete != null) {
        AlertDialog(
            onDismissRequest = { workoutToDelete = null },
            title = { Text(if (workoutToDelete!!.isCompleted) "Delete from history?" else "Delete planned workout?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWorkout(workoutToDelete!!)
                        workoutToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("DELETE") }
            },
            dismissButton = {
                TextButton(onClick = { workoutToDelete = null }) { Text("CANCEL") }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete ALL workout history?") },
            text = { Text("This action cannot be undone. Your custom exercises and plans will not be affected.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllHistory()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("DELETE ALL") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (showLogDialog) {
        if (selectedExercise != null) {
            WorkoutEntryDialog(
                exercise = selectedExercise!!,
                date = if (selectedTab == GymViewModel.GymTab.CALENDAR) selectedDate else null,
                isCompleted = selectedTab != GymViewModel.GymTab.CALENDAR,
                onDismiss = { showLogDialog = false },
                onConfirm = { setsList ->
                    setsList.forEach { set ->
                        viewModel.logWorkout(
                            exerciseName = selectedExercise!!.name,
                            sets = 1,
                            reps = set.reps,
                            weight = set.weight,
                            date = if (selectedTab == GymViewModel.GymTab.CALENDAR) selectedDate else null,
                            isCompleted = selectedTab != GymViewModel.GymTab.CALENDAR
                        )
                    }
                    showLogDialog = false
                    if (selectedTab == GymViewModel.GymTab.WORKOUT) {
                        viewModel.selectTab(GymViewModel.GymTab.HISTORY)
                    }
                }
            )
        } else if (selectedTab == GymViewModel.GymTab.CALENDAR) {
            AlertDialog(
                onDismissRequest = { showLogDialog = false },
                title = { Text("Plan a Workout") },
                text = { Text("To plan a workout, go to the WORKOUT tab, select an exercise, and choose this date in the Calendar.") },
                confirmButton = { Button(onClick = { showLogDialog = false }) { Text("OK") } }
            )
        }
    }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onSave = { name, group, part, equip, desc, s, r, w, d, n ->
                viewModel.saveCustomExercise(name, group, part, equip, desc, s, r, w, d, n)
                showAddExerciseDialog = false
            }
        )
    }

    if (showAddWorkoutDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onSave = { name, list ->
                // This could save a blank template or use active session
                viewModel.saveWorkoutTemplate(name, emptyList()) 
                showAddWorkoutDialog = false
            }
        )
    }

    if (showApplyTemplateDialog) {
        ApplyTemplateDialog(
            templates = workoutTemplates,
            onDismiss = { showApplyTemplateDialog = false },
            onApply = { template ->
                viewModel.applyTemplateToDate(template, selectedDate)
                showApplyTemplateDialog = false
                viewModel.selectTab(GymViewModel.GymTab.CALENDAR)
            },
            onDelete = { viewModel.deleteTemplate(it) }
        )
    }

    if (showSaveTemplateDialog != null) {
        var templateName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveTemplateDialog = null },
            title = { Text("Save as Template") },
            text = {
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template Name (e.g. Chest Day)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveWorkoutTemplate(templateName, showSaveTemplateDialog!!)
                        showSaveTemplateDialog = null
                    },
                    enabled = templateName.isNotBlank()
                ) { Text("SAVE") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTemplateDialog = null }) { Text("CANCEL") }
            }
        )
    }
}

@Composable
fun HistorySection(
    logs: List<WorkoutEntity>,
    onEdit: (WorkoutEntity) -> Unit,
    onDelete: (WorkoutEntity) -> Unit,
    onSaveTemplate: (WorkoutEntity) -> Unit
) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No workout history yet", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* handeled by parent fab */ }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START WORKOUT")
                }
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(logs) { log ->
                HistoryLogCard(log, onEdit = { onEdit(log) }, onDelete = { onDelete(log) }, onSaveTemplate = { onSaveTemplate(log) })
            }
        }
    }
}

@Composable
fun WorkoutSection(
    navState: GymViewModel.GymNavState,
    subCategories: List<String>,
    exercises: List<Exercise>,
    todayPlan: List<WorkoutEntity>,
    onMuscleGroupClick: (MuscleGroup) -> Unit,
    onSubCategoryClick: (MuscleGroup, String) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onBackClick: () -> Unit,
    onStartTodayPlan: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (navState is GymViewModel.GymNavState.MuscleGroups && todayPlan.isNotEmpty()) {
            TodayPlanCard(todayPlan, onStartTodayPlan)
        }

        if (navState !is GymViewModel.GymNavState.MuscleGroups) {
            TextButton(onClick = onBackClick, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
        }

        when (navState) {
            is GymViewModel.GymNavState.MuscleGroups -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(MuscleGroup.entries) { group ->
                        FolderItem(title = group.displayName, onClick = { onMuscleGroupClick(group) })
                    }
                }
            }
            is GymViewModel.GymNavState.SubCategories -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(subCategories) { sub ->
                        FolderItem(title = sub, onClick = { onSubCategoryClick(navState.muscleGroup, sub) })
                    }
                }
            }
            is GymViewModel.GymNavState.Exercises -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(exercises) { exercise ->
                        ExerciseCard(exercise = exercise, onLogClick = { onExerciseClick(exercise) })
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarSection(
    selectedDate: String,
    plannedWorkouts: List<WorkoutEntity>,
    completedLogs: List<WorkoutEntity>,
    plannedForDate: List<WorkoutEntity>,
    onDateSelected: (String) -> Unit,
    onEdit: (WorkoutEntity) -> Unit,
    onDelete: (WorkoutEntity) -> Unit,
    onComplete: (WorkoutEntity) -> Unit,
    onSaveTemplate: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        CalendarView(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            plannedDates = plannedWorkouts.map { it.date }.toSet(),
            completedDates = completedLogs.map { it.date }.toSet(),
            onDateSelected = onDateSelected,
            onMonthChange = { currentMonth = it }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Planned for ${LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}",
                style = MaterialTheme.typography.titleMedium
            )
            if (plannedForDate.isNotEmpty()) {
                TextButton(onClick = onSaveTemplate) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save as Template")
                }
            }
        }
        
        if (plannedForDate.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No workout planned", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(plannedForDate) { plan ->
                    PlannedWorkoutCard(
                        plan = plan,
                        onEdit = { onEdit(plan) },
                        onDelete = { onDelete(plan) },
                        onComplete = { onComplete(plan) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: String,
    plannedDates: Set<String>,
    completedDates: Set<String>,
    onDateSelected: (String) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7
    val today = LocalDate.now().toString()

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month")
            }
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val rows = (daysInMonth + firstDayOfMonth + 6) / 7
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val day = row * 7 + col - firstDayOfMonth + 1
                    if (day in 1..daysInMonth) {
                        val date = currentMonth.atDay(day).toString()
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val hasPlan = plannedDates.contains(date)
                        val hasCompletion = completedDates.contains(date)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.toString(),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Row {
                                    if (hasPlan) {
                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else Color.Blue))
                                    }
                                    if (hasCompletion) {
                                        if (hasPlan) Spacer(modifier = Modifier.width(2.dp))
                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else Color.Green))
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun TodayPlanCard(plan: List<WorkoutEntity>, onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Today's Workout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = plan.firstOrNull()?.workoutName ?: "Planned Workout", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${plan.size} Exercises", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                Text("Start Workout →")
            }
        }
    }
}

@Composable
fun HistoryLogCard(
    log: WorkoutEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSaveTemplate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = log.exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = log.workoutName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                Row {
                    IconButton(onClick = onSaveTemplate) { Icon(Icons.Default.Save, "Save as Template", modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) }
                }
            }
            Text(text = log.date, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${log.sets} sets x ${log.reps} reps${log.weight?.let { " @ ${it}kg" } ?: ""}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun PlannedWorkoutCard(
    plan: WorkoutEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = plan.exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = plan.workoutName, style = MaterialTheme.typography.bodySmall)
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${plan.sets} sets x ${plan.reps} reps${plan.weight?.let { " @ ${it}kg" } ?: ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onComplete, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Complete", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ApplyTemplateDialog(
    templates: List<WorkoutTemplateEntity>,
    onDismiss: () -> Unit,
    onApply: (WorkoutTemplateEntity) -> Unit,
    onDelete: (WorkoutTemplateEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Template") },
        text = {
            if (templates.isEmpty()) {
                Text("No templates saved yet.", color = Color.Gray)
            } else {
                LazyColumn {
                    items(templates) { template ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onApply(template) }.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = template.name, style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = { onDelete(template) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditWorkoutDialog(
    workout: WorkoutEntity,
    onDismiss: () -> Unit,
    onSave: (WorkoutEntity) -> Unit
) {
    var name by remember { mutableStateOf(workout.workoutName) }
    var exercise by remember { mutableStateOf(workout.exerciseName) }
    var sets by remember { mutableStateOf(workout.sets.toString()) }
    var reps by remember { mutableStateOf(workout.reps.toString()) }
    var weight by remember { mutableStateOf(workout.weight?.toString() ?: "") }
    var date by remember { mutableStateOf(workout.date) }
    var notes by remember { mutableStateOf(workout.notes ?: "") }
    var rest by remember { mutableStateOf(workout.restTimeSeconds?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Entry") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Workout Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = exercise, onValueChange = { exercise = it }, label = { Text("Exercise Name") }, modifier = Modifier.fillMaxWidth())
                Row {
                    OutlinedTextField(value = sets, onValueChange = { sets = it }, label = { Text("Sets") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Reps") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = rest, onValueChange = { rest = it }, label = { Text("Rest Time (s)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(workout.copy(
                    workoutName = name,
                    exerciseName = exercise,
                    sets = sets.toIntOrNull() ?: workout.sets,
                    reps = reps.toIntOrNull() ?: workout.reps,
                    weight = weight.toFloatOrNull(),
                    restTimeSeconds = rest.toIntOrNull(),
                    notes = notes,
                    date = date
                ))
            }) { Text("SAVE") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}

@Composable
fun FolderItem(title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun ExerciseCard(exercise: Exercise, onLogClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            if (exercise.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = exercise.imageUrl,
                    contentDescription = exercise.name,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = exercise.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "Part: ${exercise.subCategory}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onLogClick, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Workout")
                }
            }
        }
    }
}

@Composable
fun WorkoutEntryDialog(
    exercise: Exercise,
    date: String? = null,
    isCompleted: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (List<WorkoutSet>) -> Unit
) {
    var setsList by remember { mutableStateOf(listOf(WorkoutSet(10, 0f))) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCompleted) "Log ${exercise.name}" else "Plan ${exercise.name}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (date != null) {
                    Text("Date: $date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                setsList.forEachIndexed { index, set ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Set ${index + 1}", modifier = Modifier.width(50.dp))
                        OutlinedTextField(
                            value = if (set.weight == 0f) "" else set.weight.toString(),
                            onValueChange = { val w = it.toFloatOrNull() ?: 0f; setsList = setsList.toMutableList().also { l -> l[index] = set.copy(weight = w) } },
                            label = { Text("kg") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = set.reps.toString(),
                            onValueChange = { val r = it.toIntOrNull() ?: 0; setsList = setsList.toMutableList().also { l -> l[index] = set.copy(reps = r) } },
                            label = { Text("reps") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                TextButton(
                    onClick = { setsList = setsList + WorkoutSet(10, setsList.lastOrNull()?.weight ?: 0f) },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Set")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(setsList) }) {
                Text(if (isCompleted) "Complete" else "Save Plan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (String, MuscleGroup, String, String, String, Int, Int, Float, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf(MuscleGroup.CHEST) }
    var part by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }
    var weight by remember { mutableStateOf("0") }
    var duration by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    
    var groupExpanded by remember { mutableStateOf(false) }
    var partExpanded by remember { mutableStateOf(false) }

    val currentGroupInfo = MUSCLE_GROUPS_INFO.find { it.group == selectedGroup }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Exercise") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Exercise Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                
                Box {
                    OutlinedTextField(
                        value = selectedGroup.displayName,
                        onValueChange = {},
                        label = { Text("Muscle Group") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { groupExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                        MuscleGroup.entries.forEach { group ->
                            DropdownMenuItem(text = { Text(group.displayName) }, onClick = { selectedGroup = group; part = ""; groupExpanded = false })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box {
                    OutlinedTextField(
                        value = part,
                        onValueChange = { part = it },
                        label = { Text("Muscle Part") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { partExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = partExpanded, onDismissRequest = { partExpanded = false }) {
                        currentGroupInfo?.parts?.forEach { p ->
                            DropdownMenuItem(text = { Text(p) }, onClick = { part = p; partExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = equipment, onValueChange = { equipment = it }, label = { Text("Equipment") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                
                Row {
                    OutlinedTextField(value = sets, onValueChange = { sets = it }, label = { Text("Sets") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Reps") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Row {
                    OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (m)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(name, selectedGroup, part, equipment, desc, sets.toIntOrNull() ?: 3, reps.toIntOrNull() ?: 10, weight.toFloatOrNull() ?: 0f, duration.toIntOrNull() ?: 0, notes) 
            }, enabled = name.isNotBlank() && part.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onSave: (String, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Workout Plan") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Workout Name (e.g. Push Day)") }, modifier = Modifier.fillMaxWidth())
                Text("You can log exercises into this plan from the Workout tab!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, emptyList()) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
