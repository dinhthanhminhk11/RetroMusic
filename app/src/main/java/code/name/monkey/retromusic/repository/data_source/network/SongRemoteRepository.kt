package code.name.monkey.retromusic.repository.data_source.network

import code.name.monkey.retromusic.model.BodyRequest
import code.name.monkey.retromusic.network.model.response.file.ResponseFile
import okhttp3.MultipartBody
import retrofit2.Response

interface SongRemoteRepository {

    suspend fun checkFile(
        hashFile: String,
    ): Response<ResponseFile>


    suspend fun uploadChunk(
        fileHash: String,
        chunkIndex: Int,
        file: MultipartBody.Part
    ): Response<Unit>


    suspend fun mergeFile(
        fileHash: BodyRequest
    ): Response<Unit>
}