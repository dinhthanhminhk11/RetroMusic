package code.name.monkey.retromusic.fragments.upload


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.network.model.response.file.ResponseFile
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UploadViewModel(private val repository: Repository) :
    ViewModel() {

    private val _checkFileState = MutableLiveData<Result<ResponseFile>>()
    val checkFileState: LiveData<Result<ResponseFile>> get() = _checkFileState

    fun checkFile(fileHash: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _checkFileState.postValue(Result.Loading)
            try {
                val checkResponse = repository.checkFile(fileHash)
                _checkFileState.postValue(checkResponse)
            } catch (e: Exception) {
                _checkFileState.postValue(Result.Error(error = e))
            }
        }
    }
}