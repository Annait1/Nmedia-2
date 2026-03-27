package ru.netology.nmedia.viewmodel
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.UsersApi

import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Token
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val usersApi: UsersApi,
    private val appAuth: AppAuth,
): ViewModel() {

    private val _state = MutableLiveData(SignInState())
    val state: LiveData<SignInState> = _state

    private val _success = MutableLiveData<Unit>()
    val success: LiveData<Unit> = _success

    fun signIn(login: String, pass: String) = viewModelScope.launch {
        try {
            _state.value = SignInState(loading = true)

            val token: Token =
                usersApi.authenticate(login, pass)

            appAuth.setAuth(token.id, token.token)

            _state.value = SignInState()
            _success.value = Unit

        } catch (e: Exception) {
            _state.value = SignInState(error = true)
        }
    }
}

data class SignInState(
    val loading: Boolean = false,
    val error: Boolean = false
)