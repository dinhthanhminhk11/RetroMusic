package code.name.monkey.retromusic.fragments.auth.setpass

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

class SetPassViewModel(private val repository: Repository) : BaseViewModel() {
    private val _authState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    private val _setPassState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    val authState: StateFlow<Result<SuccessResponse>> = _authState.asStateFlow()
    val setPassState: StateFlow<Result<SuccessResponse>> = _setPassState.asStateFlow()

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

    fun setPassword(reqLogin: RequestBody) {
        launchJobCustom(coroutineException(_setPassState)){
            _setPassState.emit(Result.Loading)
            repository.setPassword(reqLogin)
                .flowOn(Dispatchers.IO).catch {
                    flowCatch(it, _setPassState)
                    _setPassState.emit(Result.Error(error = Exception(it.message, it)))
                }
                .collect { data ->
                    _setPassState.emit(data)
                }

        }
    }
}