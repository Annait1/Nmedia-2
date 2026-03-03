package ru.netology.nmedia.viewmodel
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.UsersApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Token


class SignInViewModel : ViewModel() {

    private val _state = MutableLiveData(SignInState())
    val state: LiveData<SignInState> = _state

    private val _success = MutableLiveData<Unit>()
    val success: LiveData<Unit> = _success

    fun signIn(login: String, pass: String) = viewModelScope.launch {
        try {
            _state.value = SignInState(loading = true)

            val token: Token =
                UsersApiService.service.authenticate(login, pass)

            AppAuth.getInstance().setAuth(token.id, token.token)

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