package code.name.monkey.retromusic.fragments.auth.login

import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.SuccessResponse
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.repository.Repository
import code.name.monkey.retromusic.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import timber.log.Timber

class LoginViewModel(private val repository: Repository) : BaseViewModel() {
    private val _authState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    private val _accountState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    val authState: StateFlow<Result<SuccessResponse>> = _authState.asStateFlow()
    val accountState: StateFlow<Result<SuccessResponse>> = _accountState.asStateFlow()

    init {
        Timber.d("LoginViewModel created")
    }

    fun login(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.emit(Result.Loading)
            try {
                val response = repository.login(reqLogin)
                _authState.emit(response)
            } catch (e: Exception) {
                _authState.emit(Result.Error(error = e))
            }
        }
    }

    fun checkAccount(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _accountState.emit(Result.Loading)
            try {
                val response = repository.checkAccount(reqLogin)
                _accountState.emit(response)
            } catch (e: Exception) {
                _accountState.emit(Result.Error(error = e))
            }
        }
    }

    fun clearAuthState() {
        _authState.value = Result.Empty
    }

    fun clearAccountState() {
        _accountState.value = Result.Empty
    }

}