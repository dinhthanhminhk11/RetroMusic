package code.name.monkey.retromusic.fragments.auth.setpass

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

class SetPassViewModel(private val repository: Repository) : ViewModel() {
    private val _authState = MutableLiveData<Result<SuccessResponse>>()
    private val _setPassState = MutableLiveData<Result<SuccessResponse>>()
    val authState: LiveData<Result<SuccessResponse>> get() = _authState
    val setPassState: LiveData<Result<SuccessResponse>> get() = _setPassState

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

    fun setPassword(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _setPassState.postValue(Result.Loading)
            try {
                val response = repository.setPassword(reqLogin)
                _setPassState.postValue(response)
            } catch (e: Exception) {
                _setPassState.postValue(Result.Error(error = e))
            }
        }
    }

}