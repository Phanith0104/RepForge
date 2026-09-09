package com.repforge.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val user: StateFlow<UserEntity?> = userRepository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            userRepository.syncUserFromFirebase()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = userRepository.loginWithEmail(email, password)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(name: String, age: Int, gender: String, phoneNumber: String, weight: Float, height: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.updateProfile(name, age, gender, phoneNumber, weight, height)
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = userRepository.signInWithGoogle(idToken)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}
