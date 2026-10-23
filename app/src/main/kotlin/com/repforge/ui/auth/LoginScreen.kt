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
enum class EmailMode { LOGIN, REGISTER }

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

    val webClientId = remember(context) {
        try {
            context.resources.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            "125160357464-cpptkjvmdaftu2dianfqk7afjbn5p5g5.apps.googleusercontent.com"
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken)
                } else {
                    viewModel.stopLoading()
                    viewModel.setError("Google Sign-In failed: No ID Token received from Google.")
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google Sign-In failed with status code: ${e.statusCode}", e)
                viewModel.stopLoading()
                val message = when (e.statusCode) {
                    12500 -> "Google Sign-In failed: Play Services configuration error."
                    7 -> "Google Sign-In failed: Network error. Please check your internet connection."
                    10 -> "Google Sign-In failed: Developer configuration error (SHA-1 fingerprint)."
                    12501 -> "Google Sign-In was cancelled."
                    else -> "Google Sign-In failed (${e.statusCode}): ${e.localizedMessage}"
                }
                if (e.statusCode != 12501) {
                    viewModel.setError(message)
                }
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
                    IconButton(onClick = { 
                        authMethod = AuthMethod.SELECTION
                        viewModel.resetPhoneState()
                        viewModel.clearError()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = if (authMethod == AuthMethod.EMAIL) "Email Authentication" else "Phone Authentication",
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

            Spacer(modifier = Modifier.height(24.dp))

            // Show error/success banner on all screens
            if (error != null) {
                AuthStatusBanner(message = error!!, onDismiss = { viewModel.clearError() })
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (authMethod == AuthMethod.SELECTION) {
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
                                if (profile.provider == "guest") {
                                    viewModel.loginAsGuest()
                                } else {
                                    authMethod = AuthMethod.EMAIL
                                }
                            })
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Button(
                    onClick = { 
                        authMethod = AuthMethod.EMAIL
                        viewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Email")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { 
                        authMethod = AuthMethod.PHONE
                        viewModel.resetPhoneState()
                        viewModel.clearError()
                    },
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
                        viewModel.clearError()
                        try {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestIdToken(webClientId)
                                .build()
                            val client = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(client.signInIntent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error launching Google Sign-In intent", e)
                            viewModel.setError("Could not launch Google Sign-In: ${e.localizedMessage}")
                        }
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

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = { viewModel.loginAsGuest() },
                    enabled = !isLoading
                ) {
                    Text("Continue as Guest", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            } else {
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
fun AuthStatusBanner(message: String, onDismiss: () -> Unit) {
    val isSuccess = message.startsWith("SUCCESS:")
    val displayMessage = if (isSuccess) message.substring(8) else message
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = displayMessage,
                    color = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer
                )
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
    var mode by remember { mutableStateOf(EmailMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isEmailValid = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPasswordValid = password.length >= 6
    val isConfirmValid = mode == EmailMode.LOGIN || (password == confirmPassword && confirmPassword.isNotEmpty())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterChip(
            selected = mode == EmailMode.LOGIN,
            onClick = { 
                mode = EmailMode.LOGIN
                viewModel.clearError()
            },
            label = { Text("Log In", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        FilterChip(
            selected = mode == EmailMode.REGISTER,
            onClick = { 
                mode = EmailMode.REGISTER
                viewModel.clearError()
            },
            label = { Text("Sign Up", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email Address") },
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

    Spacer(modifier = Modifier.height(12.dp))

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
            imeAction = if (mode == EmailMode.REGISTER) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { 
                if (isEmailValid && isPasswordValid && isConfirmValid) {
                    keyboardController?.hide()
                    if (mode == EmailMode.LOGIN) {
                        viewModel.login(email.trim(), password)
                    } else {
                        viewModel.register(email.trim(), password)
                    }
                }
            }
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )

    if (mode == EmailMode.REGISTER) {
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isEmailValid && isPasswordValid && isConfirmValid) {
                        keyboardController?.hide()
                        viewModel.register(email.trim(), password)
                    }
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (mode == EmailMode.LOGIN) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { viewModel.resetPassword(email.trim()) },
                enabled = !isLoading
            ) {
                Text("Forgot Password?", style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        Spacer(modifier = Modifier.height(12.dp))
    }

    Button(
        onClick = { 
            keyboardController?.hide()
            if (mode == EmailMode.LOGIN) {
                viewModel.login(email.trim(), password) 
            } else {
                viewModel.register(email.trim(), password)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading && isEmailValid && isPasswordValid && isConfirmValid,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(if (mode == EmailMode.LOGIN) "Log In" else "Create Account")
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
            onValueChange = { if (it.all { c -> c.isDigit() || c == '+' || c == ' ' }) phoneNumber = it },
            label = { Text("Phone Number") },
            placeholder = { Text("e.g. 9876543210 or +919876543210") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (phoneNumber.filter { it.isDigit() }.length >= 10) {
                        keyboardController?.hide()
                        viewModel.verifyPhone(context as Activity, phoneNumber)
                    }
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { 
                if (phoneNumber.filter { it.isDigit() }.length >= 10) {
                    keyboardController?.hide()
                    viewModel.verifyPhone(context as Activity, phoneNumber) 
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && phoneNumber.filter { it.isDigit() }.length >= 10,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Send Verification Code")
            }
        }
    } else {
        Text(
            text = "Enter the 6-digit verification code sent to your phone:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
            label = { Text("6-Digit Code") },
            placeholder = { Text("123456") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
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

        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = { viewModel.resendPhoneCode(context as Activity, phoneNumber) },
                enabled = !isLoading
            ) {
                Text("Resend OTP")
            }
            TextButton(
                onClick = { 
                    viewModel.resetPhoneState() 
                    otpCode = ""
                },
                enabled = !isLoading
            ) {
                Text("Change Number")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(viewModel: AuthViewModel) {
    val userState by viewModel.user.collectAsState()
    var name by remember(userState?.name) { mutableStateOf(userState?.name ?: "") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var phoneNumber by remember(userState?.phoneNumber) { mutableStateOf(userState?.phoneNumber ?: "") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Complete Your Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Help us tailor your workout experience", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            AuthStatusBanner(message = error!!, onDismiss = { viewModel.clearError() })
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { c -> c.isDigit() }) age = it },
                label = { Text("Age") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(text = { Text("Male") }, onClick = { gender = "Male"; expanded = false })
                    DropdownMenuItem(text = { Text("Female") }, onClick = { gender = "Female"; expanded = false })
                    DropdownMenuItem(text = { Text("Other") }, onClick = { gender = "Other"; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = weight,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weight = it },
                label = { Text("Weight (kg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = height,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) height = it },
                label = { Text("Height (cm)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.updateProfile(name, age.toIntOrNull() ?: 0, gender, phoneNumber, weight.toFloatOrNull() ?: 0f, height.toFloatOrNull() ?: 0f)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && name.isNotBlank() && age.isNotBlank() && weight.isNotBlank() && height.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Start My Journey")
            }
        }
    }
}
