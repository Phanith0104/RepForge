package com.repforge.ui.calisthenics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.repforge.domain.model.CALISTHENICS_LEVELS
import com.repforge.domain.model.CalisthenicsProgression

@Composable
fun CalisthenicsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Calisthenics Mastery",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(CALISTHENICS_LEVELS) { progression ->
                ProgressionCard(progression)
            }
        }
    }
}

@Composable
fun ProgressionCard(progression: CalisthenicsProgression) {
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
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = progression.levelName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = progression.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            progression.exercises.forEach { exercise ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = exercise, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
