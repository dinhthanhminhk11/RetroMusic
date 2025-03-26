package code.name.monkey.retromusic.repository.data_source_impl.network

import androidx.annotation.WorkerThread
import code.name.monkey.retromusic.model.BodyRequest
import code.name.monkey.retromusic.network.SongService
import code.name.monkey.retromusic.network.model.response.auth.ResponseDataAuth
import code.name.monkey.retromusic.network.model.response.file.ResponseFile
import code.name.monkey.retromusic.repository.data_source.network.SongRemoteRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class SongRemoteRepositoryImpl(private val songService: SongService) : SongRemoteRepository {

    override suspend fun checkFile(hashFile: String): Response<ResponseFile> =
        songService.checkFile(hashFile)

    @WorkerThread
    override suspend fun uploadChunk(
        fileHash: String,
        chunkIndex: Int,
        file: MultipartBody.Part
    ): Response<Unit> {
        return songService.uploadChunk(fileHash, chunkIndex, file)
    }

    override suspend fun mergeFile(fileHash: BodyRequest): Response<Unit> {
        return songService.mergeFile(fileHash)
    }
}