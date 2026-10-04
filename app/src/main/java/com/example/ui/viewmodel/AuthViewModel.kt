package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val user: UserEntity) : AuthUiState()
    data class Error(val messageLao: String, val messageEng: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        // Automatically check/load default user so app opens ready with sample data
        loadDefaultUser()
    }

    fun loadDefaultUser() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            // Check for demo user
            val user = authRepository.getUser(1L)
            if (user != null) {
                _currentUser.value = user
                _uiState.value = AuthUiState.Authenticated(user)
            } else {
                _uiState.value = AuthUiState.Idle
            }
        }
    }

    fun login(login: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.login(login, pass)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.user
                    _uiState.value = AuthUiState.Authenticated(result.user)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.messageLao, result.messageEng)
                }
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        fullName: String,
        currency: String = "LAK",
        language: String = "LAO"
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.register(username, email, password, fullName, currency, language)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.user
                    _uiState.value = AuthUiState.Authenticated(result.user)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.messageLao, result.messageEng)
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean) -> Unit) {
        val user = _currentUser.value ?: return onResult(false)
        viewModelScope.launch {
            val success = authRepository.changePassword(user.id, oldPass, newPass)
            onResult(success)
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
