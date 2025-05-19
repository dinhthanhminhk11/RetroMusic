package code.name.monkey.retromusic.fragments.auth.otp

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

class OtpViewModel(private val repository: Repository) : ViewModel() {
    private val _verifyOtpState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    private val _reSentState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    val verifyOtpState: StateFlow<Result<SuccessResponse>> = _verifyOtpState.asStateFlow()
    val reSentOtpState: StateFlow<Result<SuccessResponse>> = _reSentState.asStateFlow()

    fun verifyOtp(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _verifyOtpState.emit(Result.Loading)
            try {
                val response = repository.verifyOtp(reqLogin)
                _verifyOtpState.emit(response)
            } catch (e: Exception) {
                _verifyOtpState.emit(Result.Error(error = e))
            }
        }
    }

    fun reSentOtp(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _reSentState.emit(Result.Loading)
            try {
                val response = repository.reSentOtp(reqLogin)
                _reSentState.emit(response)
            } catch (e: Exception) {
                _reSentState.emit(Result.Error(error = e))
            }
        }
    }
}