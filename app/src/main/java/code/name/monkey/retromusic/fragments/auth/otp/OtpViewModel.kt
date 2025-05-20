package code.name.monkey.retromusic.fragments.auth.otp

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

class OtpViewModel(private val repository: Repository) : BaseViewModel() {
    private val _verifyOtpState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    private val _reSentState = MutableStateFlow<Result<SuccessResponse>>(Result.Empty)
    val verifyOtpState: StateFlow<Result<SuccessResponse>> = _verifyOtpState.asStateFlow()
    val reSentOtpState: StateFlow<Result<SuccessResponse>> = _reSentState.asStateFlow()

    fun verifyOtp(reqLogin: RequestBody) {
        launchJobCustom(coroutineException(_verifyOtpState)) {
            _verifyOtpState.emit(Result.Loading)
            repository.verifyOtp(reqLogin).flowOn(Dispatchers.IO)
                .catch {
                    flowCatch(it, _verifyOtpState)
                    _verifyOtpState.emit(Result.Error(error = Exception(it.message, it)))
                }.collect { data ->
                    _verifyOtpState.emit(data)
                }
        }
    }

    fun reSentOtp(reqLogin: RequestBody) {
        launchJobCustom(coroutineException(_reSentState)) {
            _reSentState.emit(Result.Loading)
            repository.reSentOtp(reqLogin).flowOn(Dispatchers.IO)
                .catch {
                    flowCatch(it, _reSentState)
                    _reSentState.emit(Result.Error(error = Exception(it.message, it)))
                }.collect { data ->
                    _reSentState.emit(data)
                }
        }
    }
}