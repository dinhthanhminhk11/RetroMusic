package code.name.monkey.retromusic.fragments.auth.setpass

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

class SetPassViewModel(private val repository: Repository) : ViewModel() {
    private val _authState = MutableLiveData<Result<LoginResponseNative>>()
    private val _setPassState = MutableLiveData<Result<LoginResponseNative>>()
    val authState: LiveData<Result<LoginResponseNative>> get() = _authState
    val setPassState: LiveData<Result<LoginResponseNative>> get() = _setPassState

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

    fun setPassword(reqLogin: REQLogin) {
        viewModelScope.launch(Dispatchers.IO) {
            _setPassState.postValue(Result.Loading)
            try {
                val response = repository.setPassword(reqLogin)
                _setPassState.postValue(response)
            } catch (e: Exception) {
                _setPassState.postValue(Result.Error(e))
            }
        }
    }

}