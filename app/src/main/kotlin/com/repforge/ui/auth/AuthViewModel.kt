package com.repforge.ui.auth

import android.app.Activity
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.repforge.data.local.entities.UserEntity
import com.repforge.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId = _verificationId.asStateFlow()

    private val _resendingToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)

    private val _isCodeSent = MutableStateFlow(false)
    val isCodeSent = _isCodeSent.asStateFlow()

    val user: StateFlow<UserEntity?> = userRepository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val savedProfiles: StateFlow<List<UserEntity>> = userRepository.getAllSavedProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.syncUserFromFirebase()
            } catch (e: Exception) {
                _error.value = "Session recovery failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }

            try {
                userRepository.observeAuthState().collect { firebaseUser ->
                    if (firebaseUser != null) {
                        userRepository.syncUserFromFirebase()
                    } else {
                        userRepository.syncUserFromFirebase()
                    }
                }
            } catch (_: Exception) {
                // Background listener
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _error.value = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = userRepository.loginWithEmail(email.trim(), password)
                if (result.isFailure) {
                    _error.value = result.exceptionOrNull()?.message ?: "Login failed"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _error.value = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = userRepository.registerWithEmail(email.trim(), password)
                if (result.isFailure) {
                    _error.value = result.exceptionOrNull()?.message ?: "Registration failed"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                userRepository.loginAsGuest()
            } catch (e: Exception) {
                _error.value = "Guest login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyPhone(activity: Activity, phoneNumber: String) {
        val cleanDigits = phoneNumber.filter { it.isDigit() }
        if (cleanDigits.length < 10) {
            _error.value = "Please enter a valid 10-digit phone number."
            return
        }

        _isLoading.value = true
        _error.value = null
        userRepository.verifyPhoneNumber(
            activity = activity,
            phoneNumber = phoneNumber,
            forceResendingToken = _resendingToken.value,
            onCodeSent = { id, token ->
                _verificationId.value = id
                _resendingToken.value = token
                _isCodeSent.value = true
                _isLoading.value = false
            },
            onVerificationCompleted = { credential ->
                signInWithPhone(credential)
            },
            onVerificationFailed = { e ->
                _error.value = e.message ?: "Phone verification failed"
                _isLoading.value = false
            }
        )
    }

    fun resendPhoneCode(activity: Activity, phoneNumber: String) {
        if (_resendingToken.value == null) {
            verifyPhone(activity, phoneNumber)
            return
        }
        _isLoading.value = true
        _error.value = null
        userRepository.verifyPhoneNumber(
            activity = activity,
            phoneNumber = phoneNumber,
            forceResendingToken = _resendingToken.value,
            onCodeSent = { id, token ->
                _verificationId.value = id
                _resendingToken.value = token
                _isCodeSent.value = true
                _isLoading.value = false
                _error.value = "SUCCESS:A new verification code has been sent."
            },
            onVerificationCompleted = { credential ->
                signInWithPhone(credential)
            },
            onVerificationFailed = { e ->
                _error.value = e.message ?: "Resend failed"
                _isLoading.value = false
            }
        )
    }

    fun resetPhoneState() {
        _isCodeSent.value = false
        _verificationId.value = null
        _resendingToken.value = null
        _error.value = null
    }

    fun signInWithPhoneCode(code: String) {
        val id = _verificationId.value
        if (id.isNullOrEmpty()) {
            _error.value = "Verification session expired. Please request a new OTP."
            return
        }
        if (code.length < 6) {
            _error.value = "Please enter the full 6-digit verification code."
            return
        }
        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithPhone(credential)
    }



    private fun signInWithPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = userRepository.signInWithPhone(credential)
                if (result.isFailure) {
                    _error.value = result.exceptionOrNull()?.message ?: "Phone sign-in failed"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, age: Int, gender: String, phoneNumber: String, weight: Float, height: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = userRepository.updateProfile(name, age, gender, phoneNumber, weight, height)
                if (result.isFailure) {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to save profile"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = userRepository.signInWithGoogle(idToken)
                if (result.isFailure) {
                    _error.value = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _error.value = "Please enter a valid email to reset your password."
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = userRepository.sendPasswordResetEmail(email.trim())
                if (result.isSuccess) {
                    _error.value = "SUCCESS:Password reset link sent to $email."
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to send reset email"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send reset email"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setError(message: String) {
        _error.value = message
    }

    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout()
            } catch (e: Exception) {
                _error.value = "Logout failed: ${e.message}"
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.syncUserFromFirebase()
            } catch (e: Exception) {
                _error.value = "Refresh failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun stopLoading() {
        _isLoading.value = false
    }
}
