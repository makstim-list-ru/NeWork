package ru.netology.nework.ui.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthStateViewModel @Inject constructor(authApp: AuthApp) : ViewModel() {
    val data: StateFlow<AuthState> = authApp.authStateFlow
    val authenticated: Boolean
        get() = (data.value.id != 0L)
}