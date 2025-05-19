package code.name.monkey.retromusic.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import code.name.monkey.retromusic.Constants
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class NetworkCheckerInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return if (hasConnection(context)) {
            chain.proceed(chain.request())
        } else {
            throw NoConnectivityException()
        }
    }

    class NoConnectivityException : IOException() {
        override val message: String
            get() = Constants.NO_INTERNET
    }
}

@SuppressLint("ServiceCast")
fun hasConnection(context: Context?): Boolean {
    if (context == null) return false

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}