package com.repforge.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val userState by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken: String? = account?.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    if (userState?.isLoggedIn == true && userState?.isProfileComplete == false) {
        ProfileSetupScreen(viewModel)
    } else {
        AuthScreen(
            viewModel = viewModel,
            isLoading = isLoading,
            error = error,
            onGoogleSignIn = {
                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken("125160357464-cpptkjvmdaftu2dianfqk7afjbn5p5g5.apps.googleusercontent.com")
                    .build()
                val client = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                googleSignInLauncher.launch(client.signInIntent)
            }
        )
    }
}

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    isLoading: Boolean,
    error: String?,
    onGoogleSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "RepForge",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Strength in consistency", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(48.dp))

        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.length >= 6
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login / Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "OR", color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google")
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(viewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var phoneNumber by remember { mutableStateOf("") }

    val genders = listOf("Male", "Female", "Other")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Setup Your Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) weight = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = height,
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) height = it },
            label = { Text("Height (cm)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = gender, onValueChange = {}, label = { Text("Gender") }, readOnly = true, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { TextButton(onClick = { expanded = true }) { Text("Select") } })
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                genders.forEach { g -> DropdownMenuItem(text = { Text(g) }, onClick = { gender = g; expanded = false }) }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.updateProfile(name, age.toIntOrNull() ?: 0, gender, phoneNumber, weight.toFloatOrNull() ?: 0f, height.toFloatOrNull() ?: 0f)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && age.isNotBlank() && weight.isNotBlank() && height.isNotBlank()
        ) {
            Text("Complete Setup")
        }
    }
}
