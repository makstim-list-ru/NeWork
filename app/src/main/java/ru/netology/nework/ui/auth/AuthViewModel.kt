package ru.netology.nework.ui.auth

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.ui.retrofit.PhotoModel
import ru.netology.nework.ui.retrofit.PostsRetrofitSuspendInterface
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val postsRetrofitSuspendInterface: PostsRetrofitSuspendInterface,
    private val authApp: AuthApp
) : ViewModel() {

    private val _photoLive = MutableLiveData<PhotoModel?>(null)
    val photoLive: LiveData<PhotoModel?>
        get() = _photoLive

    private val _loginFaultFlag = MutableLiveData<Boolean?>(null)
    val loginFaultFlag: LiveData<Boolean?>
        get() = _loginFaultFlag

    private val _loginRegFaultFlag = MutableLiveData<Int?>(null)
    val loginRegFaultFlag: LiveData<Int?>
        get() = _loginRegFaultFlag

    val data = LoginDefault("test", "test") //todo temporary solution
    val dataRegistration =
        LoginRegistrationDefault(
            "test",
            "test",
            "test",
            "Maks Timm",
        ) //todo temporary solution

    fun loginVM(login: String, pass: String) {
        viewModelScope.launch {
            _loginFaultFlag.value = (login(login, pass) == null)
        }
    }

    fun loginRegVM(
        login: String,
        pass: String,
        fullName: String,
    ) {
        viewModelScope.launch {
            loginReg(login, pass, fullName)
        }
    }

    fun loginFaultClear() {
        _loginFaultFlag.value = null
    }

    fun loginRegFaultClear() {
        _loginRegFaultFlag.value = null
    }

    private suspend fun login(login: String, pass: String): AuthUploadResponse? {
        try {
            val response = postsRetrofitSuspendInterface.updateUser(login, pass)

            if (!response.isSuccessful) {
                Timber.i("INFO login->response.isSuccessful ERROR: if-else")
                return null
            }
            val authUploadResponse = requireNotNull(response.body())
            Timber.i("INFO authUploadResponse => $authUploadResponse")
            authApp.setAuth(id = authUploadResponse.id, token = authUploadResponse.token)
            return response.body()
        } catch (_: Exception) {
            return null
        }
    }

    private suspend fun loginReg(
        login: String,
        pass: String,
        fullName: String,
    ) {
        try {
            val file = photoLive.value?.file
            val media = file?.let {
                MultipartBody.Part.createFormData(
                    "file", file.name, file.asRequestBody()
                )
            }

            val mediaType = "text/plain; charset=utf-8".toMediaType()
            val response = postsRetrofitSuspendInterface.registerUser(
                login.toRequestBody(mediaType),
                pass.toRequestBody(mediaType),
                fullName.toRequestBody(mediaType),
                media
            )
            if (response.isSuccessful) {
                val authUploadResponse = requireNotNull(response.body())
                authApp.setAuth(id = authUploadResponse.id, token = authUploadResponse.token)
            }
            _loginRegFaultFlag.value = response.code()
        } catch (_: Exception) {
            _loginRegFaultFlag.value = -10_000
        }
    }

    fun changePhotoVM(uri: Uri?, file: File?) {
        _photoLive.value = PhotoModel(uri, file)
    }

    fun removePhotoVM() {
        _photoLive.value = null
    }

}

data class LoginDefault(val login: String? = null, val pass: String? = null)
data class LoginRegistrationDefault(
    val login: String? = null,
    val pass: String? = null,
    val passConf: String? = null,
    val fullName: String? = null,
)
