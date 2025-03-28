package code.name.monkey.retromusic.fragments.upload

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import code.name.monkey.retromusic.model.BodyRequest
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class FileUploader(private val context: Context, private val repository: Repository) {

    private val CHUNK_SIZE_MOBILE = 1 * 1024 * 1024;// Mobile: 1MB, upload tuần tự
    private val CHUNK_SIZE_WIFI = 2 * 1024 * 1024;// WiFi: 2MB, tối đa 4 chunk song song

    private val SIZE_STREAM_CHUNKS_WIFI = 4
    private val SIZE_STREAM_CHUNKS_MOBILE = 1
    private val MAX_TRIES = 3

    private val TAG: String = "FileUploader"

    // chạy đơn giản load những file chunk lên chunk nào lỗi trả ra exception sau khi up hết lên bắn ra upload chưa thành công
    suspend fun uploadFile(
        fileHash: String, file: File, fileName: String,
        onProgress: (Int) -> Unit,
        onError: (() -> Unit)? = null
    ): Result<Unit> { // concurrent uploads dùng khi server latency khỏe
        val networkType = getNetworkType()

        val (chunkSize, maxConcurrentUploads) = when (networkType) {
            NetworkType.WIFI -> Pair(CHUNK_SIZE_WIFI, SIZE_STREAM_CHUNKS_WIFI)
            NetworkType.MOBILE -> Pair(CHUNK_SIZE_MOBILE, SIZE_STREAM_CHUNKS_MOBILE)
            else -> Pair(CHUNK_SIZE_MOBILE, SIZE_STREAM_CHUNKS_MOBILE)
        }

        val chunks = splitFileIntoChunks(file, chunkSize)
        val totalChunks = chunks.size
        val semaphore = Semaphore(maxConcurrentUploads)
        val uploadedChunks = AtomicInteger(0) // dm dùng thằng để ddeems da luong
        val failedChunks = ConcurrentHashMap<Int, Exception>() // Lưu các chunk bị lỗi

        return withContext(Dispatchers.IO) {
            try {
                val deferredList = chunks.mapIndexed { index, chunk ->
                    async {
                        semaphore.acquire()
                        try {
                            var attempt = 0
                            var success = false

                            while (attempt < MAX_TRIES && !success) {
                                attempt++
                                try {
                                    val requestFile =
                                        chunk.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                                    val multipartBody = MultipartBody.Part.createFormData(
                                        "file",
                                        chunk.name,
                                        requestFile
                                    )

                                    val uploadResponse =
                                        repository.uploadChunk(fileHash, index, multipartBody)

                                    if (uploadResponse is Result.Error) {
                                        Log.d(TAG, "Chunk $index upload failed")
                                        throw Exception("Chunk $index upload failed")
                                    }
                                    if (uploadResponse is Result.Success) {
                                        val completed = uploadedChunks.incrementAndGet()
                                        val progress = (completed * 100) / totalChunks
                                        onProgress(progress)
                                        Log.i(TAG, "progress  : $progress percent")
                                        success = true

                                        failedChunks.remove(index)
                                    }

                                } catch (e: Exception) {
                                    if (attempt < MAX_TRIES) {
                                        delay(attempt * 1000L) // suspend 1 second
                                    } else {
                                        failedChunks[index] = e
                                        Log.e(TAG, "Chunk $index failed after $MAX_TRIES retries")
                                    }
                                }
                            }
                            Result.Success(Unit)
                        } finally {
                            semaphore.release()
                        }
                    }
                }

                deferredList.awaitAll()
                if (failedChunks.isNotEmpty()) {
                    onError?.invoke()// show error
                    Log.i(TAG, "Upload failed: Some chunks failed. Retry possible.")
                    return@withContext Result.Error(
                        error = Exception("Upload failed: Some chunks failed. Retry possible.")
                    )
                }

                val bodyRequest = BodyRequest(
                    "fileHash", fileHash,
                    "totalChunks", chunks.size,
                    "fileName", fileName
                )
                return@withContext repository.mergeFile(bodyRequest)

            } catch (e: Exception) {
                return@withContext Result.Error(error = e)
            }
        }
    }

    suspend fun uploadFileWithProgress(
        fileHash: String,
        file: File,
        fileName: String,
        onProgress: (Int) -> Unit
    ): Result<Unit> { //sequential uploads, dùng khi server latency yếu
        if (!file.exists() || !file.canRead()) {
            return Result.Error(error = Exception("Không thể đọc file"))
        }

        val networkType = getNetworkType()

        val chunkSize = when (networkType) {
            NetworkType.WIFI -> CHUNK_SIZE_WIFI
            NetworkType.MOBILE -> CHUNK_SIZE_MOBILE
            else -> CHUNK_SIZE_MOBILE
        }

        return withContext(Dispatchers.IO) {
            try {
                val chunks = splitFileIntoChunks(file, chunkSize)
                val totalChunks = chunks.size

                for (index in chunks.indices) {
                    val chunk = chunks[index]
                    val requestFile =
                        chunk.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    val multipartBody =
                        MultipartBody.Part.createFormData("file", chunk.name, requestFile)

                    val uploadResponse = repository.uploadChunk(fileHash, index, multipartBody)
                    if (uploadResponse is Result.Error) {
                        return@withContext uploadResponse
                    }


                    val progress = ((index + 1) * 100) / totalChunks
                    onProgress(progress)
                }

                val bodyRequest = BodyRequest(
                    "fileHash", fileHash,
                    "totalChunks", chunks.size,
                    "fileName", fileName
                )
                return@withContext repository.mergeFile(bodyRequest)

            } catch (e: Exception) {
                return@withContext Result.Error(error = e)
            }
        }
    }

    private fun splitFileIntoChunks(file: File, chunkSize: Int): List<File> {
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

    private fun getNetworkType(): NetworkType {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return NetworkType.UNKNOWN
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.UNKNOWN

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
            else -> NetworkType.UNKNOWN
        }
    }

    enum class NetworkType {
        WIFI, MOBILE, UNKNOWN
    }
}