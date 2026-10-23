package com.repforge.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.repforge.ui.auth.AuthViewModel
import com.repforge.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()
    val isRefreshing by viewModel.isLoading.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val avatarSize = (screenWidth * 0.28f).coerceIn(80.dp, 160.dp)

    var isEditing by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }

    LaunchedEffect(user, isEditing) {
        user?.let { u ->
            if (!isEditing) {
                nameInput = u.name
                heightInput = if (u.height > 0) u.height.toString() else "175"
                weightInput = if (u.weight > 0) u.weight.toString() else "69"
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
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Profile" else "Profile",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            user?.let { u ->
                // Avatar & User Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                tint = GoldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (u.name.isNotBlank()) u.name else "A. Chandan Teja",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "250160226720",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Fitness Enthusiast",
                            fontSize = 12.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                nameInput = u.name
                                heightInput = if (u.height > 0) u.height.toString() else "175"
                                weightInput = if (u.weight > 0) u.weight.toString() else "69"
                                isEditing = !isEditing
                            },
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(if (isEditing) "Cancel" else "Edit Profile", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isEditing) {
                    // Body Measurements Card
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
                            val h = if (u.height > 0) u.height else 175f
                            val w = if (u.weight > 0) u.weight else 69f
                            val hM = h / 100f
                            val bmi = if (hM > 0) w / (hM * hM) else 22.5f

                            MeasurementItem("Height", "%.0f cm".format(h))
                            VerticalDivider(modifier = Modifier.height(36.dp), color = Color.DarkGray)
                            MeasurementItem("Weight", "%.0f kg".format(w))
                            VerticalDivider(modifier = Modifier.height(36.dp), color = Color.DarkGray)
                            MeasurementItem("BMI", "%.1f\n(Healthy)".format(bmi))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Settings & Preferences List
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ProfileSettingRow("Fitness Goal", "Gain Muscle", Icons.Default.AddTask)
                            ProfileSettingRow("Daily Step Target", "8,000 steps", Icons.AutoMirrored.Filled.DirectionsWalk)
                            ProfileSettingRow("Units", "Metric (kg, cm)", Icons.Default.Straighten)
                            ProfileSettingRow("Notifications", "On", Icons.Default.Notifications)
                            ProfileSettingRow("Account & Security", "", Icons.Default.Shield)
                            ProfileSettingRow("Sync with Cloud", "Synced", Icons.Default.CloudDone)
                        }
                    }
                } else {
                    // Edit Profile Fields
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { if (it.all { c -> (c.isDigit() || c == '.') }) heightInput = it },
                                label = { Text("Height (cm)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { if (it.all { c -> (c.isDigit() || c == '.') }) weightInput = it },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    viewModel.updateProfile(
                                        name = nameInput.trim(),
                                        age = u.age,
                                        gender = u.gender,
                                        phoneNumber = u.phoneNumber,
                                        weight = weightInput.toFloatOrNull() ?: u.weight,
                                        height = heightInput.toFloatOrNull() ?: u.height
                                    )
                                    isEditing = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Changes", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign Out Button
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MeasurementItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldPrimary, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProfileSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (subtitle.isNotBlank()) {
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        }
    }
}
