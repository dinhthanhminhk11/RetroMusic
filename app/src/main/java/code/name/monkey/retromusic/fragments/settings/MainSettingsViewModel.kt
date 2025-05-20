package code.name.monkey.retromusic.fragments.settings

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

class MainSettingsViewModel(
    private val repository: Repository
) : BaseViewModel() {
    private val _authState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    val authState: StateFlow<Result<SuccessResponse>> = _authState.asStateFlow()
    fun logout(reqLogin: RequestBody) {
        launchJobCustom(coroutineException(_authState)) {
            _authState.emit(Result.Loading)
            repository.logout(reqLogin).flowOn(Dispatchers.IO).catch { e ->
                flowCatch(e, _authState)
                _authState.emit(Result.Error(error = Exception(e.message, e)))
            }.collect { data ->
                _authState.emit(data)
            }
        }
    }
}