package code.name.monkey.retromusic.network.interceptor

import android.content.Context
import android.content.Intent
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber

class ErrorInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 500) {
            Timber.d("ErrorInterceptor code : 500")
            return response
        }
        if (response.code == 401) {
            Timber.d("ErrorInterceptor code : 401")

            return response
        }
        response.peekBody(Long.MAX_VALUE).let {
            val jsonResponse = JSONObject(it.string())
            val code = jsonResponse.optString("code", "")
            Timber.d("ErrorInterceptor jsonResponse : $jsonResponse")
            when (code) {
                ErrorCode.AUTHENTICATION_FAILED.code -> {
                    Timber.d("ErrorInterceptor Need to logout because Token is Expired")
                    val intent = Intent(ErrorAction.ACTION_FORCE_LOGOUT)
                    context.sendBroadcast(intent)
                }
            }
        }


        return response
    }
}

enum class ErrorCode(val code: String) {
    NO_INTERNET("101"),
    SIGN_IN_ERROR("102"),
    WRONG_OTP("103"),
    SIGN_IN_ANOTHER_DEVICE("104"),
    TOKEN_EXPIRED("EAUTH402"),
    AUTHENTICATION_FAILED("401")
}

object ErrorAction {
    const val ACTION_FORCE_LOGOUT = "com.action.ACTION_FORCE_LOGOUT"
}