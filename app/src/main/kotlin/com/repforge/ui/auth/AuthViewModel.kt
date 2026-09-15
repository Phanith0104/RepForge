package com.repforge.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.data.local.entities.UserEntity
import com.repforge.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import android.app.Activity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
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
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = userRepository.loginWithEmail(email, password)
                if (result.isFailure) {
                    _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
                }
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
        _isLoading.value = true
        _error.value = null
        userRepository.verifyPhoneNumber(
            activity = activity,
            phoneNumber = phoneNumber,
            onCodeSent = { id, _ ->
                _verificationId.value = id
                _isCodeSent.value = true
                _isLoading.value = false
            },
            onVerificationCompleted = { credential ->
                signInWithPhone(credential)
            },
            onVerificationFailed = { e ->
                _error.value = e.message ?: "Verification failed"
                _isLoading.value = false
            }
        )
    }

    fun signInWithPhoneCode(code: String) {
        val id = _verificationId.value ?: return
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
                    _error.value = result.exceptionOrNull()?.message ?: "Sign-in failed"
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
}
