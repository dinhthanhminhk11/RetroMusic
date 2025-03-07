package code.name.monkey.retromusic.network

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    object Loading : Result<Nothing>()
    data class Error(
        val code: String? = null,
        val message: String? = null,
        val error: Exception? = null
    ) : Result<Nothing>()
}