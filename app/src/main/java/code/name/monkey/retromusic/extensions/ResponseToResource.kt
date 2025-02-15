package code.name.monkey.retromusic.extensions

import code.name.monkey.retromusic.network.Result
import retrofit2.Response


fun <T> responseToResource(response: Response<T>): Result<T> {
    if (response.isSuccessful) {
        response.body()?.let {
            return Result.Success(it)
        }
    }
    return Result.Error(null)
}