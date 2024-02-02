package ru.netology.nmedia.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.api.ApiServiceModule
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val apiService: ApiService,
    private val auth: AppAuth
): ViewModel() {

    fun signIn(login: String, password: String) {
        viewModelScope.launch {
            try {
                val response = apiService.updateUser(login, password)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                Log.d("MyLog", "id body=${body.id} token body=${body.token}")

                auth.setAuth(body.id, body.token)

            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                Log.d("MyLog", "ошибка signIn")
                //  throw MyUnknownError
            }

        }

    }

}