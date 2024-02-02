package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    auth: AppAuth
) : ViewModel() {
    val data = auth.authStateFlow

    val authenticated:Boolean
        get() = data.value.id !=0L

//    val data: LiveData<AuthState> = AppAuth.getInstance()
//        .authStateFlow
//        .asLiveData(Dispatchers.Default)

//    val authenticated: Boolean
//        get() = AppAuth.getInstance().authStateFlow.value.id != 0L
}