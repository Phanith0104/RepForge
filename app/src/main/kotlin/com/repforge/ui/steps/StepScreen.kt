package com.repforge.ui.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repforge.data.local.entities.StepEntity

@Composable
fun StepScreen(viewModel: StepViewModel = hiltViewModel()) {
    val stepData by viewModel.todaySteps.collectAsState()
    val history by viewModel.stepHistory.collectAsState()
    
    val steps = stepData?.count ?: 0
    val streak = stepData?.streak ?: 0
    val isAchievement = stepData?.isAchievement ?: false
    val goal = 10000
    val progress = (steps.toFloat() / goal).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(200.dp),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.LightGray
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = steps.toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "/ $goal", fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (streak > 0) {
                Text(
                    text = "🔥 $streak Day Streak!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFF5722)
                )
            } else {
                Text(
                    text = "Reach 10,000 steps for a streak!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            HorizontalDivider()
            
            Text(
                text = "Monthly History",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        items(history) { record ->
            HistoryItem(record)
        }
    }
}

@Composable
fun HistoryItem(record: StepEntity) {
    Card(
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
                if (record.isAchievement) {
                    Text(text = "🏆 Achievement", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8860B))
                }
            }
            Text(
                text = "${record.count} steps",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
