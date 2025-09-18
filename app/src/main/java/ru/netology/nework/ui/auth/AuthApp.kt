package ru.netology.nework.ui.auth

import android.content.Context
import android.content.Context.MODE_PRIVATE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthApp @Inject constructor(@ApplicationContext context: Context){
    private val prefs = context.getSharedPreferences("auth", MODE_PRIVATE)
    private val prefsEditor = prefs.edit()
    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        _authStateFlow = if (id == 0L || token == null) {
            MutableStateFlow(AuthState())
        } else {
            MutableStateFlow(AuthState(id, token))
        }
    }

    val authStateFlow = _authStateFlow.asStateFlow()


    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = AuthState(id, token)
        with(prefsEditor) {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
            apply()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(prefsEditor) {
            clear()
            commit()
        }
    }

    companion object {
        private const val ID_KEY = "id"
        private const val TOKEN_KEY = "token"
    }
}

data class AuthState(val id: Long = 0, val token: String? = null)