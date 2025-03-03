package code.name.monkey.retromusic.fragments.auth.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.network.model.request.auth.REQLogin
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OtpViewModel(private val repository: Repository) : ViewModel() {
    private val _verifyOtpState = MutableLiveData<Result<LoginResponseNative>>()
    private val _reSentState = MutableLiveData<Result<LoginResponseNative>>()
    val verifyOtpState: LiveData<Result<LoginResponseNative>> get() = _verifyOtpState
    val reSentOtpState: LiveData<Result<LoginResponseNative>> get() = _reSentState

    fun verifyOtp(reqLogin: REQLogin) {
        viewModelScope.launch(Dispatchers.IO) {
            _verifyOtpState.postValue(Result.Loading)
            try {
                val response = repository.verifyOtp(reqLogin)
                _verifyOtpState.postValue(response)
            } catch (e: Exception) {
                _verifyOtpState.postValue(Result.Error(e))
            }
        }
    }

    fun reSentOtp(reqLogin: REQLogin) {
        viewModelScope.launch(Dispatchers.IO) {
            _reSentState.postValue(Result.Loading)
            try {
                val response = repository.reSentOtp(reqLogin)
                _reSentState.postValue(response)
            } catch (e: Exception) {
                _reSentState.postValue(Result.Error(e))
            }
        }
    }
}