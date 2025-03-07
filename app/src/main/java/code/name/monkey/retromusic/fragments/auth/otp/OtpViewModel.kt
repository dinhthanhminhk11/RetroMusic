package code.name.monkey.retromusic.fragments.auth.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.SuccessResponse
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class OtpViewModel(private val repository: Repository) : ViewModel() {
    private val _verifyOtpState = MutableLiveData<Result<SuccessResponse>>()
    private val _reSentState = MutableLiveData<Result<SuccessResponse>>()
    val verifyOtpState: LiveData<Result<SuccessResponse>> get() = _verifyOtpState
    val reSentOtpState: LiveData<Result<SuccessResponse>> get() = _reSentState

    fun verifyOtp(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _verifyOtpState.postValue(Result.Loading)
            try {
                val response = repository.verifyOtp(reqLogin)
                _verifyOtpState.postValue(response)
            } catch (e: Exception) {
                _verifyOtpState.postValue(Result.Error(error = e))
            }
        }
    }

    fun reSentOtp(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _reSentState.postValue(Result.Loading)
            try {
                val response = repository.reSentOtp(reqLogin)
                _reSentState.postValue(response)
            } catch (e: Exception) {
                _reSentState.postValue(Result.Error(error = e))
            }
        }
    }
}