package code.name.monkey.retromusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.extensions.mapErrorMessage
import code.name.monkey.retromusic.network.NetworkCheckerInterceptor
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.network.interceptor.ErrorCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class BaseViewModel() : ViewModel() {
    protected var jobCall: Job? = null
    protected fun launchJobCustom(
        exceptionHandler: CoroutineExceptionHandler,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job = viewModelScope.launch(context + exceptionHandler, start, block)

    open fun <T> coroutineException(data: MutableStateFlow<Result<T>>): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            if (exception !is CancellationException) {
                when (exception) {
                    is HttpException -> {
                        Timber.e("Handler  coroutineException HttpException")
                        Timber.e("Handler  coroutineException HttpException exception ${exception.message()}")
                        val errorResponse =
                            exception.response()?.errorBody()?.string()?.mapErrorMessage()
                        if (errorResponse != null) {
                            if (errorResponse.errorCode != null) {
                                data.value = Result.Error(
                                    errorResponse.message!!,
                                    errorResponse.errorCode.toString(),
                                )
                            } else if (errorResponse.status != null) {
                                Timber.e("Handler  HttpException $errorResponse?.status ")
                                data.value = Result.Error(
                                    errorResponse.message!!,
                                    errorResponse.errorCode.toString(),
                                )
                            } else data.value = Result.Error(
                                "Unknown error!",
                                (-1).toString(),
                            )
                        } else {
                            data.value = Result.Error(
                                exception.code().toString(), exception.message()
                            )
                            Timber.e("Handler  coroutineException errorResponse null with msg = ${exception.message()} ${exception.code()}")
                        }
                    }

                    else -> {
                        Timber.e("Handler  otherExceptions")
                        otherExceptions(exception, data)
                    }
                }
                data.value = Result.Empty
            }
        }

    open suspend fun <T> flowCatchShared(
        exception: Throwable, emitter: MutableSharedFlow<Result<T>>, tag: String = ""
    ) {
        if (exception is HttpException) {
            Timber.e("flowCatchShared $tag HttpException ${exception.message}")
            val errorResponse = exception.response()?.errorBody()?.string()
            Timber.e("flowCatchShared $tag HttpException errorResponse $errorResponse")
            try {
                if (errorResponse != null) {
                    val errorObject = JSONObject(errorResponse)
                    val errorCode = errorObject.optInt("error_code", -1)
                    val errorMessage = errorObject.optString("message", "Unknown error")
                    emitter.emit(
                        Result.Error(
                            errorCode.toString(), errorMessage
                        )
                    )
                    emitter.emit(Result.Empty)
                    return
                }
            } catch (e: Exception) {
                e.message?.let {
                    emitter.emit(Result.Error(0.toString(), it, e))
                }
                emitter.emit(Result.Empty)
            }
            Timber.e(
                "flowCatchShared HttpException2 ${exception.code()} | ${
                    exception.response()?.errorBody()?.string()
                }"
            )
        } else {
            Timber.e("flowCatchShared otherExceptions%s", exception.message)
        }
        emitter.emit(Result.Empty)
    }

    open fun <T> flowCatch(
        exception: Throwable, data: MutableStateFlow<Result<T>>, tag: String = ""
    ) {
        if (exception is HttpException) {
            Timber.e("flowCatch $tag  HttpException ${exception.message}")
            val errorResponse = exception.response()?.errorBody()?.string()
            Timber.e("flowCatch  $tag HttpException errorResponse $errorResponse")
            try {
                if (errorResponse != null) {
                    val errorObject = JSONObject(errorResponse)
                    val errorCode = errorObject.optInt("error_code", -1)
                    val errorMessage = errorObject.optString("message", "Unknown error")
                    data.value = Result.Error(errorCode.toString(), errorMessage)
                    data.value = Result.Empty
                    return
                }
            } catch (e: Exception) {
                data.value = e.message?.let { Result.Error(0.toString(), it, e) }!!
                data.value = Result.Empty
            }
            Timber.e(
                "flowCatch  HttpException2 ${exception.code()} | ${
                    exception.response()?.errorBody()?.string()
                }"
            )
        } else {
            Timber.e("flowCatch  otherExceptions%s", exception.message)
            otherExceptions(exception, data)
        }
        data.value = Result.Empty
    }

    open fun <T> otherExceptions(exception: Throwable, data: MutableStateFlow<Result<T>>) {
        when (exception) {
            is NetworkCheckerInterceptor.NoConnectivityException -> {
                data.value = Result.Error(
                    "Network connection lost, please try again", ErrorCode.NO_INTERNET.code
                )
            }

            else -> {
                Timber.e(
                    "Handler IOException ${exception.message} Oops! Something went wrong. Please try again later."
                )
                data.value = Result.Error(
                    "Oops! Something went wrong. Please try again later.", "400.toString()"
                )
            }
        }
    }


    private suspend fun <T> otherExceptionsShared(
        exception: Throwable,
        emitter: MutableSharedFlow<Result<T>>,
    ) {
        when (exception) {
            is SocketTimeoutException -> {
                emitter.emit(Result.Error("0", "Connection timeout"))
            }

            is IOException -> {
                emitter.emit(Result.Error("0", "No internet connection"))
            }

            else -> {
                emitter.emit(
                    Result.Error(
                        "0",
                        exception.message ?: "Unknown error",
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        jobCall?.cancel()
    }
}