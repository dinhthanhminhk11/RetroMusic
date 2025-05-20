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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
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
        launchJobCustom(coroutineException(_authState)) {
            _authState.emit(Result.Loading)
            repository.login(reqLogin)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    flowCatch(e, _authState)
                    _authState.emit(Result.Error(error = Exception(e.message, e)))
                }
                .collect { data ->
                    _authState.emit(data)
                }
        }
    }

    fun checkAccount(reqLogin: RequestBody) {
        launchJobCustom(coroutineException(_accountState)) {
            _accountState.emit(Result.Loading)
            repository.checkAccount(reqLogin)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    flowCatch(e, _accountState)
                    _accountState.emit(Result.Error(error = Exception(e.message, e)))

                }.collect { data ->
                    _accountState.emit(data)
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