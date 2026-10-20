package com.repforge.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.repforge.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()
    val isRefreshing by viewModel.isLoading.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val avatarSize = (screenWidth * 0.35f).coerceIn(100.dp, 200.dp)

    var isEditing by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }

    LaunchedEffect(user, isEditing) {
        user?.let { u ->
            if (!isEditing) {
                nameInput = u.name
                heightInput = if (u.height > 0) u.height.toString() else ""
                weightInput = if (u.weight > 0) u.weight.toString() else ""
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEditing) "Edit Profile" else "My Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
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
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(avatarSize * 0.6f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isEditing) {
                    // View Mode
                    ProfileItem(label = "Name", value = u.name)
                    if (!u.isGuest) {
                        ProfileItem(label = "Email", value = u.email)
                    }
                    ProfileItem(label = "Age", value = "${u.age} years")
                    ProfileItem(label = "Gender", value = u.gender)
                    ProfileItem(label = "Height", value = if (u.height > 0) "%.1f cm".format(u.height) else "Not set")
                    ProfileItem(label = "Weight", value = if (u.weight > 0) "%.1f kg".format(u.weight) else "Not set")

                    Spacer(modifier = Modifier.height(20.dp))

                    BmiDisplayCard(heightCm = u.height, weightKg = u.weight)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            nameInput = u.name
                            heightInput = if (u.height > 0) u.height.toString() else ""
                            weightInput = if (u.weight > 0) u.weight.toString() else ""
                            isEditing = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                } else {
                    // Edit Mode
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) heightInput = it },
                        label = { Text("Height (cm)") },
                        placeholder = { Text("e.g. 170") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weightInput = it },
                        label = { Text("Weight (kg)") },
                        placeholder = { Text("e.g. 70") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Live Calculated BMI
                    val liveHeight = heightInput.toFloatOrNull() ?: 0f
                    val liveWeight = weightInput.toFloatOrNull() ?: 0f
                    BmiDisplayCard(heightCm = liveHeight, weightKg = liveWeight)

                    Spacer(modifier = Modifier.height(24.dp))

                    val isInputValid = nameInput.isNotBlank() && (heightInput.toFloatOrNull() ?: 0f) > 0f && (weightInput.toFloatOrNull() ?: 0f) > 0f

                    Button(
                        onClick = {
                            val hFloat = heightInput.toFloatOrNull() ?: 0f
                            val wFloat = weightInput.toFloatOrNull() ?: 0f
                            viewModel.updateProfile(
                                name = nameInput.trim(),
                                age = u.age,
                                gender = u.gender,
                                phoneNumber = u.phoneNumber,
                                weight = wFloat,
                                height = hFloat
                            )
                            isEditing = false
                        },
                        enabled = isInputValid,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isEditing) {
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun BmiDisplayCard(heightCm: Float, weightKg: Float) {
    if (heightCm > 0f && weightKg > 0f) {
        val heightInMeters = heightCm / 100f
        val bmi = weightKg / (heightInMeters * heightInMeters)
        val (bmiCategory, categoryColor) = when {
            bmi < 18.5f -> "Underweight" to Color(0xFF03A9F4) // Blue
            bmi < 25f -> "Healthy Weight" to Color(0xFF4CAF50) // Green
            bmi < 30f -> "Overweight" to Color(0xFFFF9800) // Orange
            else -> "Obese" to Color(0xFFF44336) // Red
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Body Mass Index (BMI)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "%.1f".format(bmi),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
                Text(
                    text = bmiCategory,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "* BMI categories are standard adult guidelines and not a medical diagnosis.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Body Mass Index (BMI)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "BMI unavailable",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Please enter valid positive values for height and weight.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
