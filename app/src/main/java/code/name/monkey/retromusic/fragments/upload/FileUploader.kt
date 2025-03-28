package code.name.monkey.retromusic.fragments.upload

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import code.name.monkey.retromusic.FILE
import code.name.monkey.retromusic.FILE_HASH
import code.name.monkey.retromusic.FILE_NAME
import code.name.monkey.retromusic.FILE_SIZE
import code.name.monkey.retromusic.TOTAL_CHUNKS
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
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.RandomAccessFile
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
        fileHash: String, file: File, fileName: String, fileSize: Int,
        upLoadedChunks: ArrayList<Int>? = null,
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

        val remainingChunks = chunks.indices.filter { upLoadedChunks?.contains(it) == false }

        if (remainingChunks.isEmpty()) {
            return Result.Success(Unit)
        }

        val semaphore = Semaphore(maxConcurrentUploads)
        val uploadedChunksCounter =
            AtomicInteger(upLoadedChunks?.size ?: 0) // dùng atomic và volatile để đếm da luồng

        val failedChunks = ConcurrentHashMap<Int, Exception>() // Lưu các chunk bị lỗi

        return withContext(Dispatchers.IO) {
            try {
                val deferredList = remainingChunks.map { index ->
                    async {
                        semaphore.acquire()
                        try {
                            var attempt = 0
                            var success = false

                            while (attempt < MAX_TRIES && !success) {
                                attempt++
                                try {
                                    val chunk = chunks[index]
                                    val requestFile =
                                        chunk.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                                    val multipartBody = MultipartBody.Part.createFormData(
                                        FILE,
                                        "$fileName.chunk.$index",
                                        requestFile
                                    )

                                    val uploadResponse =
                                        repository.uploadChunk(fileHash, index, multipartBody)

                                    if (uploadResponse is Result.Error) {
                                        Log.d(TAG, "Chunk $index upload failed")
                                        throw Exception("Chunk $index upload failed")
                                    }
                                    if (uploadResponse is Result.Success) {
                                        val completed = uploadedChunksCounter.incrementAndGet()
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
                    FILE_HASH, fileHash,
                    TOTAL_CHUNKS, chunks.size,
                    FILE_NAME, fileName,
                    FILE_SIZE, fileSize
                )
                return@withContext repository.mergeFile(bodyRequest)

            } catch (e: Exception) {
                return@withContext Result.Error(error = e)
            }
        }
    }


    //testing upload sequential
//    suspend fun uploadFileWithProgress(
//        fileHash: String,
//        file: File,
//        fileName: String,
//        onProgress: (Int) -> Unit
//    ): Result<Unit> { //sequential uploads, dùng khi server latency yếu
//        if (!file.exists() || !file.canRead()) {
//            return Result.Error(error = Exception("Không thể đọc file"))
//        }
//
//        val networkType = getNetworkType()
//
//        val chunkSize = when (networkType) {
//            NetworkType.WIFI -> CHUNK_SIZE_WIFI
//            NetworkType.MOBILE -> CHUNK_SIZE_MOBILE
//            else -> CHUNK_SIZE_MOBILE
//        }
//
//        return withContext(Dispatchers.IO) {
//            try {
//                val chunks = splitFileIntoChunks(file, chunkSize)
//                val totalChunks = chunks.size
//
//                for (index in chunks.indices) {
//                    val chunk = chunks[index]
//                    val requestFile =
//                        chunk.asRequestBody("application/octet-stream".toMediaTypeOrNull())
//                    val multipartBody =
//                        MultipartBody.Part.createFormData("file", chunk.name, requestFile)
//
//                    val uploadResponse = repository.uploadChunk(fileHash, index, multipartBody)
//                    if (uploadResponse is Result.Error) {
//                        return@withContext uploadResponse
//                    }
//
//
//                    val progress = ((index + 1) * 100) / totalChunks
//                    onProgress(progress)
//                }
//
//                val bodyRequest = BodyRequest(
//                    "fileHash", fileHash,
//                    "totalChunks", chunks.size,
//                    "fileName", fileName
//                )
//                return@withContext repository.mergeFile(bodyRequest)
//
//            } catch (e: Exception) {
//                return@withContext Result.Error(error = e)
//            }
//        }
//    }

    private fun splitFileIntoChunks(file: File, chunkSize: Int): List<ByteArray> {
        val chunks = mutableListOf<ByteArray>()
        val inputStream = RandomAccessFile(file, "r")

        var bytesRead: Int
        val buffer = ByteArray(chunkSize)

        while (inputStream.channel.position() < inputStream.length()) {
            bytesRead = inputStream.read(buffer)
            if (bytesRead > 0) {
                chunks.add(buffer.copyOf(bytesRead))
            }
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