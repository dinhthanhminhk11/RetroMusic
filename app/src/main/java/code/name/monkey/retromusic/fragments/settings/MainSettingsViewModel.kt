package code.name.monkey.retromusic.fragments.settings

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

class MainSettingsViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _authState = MutableLiveData<Result<SuccessResponse>?>()
    val authState: LiveData<Result<SuccessResponse>?> get() = _authState
    fun logout(reqLogin: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.postValue(Result.Loading)
            try {
                val response = repository.logout(reqLogin)
                _authState.postValue(response)
            } catch (e: Exception) {
                _authState.postValue(Result.Error(error = e))
            }
        }
    }
}