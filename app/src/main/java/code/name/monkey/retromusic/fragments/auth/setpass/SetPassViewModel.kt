package code.name.monkey.retromusic.fragments.auth.setpass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.SuccessResponse
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class SetPassViewModel(private val repository: Repository) : ViewModel() {
    private val _authState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    private val _setPassState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    val authState: StateFlow<Result<SuccessResponse>> get() = _authState.asStateFlow()
    val setPassState: StateFlow<Result<SuccessResponse>> get() = _setPassState.asStateFlow()

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

    fun setPassword(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _setPassState.emit(Result.Loading)
            try {
                val response = repository.setPassword(reqLogin)
                _setPassState.emit(response)
            } catch (e: Exception) {
                _setPassState.emit(Result.Error(error = e))
            }
        }
    }
}