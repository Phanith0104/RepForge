package com.repforge.ui.steps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.data.local.entities.StepEntity
import com.repforge.ui.theme.GoldPrimary

enum class StepPeriodFilter { DAY, WEEK, MONTH, YEAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepScreen(viewModel: StepViewModel = hiltViewModel()) {
    val stepData by viewModel.todaySteps.collectAsState()
    val history by viewModel.stepHistory.collectAsState()
    val currentGoal by viewModel.stepGoal.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()

    var periodFilter by remember { mutableStateOf(StepPeriodFilter.DAY) }
    var selectedHistoryRecord by remember { mutableStateOf<StepEntity?>(null) }

    val context = LocalContext.current
    val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

    val steps = stepData?.count ?: 6240
    val distance = stepData?.distanceKm ?: 4.5f
    val calories = stepData?.caloriesBurned ?: 240
    val activeTime = stepData?.activeTimeMinutes ?: 72
    val progress = if (currentGoal > 0) (steps.toFloat() / currentGoal).coerceIn(0f, 1f) else 0.78f

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Steps", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = GoldPrimary)
                    }
                }
            }

            // Period Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StepPeriodFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = periodFilter == filter,
                            onClick = { periodFilter = filter },
                            label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Circular Step Progress Ring
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(220.dp)
                        .padding(12.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 18.dp.toPx()
                        drawArc(
                            color = Color(0xFF222834),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = GoldPrimary,
                            startAngle = 135f,
                            sweepAngle = 270f * progress,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👣", fontSize = 32.sp)
                        Text(
                            text = "%,d".format(steps),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Steps",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Goal: %,d".format(currentGoal),
                            fontSize = 13.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Stats Bar
            item {
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
                        StepStatItem("%.1f km".format(distance), "Distance")
                        StepStatItem("$calories", "Calories")
                        StepStatItem("${activeTime / 60}h ${activeTime % 60}m", "Active Time")
                    }
                }
            }

            // Hourly Step Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hourly Distribution", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val dummyChartData = listOf(150, 80, 420, 680, 510, 390, 820, 600, 300, 450, 200, 100)
                        val maxBar = dummyChartData.maxOrNull() ?: 1000

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dummyChartData.forEach { value ->
                                val barHeight = (value.toFloat() / maxBar).coerceIn(0.1f, 1f)
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .fillMaxHeight(barHeight)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(GoldPrimary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("12 AM", fontSize = 10.sp, color = Color.Gray)
                            Text("6 AM", fontSize = 10.sp, color = Color.Gray)
                            Text("12 PM", fontSize = 10.sp, color = Color.Gray)
                            Text("6 PM", fontSize = 10.sp, color = Color.Gray)
                            Text("12 AM", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Today's Goal Control Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Today's Goal", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Daily step target", fontSize = 12.sp, color = Color.Gray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.updateStepGoal((currentGoal - 500).coerceAtLeast(1000)) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) { Text("-", fontSize = 20.sp, color = Color.White) }

                            Text(
                                text = "%,d".format(currentGoal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            IconButton(
                                onClick = { viewModel.updateStepGoal(currentGoal + 500) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) { Text("+", fontSize = 20.sp, color = Color.White) }
                        }
                    }
                }
            }

            // Step Streak Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("$streak days", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Current Streak", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        VerticalDivider(modifier = Modifier.height(36.dp), color = Color.DarkGray)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("18 days", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Best Streak", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Recent Days Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recent Days", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            items(history) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedHistoryRecord = record },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (record.count >= currentGoal) Color(0xFF4CAF50) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(record.date, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "%,d / %,d".format(record.count, currentGoal),
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            val pct = if (currentGoal > 0) ((record.count.toFloat() / currentGoal) * 100).toInt() else 0
                            Text(
                                "$pct%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pct >= 100) Color(0xFF4CAF50) else GoldPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
