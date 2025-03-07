package code.name.monkey.retromusic.extensions

import code.name.monkey.retromusic.ErrorResponse
import code.name.monkey.retromusic.network.Result
import com.squareup.wire.ProtoAdapter
import okhttp3.ResponseBody
import retrofit2.Response

fun <T> responseToResource(response: Response<T>): Result<T> {
    if (response.isSuccessful) {
        response.body()?.let {
            return Result.Success(it)
        }
    }
    return Result.Error(null)
}

fun <T> responseToResourceProtobuf(
    response: Response<ResponseBody>,
    adapter: ProtoAdapter<T>
): Result<T> {
    return if (response.isSuccessful) {
        response.body()?.let { body ->
            try {
                val successResponse = adapter.decode(body.bytes())
                Result.Success(successResponse)
            } catch (e: Exception) {
                Result.Error(error = e, message = "Failed to parse success response")
            }
        } ?: Result.Error(message = "Empty success response")
    } else {
        try {
            val errorBody = response.errorBody()?.bytes()
            if (errorBody != null) {
                val errorResponse = ErrorResponse.ADAPTER.decode(errorBody)
                Result.Error(
                    code = errorResponse.error.code,
                    message = errorResponse.error.message
                )
            } else {
                Result.Error(message = "Unknown error")
            }
        } catch (e: Exception) {
            Result.Error(error = e, message = "Failed to parse error response")
        }
    }
}
