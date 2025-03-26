package code.name.monkey.retromusic.fragments.upload

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.model.BodyRequest
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.network.model.response.auth.ResponseDataAuth
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadViewModel(private val repository: Repository) : ViewModel() {
    private val _uploadState = MutableLiveData<Result<ResponseDataAuth>?>()
    val uploadState: LiveData<Result<ResponseDataAuth>?> get() = _uploadState


    private val _checkFileState = MutableLiveData<Result<Unit>>()
    val checkFileState: LiveData<Result<Unit>> get() = _checkFileState

    fun uploadFile(fileHash: String, file: File, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _checkFileState.postValue(Result.Loading)

            try {
                val checkResponse = repository.checkFile(fileHash)

                if (checkResponse is Result.Success && checkResponse.data.exists == true) {
                    _checkFileState.postValue(Result.Success(Unit))
                    return@launch
                }

                val chunks = splitFileIntoChunks(file)

                for (index in chunks.indices) {
                    val chunk = chunks[index]
                    val requestFile =
                        chunk.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    val multipartBody =
                        MultipartBody.Part.createFormData("file", chunk.name, requestFile)

                    val uploadResponse = repository.uploadChunk(fileHash, index, multipartBody)

                    if (uploadResponse is Result.Error) {
                        _checkFileState.postValue(uploadResponse)
                        return@launch
                    }

                    Log.d("UPLOAD", "Uploaded chunk: $index / ${chunks.size}")
                }

                // Sau khi tất cả các chunk đã upload thành công, gọi mergeFile
                Log.d("UPLOAD", "All chunks uploaded, calling mergeFile")

                val bodyRequest = BodyRequest(
                    "fileHash", fileHash,
                    "totalChunks", chunks.size,
                    "fileName", fileName
                )

                val mergeResponse = repository.mergeFile(bodyRequest)
                _checkFileState.postValue(mergeResponse)

            } catch (e: Exception) {
                _checkFileState.postValue(Result.Error(error = e))
            }
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