package ru.netology.nework.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.ui.retrofit.PostsRetrofitSuspendInterface
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val postsRetrofitSuspendInterface: PostsRetrofitSuspendInterface,
    private val authApp: AuthApp
) : ViewModel() {

    private val _loginFaultFlag = MutableLiveData<Boolean?>(null)
    val loginFaultFlag: LiveData<Boolean?>
        get() = _loginFaultFlag

    private val _loginRegFaultFlag = MutableLiveData<Boolean?>(null)
    val loginRegFaultFlag: LiveData<Boolean?>
        get() = _loginRegFaultFlag

    val data = LoginDefault("test", "test") //todo temporary solution
    val dataRegistration =
        LoginRegistrationDefault("test", "test", "test", "Maks Timm") //todo temporary solution

    fun loginVM(login: String, pass: String) {
        viewModelScope.launch {
            _loginFaultFlag.value = (login(login, pass) == null)
        }
    }

    fun loginRegVM(
        login: String,
        fullName: String,
        password: String,
        passwordConfirmation: String
    ) {
        viewModelScope.launch {
            _loginRegFaultFlag.value =
                (loginReg(login, fullName, password, passwordConfirmation) == null)
        }
    }

    fun loginFaultClear() {
        _loginFaultFlag.value = null
    }

    fun loginRegFaultClear() {
        _loginRegFaultFlag.value = null
    }

    private suspend fun login(login: String, pass: String): AuthUploadResponse? {
        val response = postsRetrofitSuspendInterface.updateUser(login, pass)
        if (!response.isSuccessful) {
            println("login->response.isSuccessful ERROR: if-else")
            return null
        }
        val authUploadResponse = requireNotNull(response.body())
        println(authUploadResponse)
        authApp.setAuth(id = authUploadResponse.id, token = authUploadResponse.token)
        return response.body()
    }

    private suspend fun loginReg(login: String, fullName: String, pass: String, passConf: String): AuthUploadResponse? {
        val response = postsRetrofitSuspendInterface.updateUser(login, pass)
        if (!response.isSuccessful) {
            println("login->response.isSuccessful ERROR: if-else")
            return null
        }
        val authUploadResponse = requireNotNull(response.body())
        println(authUploadResponse)
        authApp.setAuth(id = authUploadResponse.id, token = authUploadResponse.token)
        return response.body()
    }
}

data class LoginDefault(val login: String? = null, val pass: String? = null)
data class LoginRegistrationDefault(
    val login: String? = null,
    val pass: String? = null,
    val passConfirmation: String? = null,
    val fullName: String? = null
)
