package com.repforge.ui.auth

import android.app.Activity
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.repforge.R
import com.repforge.data.local.entities.UserEntity

private const val TAG = "LOGIN_SCREEN"

enum class AuthMethod { SELECTION, EMAIL, PHONE }

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val userState by viewModel.user.collectAsState()
    val savedProfiles by viewModel.savedProfiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isCodeSent by viewModel.isCodeSent.collectAsState()
    
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { viewModel.signInWithGoogle(it) } ?: run {
                    viewModel.stopLoading()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google Sign-In failed", e)
                viewModel.stopLoading()
            }
        } else {
            viewModel.stopLoading()
        }
    }

    if (userState?.isLoggedIn == true && userState?.isProfileComplete == false) {
        ProfileSetupScreen(viewModel)
    } else {
        var authMethod by remember { mutableStateOf(AuthMethod.SELECTION) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (authMethod != AuthMethod.SELECTION) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { authMethod = AuthMethod.SELECTION }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = if (authMethod == AuthMethod.EMAIL) "Email Login" else "Phone Login",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "RepForge",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Strength in consistency", 
                fontSize = 16.sp, 
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (authMethod == AuthMethod.SELECTION) {
                // Main Selection UI
                if (savedProfiles.isNotEmpty()) {
                    Text(
                        text = "Recent Sessions",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(savedProfiles) { profile ->
                            RecentUserItem(profile, screenWidth, onClick = {
                                // Pre-fill logic can be added here
                            })
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Button(
                    onClick = { authMethod = AuthMethod.EMAIL },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Email")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { authMethod = AuthMethod.PHONE },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Phone")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "OR", color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestIdToken("125160357464-cpptkjvmdaftu2dianfqk7afjbn5p5g5.apps.googleusercontent.com")
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(client.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continue with Google")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = { viewModel.loginAsGuest() },
                    enabled = !isLoading
                ) {
                    Text("Continue as Guest", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            } else {
                // Sub-auth fields
                if (error != null) {
                    val isSuccess = error!!.startsWith("SUCCESS:")
                    val displayError = if (isSuccess) error!!.substring(8) else error!!
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuccess) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = displayError,
                            color = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (authMethod == AuthMethod.EMAIL) {
                    EmailAuthFields(viewModel, isLoading)
                } else {
                    PhoneAuthFields(viewModel, isLoading, isCodeSent)
                }
            }
        }
    }
}

@Composable
fun RecentUserItem(user: UserEntity, screenWidth: Dp, onClick: () -> Unit) {
    val avatarSize = (screenWidth * 0.15f).coerceIn(48.dp, 80.dp)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(avatarSize + 16.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (user.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(avatarSize * 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (user.isGuest) "Guest" else user.name.split(" ").firstOrNull() ?: "User",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun EmailAuthFields(viewModel: AuthViewModel, isLoading: Boolean) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isEmailValid = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPasswordValid = password.length >= 6

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(image, contentDescription = null)
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { 
                if (isEmailValid && isPasswordValid) {
                    keyboardController?.hide()
                    viewModel.login(email.trim(), password)
                }
            }
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = { 
                if (isEmailValid) {
                    viewModel.resetPassword(email.trim())
                } else {
                    // Could show a specific tip to enter email first
                }
            },
            enabled = !isLoading
        ) {
            Text("Forgot Password?", style = MaterialTheme.typography.bodySmall)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { 
            keyboardController?.hide()
            viewModel.login(email.trim(), password) 
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading && isEmailValid && isPasswordValid,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("Login / Sign Up")
        }
    }
}

@Composable
fun PhoneAuthFields(viewModel: AuthViewModel, isLoading: Boolean, isCodeSent: Boolean) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    if (!isCodeSent) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '+' }) phoneNumber = it },
            label = { Text("Phone Number") },
            placeholder = { Text("+91XXXXXXXXXX") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (phoneNumber.isNotBlank()) {
                        keyboardController?.hide()
                        viewModel.verifyPhone(context as Activity, phoneNumber)
                    }
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                if (phoneNumber.isNotBlank()) {
                    keyboardController?.hide()
                    viewModel.verifyPhone(context as Activity, phoneNumber) 
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && phoneNumber.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Send Verification Code")
            }
        }
    } else {
        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
            label = { Text("6-Digit Code") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (otpCode.length == 6) {
                        keyboardController?.hide()
                        viewModel.signInWithPhoneCode(otpCode)
                    }
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                if (otpCode.length == 6) {
                    keyboardController?.hide()
                    viewModel.signInWithPhoneCode(otpCode) 
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && otpCode.length == 6,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Verify & Login")
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(viewModel: AuthViewModel) {
    val user by viewModel.user.collectAsState()
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var phoneNumber by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val avatarSize = (screenWidth * 0.25f).coerceIn(80.dp, 140.dp)

    val genders = listOf("Male", "Female", "Other")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.logout() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Complete Your Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (user?.photoUrl != null) {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(avatarSize * 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This helps us calculate your BMI and personalize your experience", 
            fontSize = 14.sp, 
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name, 
            onValueChange = { name = it }, 
            label = { Text("Full Name") }, 
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = age, 
            onValueChange = { if (it.all { c -> c.isDigit() }) age = it }, 
            label = { Text("Age") }, 
            modifier = Modifier.fillMaxWidth(), 
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = weight, 
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weight = it }, 
                label = { Text("Weight (kg)") }, 
                modifier = Modifier.weight(1f), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = height, 
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) height = it }, 
                label = { Text("Height (cm)") }, 
                modifier = Modifier.weight(1f), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phoneNumber, 
            onValueChange = { phoneNumber = it }, 
            label = { Text("Phone Number") }, 
            modifier = Modifier.fillMaxWidth(), 
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = gender, 
                onValueChange = {}, 
                label = { Text("Gender") }, 
                readOnly = true, 
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { TextButton(onClick = { expanded = true }) { Text("Select") } },
                shape = RoundedCornerShape(12.dp)
            )
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
            enabled = name.isNotBlank() && age.isNotBlank() && weight.isNotBlank() && height.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Start My Journey")
        }
    }
}

@Composable
fun TextAlign(textAlign: TextAlign) {
    // This is just a placeholder for the import if needed
}
