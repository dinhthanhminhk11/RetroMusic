package code.name.monkey.retromusic.network

import code.name.monkey.retromusic.model.BodyRequest
import code.name.monkey.retromusic.network.model.response.file.ResponseFile
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SongService {

    @POST(Endpoint.SONG_CHECK_FILE)
    suspend fun checkFile(
        @Path("fileHash") fileHash: String,
    ): Response<ResponseFile>


    @Multipart
    @POST(Endpoint.SONG_UPLOAD_CHUNK)
    suspend fun uploadChunk(
        @Path("fileHash") fileHash: String,
        @Path("chunkIndex") chunkIndex: Int,
        @Part file: MultipartBody.Part
    ): Response<Unit>


    @POST(Endpoint.SONG_MERGE_FILE)
    suspend fun mergeFile(
        @Body fileHash: BodyRequest
    ): Response<Unit>
}