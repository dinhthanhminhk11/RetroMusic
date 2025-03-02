package code.name.monkey.retromusic.fragments.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.network.model.request.auth.REQLogin
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import code.name.monkey.retromusic.network.Result
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: Repository) : ViewModel() {
    private val _authState = MutableLiveData<Result<LoginResponseNative>>()
    private val _accountstate = MutableLiveData<Result<LoginResponseNative>>()
    val authState: LiveData<Result<LoginResponseNative>> get() = _authState
    val accountState: LiveData<Result<LoginResponseNative>> get() = _accountstate
    fun login(reqLogin: REQLogin) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.postValue(Result.Loading)
            try {
                val response = repository.login(reqLogin)
                _authState.postValue(response)
            } catch (e: Exception) {
                _authState.postValue(Result.Error(e))
            }
        }
    }

    fun checkAccount(reqLogin: REQLogin) {
        viewModelScope.launch(Dispatchers.IO) {
            _accountstate.postValue(Result.Loading)
            try {
                val response = repository.checkAccount(reqLogin)
                _accountstate.postValue(response)
            } catch (e: Exception) {
                _accountstate.postValue(Result.Error(e))
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
}