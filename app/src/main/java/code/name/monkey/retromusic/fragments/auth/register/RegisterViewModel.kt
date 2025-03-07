package code.name.monkey.retromusic.fragments.auth.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.SuccessResponse
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.network.model.request.auth.REQLogin
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class RegisterViewModel(private val repository: Repository) : ViewModel() {
    val _authState = MutableLiveData<Result<SuccessResponse>>()
    val authState: LiveData<Result<SuccessResponse>> get() = _authState

    fun register(requestBody: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.postValue(Result.Loading)
            try {
                val response = repository.register(requestBody)
                _authState.postValue(response)
            } catch (e: Exception) {
                _authState.postValue(Result.Error(error = e))
            }
        }
    }
}