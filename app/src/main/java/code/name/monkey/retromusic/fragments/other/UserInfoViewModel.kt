package code.name.monkey.retromusic.fragments.other

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.network.model.response.auth.ResponseDataAuth
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserInfoViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _authState = MutableLiveData<Result<ResponseDataAuth>?>()
    val authState: LiveData<Result<ResponseDataAuth>?> get() = _authState
    fun updateUserInfo(
        token: String,
        data: RequestBody?,
        imageFile: MultipartBody.Part?,
        imageBannerFile: MultipartBody.Part?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.postValue(Result.Loading)
            try {
                val response = repository.updateUserInfo(token, data, imageFile, imageBannerFile)
                _authState.postValue(response)
            } catch (e: Exception) {
                _authState.postValue(Result.Error(error = e))
            }
        }
    }
}