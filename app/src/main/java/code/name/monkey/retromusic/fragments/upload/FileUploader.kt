package code.name.monkey.retromusic.fragments.upload

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class FileUploader(private val serverUrl: String) {
    private val client = OkHttpClient()

    /**
     * Kiểm tra file đã tồn tại hay chưa
     */
    fun checkFile(file: File): Boolean {
        val fileHash = calculateFileHash(file)

        val requestBody = FormBody.Builder()
            .add("fileHash", fileHash)
            .build()

        val request = Request.Builder()
            .url("$serverUrl/check-file")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            return if (response.isSuccessful) {
                val responseBody = response.body?.string()
                responseBody?.contains("exists") == true
            } else {
                false
            }
        }
    }

    /**
     * Upload từng chunk lên server
     */
    suspend fun uploadChunk(file: File, chunkSize: Int = 5 * 1024 * 1024) {
        withContext(Dispatchers.IO) {
            val fileHash = calculateFileHash(file)
            val totalChunks = (file.length() / chunkSize) + 1

            for (chunkIndex in 0 until totalChunks) {
                val start = chunkIndex * chunkSize
                val end = minOf((start + chunkSize).toInt(), file.length().toInt())

                val chunk = file.readBytes().copyOfRange(start.toInt(), end)

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("fileHash", fileHash)
                    .addFormDataPart("chunkIndex", chunkIndex.toString())
                    .addFormDataPart(
                        "chunk", "chunk_$chunkIndex",
                        RequestBody.create("application/octet-stream".toMediaTypeOrNull(), chunk)
                    )
                    .build()

                val request = Request.Builder()
                    .url("$serverUrl/upload-chunk")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        println("❌ Chunk $chunkIndex upload failed: ${response.message}")
                    } else {
                        println("✅ Chunk $chunkIndex uploaded successfully")
                    }
                }
            }

            // Sau khi upload xong, gọi API merge file
            mergeFile(fileHash, file.name)
        }
    }

    /**
     * Gửi request merge file sau khi upload xong
     */
    private fun mergeFile(fileHash: String, fileName: String) {
        val requestBody = FormBody.Builder()
            .add("fileHash", fileHash)
            .add("fileName", fileName)
            .build()

        val request = Request.Builder()
            .url("$serverUrl/merge-file")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                println("✅ File merged successfully")
            } else {
                println("❌ Failed to merge file: ${response.message}")
            }
        }
    }

    /**
     * Hàm tính hash của file (SHA-256)
     */
    private fun calculateFileHash(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}