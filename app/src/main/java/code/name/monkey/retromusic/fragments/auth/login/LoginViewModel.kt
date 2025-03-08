package code.name.monkey.retromusic.fragments.auth.login

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
import timber.log.Timber

class LoginViewModel(private val repository: Repository) : ViewModel() {
    private val _authState = MutableLiveData<Result<SuccessResponse>?>()
    private val _accountstate = MutableLiveData<Result<SuccessResponse>?>()
    val authState: LiveData<Result<SuccessResponse>?> get() = _authState
    val accountState: LiveData<Result<SuccessResponse>?> get() = _accountstate

    init {
        Timber.d("LoginViewModel created")
    }

    fun login(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.postValue(Result.Loading)
            try {
                val response = repository.login(reqLogin)
                _authState.postValue(response)
            } catch (e: Exception) {
                _authState.postValue(Result.Error(error = e))
            }
        }
    }

    fun checkAccount(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _accountstate.postValue(Result.Loading)
            try {
                val response = repository.checkAccount(reqLogin)
                _accountstate.postValue(response)
            } catch (e: Exception) {
                _accountstate.postValue(Result.Error(error = e))
            }
        }
    }

//    fun checkAccount(reqLogin: REQLogin): LiveData<Result<LoginResponseNative>> =
//        liveData(Dispatchers.IO) {
//            emit(Result.Loading)
//            try {
//                val loginResponse = repository.checkAccount(reqLogin)
//                emit(loginResponse)
//            } catch (e: Exception) {
//                emit(Result.Error(e))
//            }
//        }

    /*fun login(reqLogin: REQLogin): LiveData<Result<LoginResponseNative>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val loginResponse = repository.login(reqLogin)
                emit(loginResponse)
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }*/

    fun clearState() {
        _authState.value = null
        _accountstate.value = null
    }
}