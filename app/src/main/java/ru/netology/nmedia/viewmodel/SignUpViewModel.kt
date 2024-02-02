package ru.netology.nmedia.viewmodel

import android.net.Uri
import android.util.Log
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
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.model.PhotoModel
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val apiService: ApiService,
    private val auth: AppAuth
): ViewModel() {

    private val _photo = MutableLiveData(noPhoto)

    val photoAvatarUrl: MutableLiveData<String>? = null
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun signUp(login: String, password: String, name: String) {

        viewModelScope.launch {
            try {
                val response = apiService.registerUser(login, password, name)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                Log.d("MyLog", "signUp id body=${body.id} token body=${body.token}")

                auth.setAuth(body.id, body.token)

            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                Log.d("MyLog", "ошибка signUp")
                //  throw MyUnknownError
            }
        }
    }

    fun save(login: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                when (_photo.value) {
                    noPhoto -> signUp(login, password, name)
                    else -> _photo.value?.file?.let { file ->
                        signUpWithPhoto(login, password, name, MediaUpload(file))
                    }
                }

            } catch (e: Exception) {
                println(e.stackTrace)
            }
        }
        _photo.value = noPhoto
    }

    fun signUpWithPhoto(login: String, pass: String, name: String, upload: MediaUpload) {

        viewModelScope.launch {
            try {
                val media = MultipartBody.Part.createFormData(
                    "file", upload.file.name, upload.file.asRequestBody()
                )

                upload(media)

                val response = apiService.registerWithPhoto(
                    login.toRequestBody("text/plain".toMediaType()),
                    pass.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    media
                )
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                Log.d(
                    "MyLog",
                    "signUpWithPhoto id body=${body.id} token body=${body.token}"
                )

                auth.setAuth(body.id, body.token)

            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                Log.d("MyLog", "ошибка signUpWithPhoto")
                //  throw MyUnknownError
            }
            _photo.value = noPhoto
        }
    }

    suspend fun upload(media: MultipartBody.Part) {
        try {
//            val media = MultipartBody.Part.createFormData(
//                "file", upload.file.name, upload.file.asRequestBody()
//            )
            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            photoAvatarUrl?.value =
                response.body()?.id ?: throw ApiError(response.code(), response.message())

       //     return media

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            Log.d("MyLog", "ошибка upload")
            //  throw MyUnknownError
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }
}