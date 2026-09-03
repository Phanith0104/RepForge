package com.repforge.ui.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun StepScreen(viewModel: StepViewModel = hiltViewModel()) {
    val stepData by viewModel.todaySteps.collectAsState()
    val steps = stepData?.count ?: 0
    val streak = stepData?.streak ?: 0
    val goal = 10000
    val progress = (steps.toFloat() / goal).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
    }
}
