package code.name.monkey.retromusic.fragments.upload

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class UploadViewModel(context: Context, private val repository: Repository) :
    ViewModel() {

    private val _checkFileState = MutableLiveData<Result<Unit>>()
    private val _stateProcess = MutableLiveData<Int>()
    val checkFileState: LiveData<Result<Unit>> get() = _checkFileState
    val stateProcess: LiveData<Int> get() = _stateProcess

    private var uploadManager: FileUploader = FileUploader(context, repository)

    fun uploadFile(fileHash: String, file: File, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                uploadManager.uploadFileWithProgress(fileHash, file, fileName) { progress ->
                    _checkFileState.postValue(Result.Loading)
                    Log.e("MinhProcess", "Process Loading : " + progress)
                    _stateProcess.postValue(progress)
                }

            _checkFileState.postValue(result)
        }
    }

    private fun splitFileIntoChunks(file: File, chunkSize: Int = 2 * 1024 * 1024): List<File> {
        val chunks = mutableListOf<File>()
        val buffer = ByteArray(chunkSize)
        val inputStream = file.inputStream()

        var bytesRead: Int
        var chunkIndex = 0
        while (inputStream.read(buffer).also { bytesRead = it } > 0) {
            val chunkFile = File(file.parent, "${file.name}_chunk_$chunkIndex")
            chunkFile.outputStream().use { it.write(buffer, 0, bytesRead) }
            chunks.add(chunkFile)
            chunkIndex++
        }

        inputStream.close()
        return chunks
    }
}