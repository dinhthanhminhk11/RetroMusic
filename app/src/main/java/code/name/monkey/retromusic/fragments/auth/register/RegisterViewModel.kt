package code.name.monkey.retromusic.fragments.auth.register

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

class RegisterViewModel(private val repository: Repository) : ViewModel() {
    private val _authState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    val authState: StateFlow<Result<SuccessResponse>> = _authState.asStateFlow()

    fun register(requestBody: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.emit(Result.Loading)
            try {
                val response = repository.register(requestBody)
                _authState.emit(response)
            } catch (e: Exception) {
                _authState.emit(Result.Error(error = e))
            }
        }
    }

    fun clearState() {
        _authState.value = Result.Empty
    }
}