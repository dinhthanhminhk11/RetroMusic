package code.name.monkey.retromusic.network

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    object Loading : Result<Nothing>()
    data class Error(
        val code: String? = null,
        val message: String? = null,
        val error: Exception? = null
    ) : Result<Nothing>()

    data object Empty : Result<Nothing>()
}

inline fun <T> Result<T>.handleResult(
    onLoading: () -> Unit,
    onError: (Result.Error) -> Unit,
    onSuccess: (T) -> Unit
) {
    when (this) {
        is Result.Loading -> onLoading()
        is Result.Error -> onError(this)
        is Result.Success -> onSuccess(data)
        else -> {}
    }
}