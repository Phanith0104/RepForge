package com.repforge.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.repforge.ui.auth.AuthViewModel

@Composable
fun ProfileScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val avatarSize = (screenWidth * 0.35f).coerceIn(100.dp, 200.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "My Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        user?.let { u ->
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
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(avatarSize * 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileItem(label = "Name", value = u.name)
            if (!u.isGuest) {
                ProfileItem(label = "Email", value = u.email)
            }
            ProfileItem(label = "Age", value = "${u.age} years")
            ProfileItem(label = "Gender", value = u.gender)
            ProfileItem(label = "Height", value = "${u.height} cm")
            ProfileItem(label = "Weight", value = "${u.weight} kg")

            Spacer(modifier = Modifier.height(24.dp))

            // BMI Calculation
            if (u.height > 0 && u.weight > 0) {
                val heightInMeters = u.height / 100
                val bmi = u.weight / (heightInMeters * heightInMeters)
                val (bmiCategory, categoryColor) = when {
                    bmi < 18.5 -> "Underweight" to Color(0xFF03A9F4) // Blue
                    bmi < 25 -> "Healthy Weight" to Color(0xFF4CAF50) // Green
                    bmi < 30 -> "Overweight" to Color(0xFFFF9800) // Orange
                    else -> "Obese" to Color(0xFFF44336) // Red
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Body Mass Index (BMI)", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "%.1f".format(bmi),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                        Text(text = bmiCategory, fontWeight = FontWeight.Bold, color = categoryColor)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}
