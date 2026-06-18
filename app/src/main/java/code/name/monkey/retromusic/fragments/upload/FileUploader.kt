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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class FileUploader(private val context: Context, private val repository: Repository) {

    private val CHUNK_SIZE_MOBILE = 1 * 1024 * 1024;// Mobile: 1MB, upload tuần tự
    private val CHUNK_SIZE_WIFI = 2 * 1024 * 1024;// WiFi: 2MB, tối đa 2 chunk song song đoạn này khả năng con cho nhỏ nữa lại mạng server không đủ

    private val SIZE_STREAM_CHUNKS_WIFI = 4
    private val SIZE_STREAM_CHUNKS_MOBILE = 1
    private val MAX_TRIES = 3

    private val TAG: String = "FileUploader"

    // chạy đơn giản load những file chunk lên chunk nào lỗi trả ra exception sau khi up hết lên bắn ra upload chưa thành công
    suspend fun uploadFile(
        fileHash: String, file: File, fileName: String, fileSize: Int,
        uploadedChunks: ArrayList<Int>? = null,
        onProgress: (Int) -> Unit,
        onError: (() -> Unit)? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val networkType = getNetworkType()
        val (chunkSize, maxConcurrentUploads) = when (networkType) {
            NetworkType.WIFI -> Pair(CHUNK_SIZE_WIFI, SIZE_STREAM_CHUNKS_WIFI)
            NetworkType.MOBILE -> Pair(CHUNK_SIZE_MOBILE, SIZE_STREAM_CHUNKS_MOBILE)
            else -> Pair(CHUNK_SIZE_MOBILE, SIZE_STREAM_CHUNKS_MOBILE)
        }

        val totalChunks = ((file.length() + chunkSize - 1) / chunkSize).toInt().coerceAtLeast(1)
        val remainingChunks = (0 until totalChunks).filter { uploadedChunks?.contains(it) == false }

        if (remainingChunks.isEmpty()) return@withContext Result.Success(Unit)

        val semaphore = Semaphore(maxConcurrentUploads)
        val uploadedChunksCounter =
            AtomicInteger(uploadedChunks?.size ?: 0)// dùng atomic và volatile để đếm da luồng
        val failedChunks = ConcurrentHashMap<Int, Exception>() //  lưu lại các chunk retry bị lỗi

        try {
            val deferredList = splitFileIntoChunksStream(file, chunkSize)
                .filter { (index, _) -> index in remainingChunks } // suửa lại logic đoạn này nếu vào trường hợp file nặng quá thì con check đc filter nhưng file có trên server
                .map { (index, chunkData) ->
                    async {
                        semaphore.acquire()
                        try {
                            var attempt = 0
                            var success = false

                            while (attempt < MAX_TRIES && !success) {
                                attempt++
                                try {
                                    val requestBody =
                                        chunkData.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                                    val multipartBody = MultipartBody.Part.createFormData(
                                        FILE, "$fileName.chunk.$index", requestBody
                                    )

                                    val uploadResponse =
                                        repository.uploadChunk(fileHash, index, multipartBody)

                                    if (uploadResponse is Result.Success) {
                                        val completed = uploadedChunksCounter.incrementAndGet()
                                        val progress = (completed * 100) / totalChunks
                                        onProgress(progress)
                                        Log.i(TAG, "progress  : $progress percent")
                                        success = true

                                        failedChunks.remove(index)
                                    } else {
                                        Log.d(TAG, "Chunk $index upload failed")
                                        throw Exception("Chunk $index upload failed")
                                    }

                                } catch (e: Exception) {
                                    if (attempt < MAX_TRIES) {
                                        delay(attempt * 1000L) // suspend 1 second
                                    } else {
                                        failedChunks[index] = e
                                    }
                                }
                            }
                            Result.Success(Unit)
                        } finally {
                            semaphore.release()
                        }
                    }
                }.toList() // Chuyển thành list để chờ `awaitAll()`

            deferredList.awaitAll()
            if (failedChunks.isNotEmpty()) {
                onError?.invoke()
                // show error
                Log.i(TAG, "Upload failed: Some chunks failed. Retry possible.")
                return@withContext Result.Error(error = Exception("Upload failed: Some chunks failed. Retry possible."))
            }

            val bodyRequest = BodyRequest(
                FILE_HASH, fileHash,
                TOTAL_CHUNKS, totalChunks,
                FILE_NAME, fileName,
                FILE_SIZE, fileSize
            )
            return@withContext repository.mergeFile(bodyRequest)

        } catch (e: Exception) {
            return@withContext Result.Error(error = e)
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

    private fun splitFileIntoChunksStream(
        file: File,
        chunkSize: Int
    ): Sequence<Pair<Int, ByteArray>> = sequence {
        file.inputStream().buffered().use { inputStream ->
            var chunkIndex = 0
            val buffer = ByteArray(chunkSize)

            while (true) {
                var offset = 0
                while (offset < chunkSize) {
                    val read = inputStream.read(buffer, offset, chunkSize - offset)
                    if (read == -1) break
                    offset += read
                }
                if (offset == 0) break
                yield(chunkIndex to buffer.copyOf(offset))
                chunkIndex++
            }
        }
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