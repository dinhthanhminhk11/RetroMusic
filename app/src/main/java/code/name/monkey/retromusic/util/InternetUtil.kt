package code.name.monkey.retromusic.util

import android.app.Application
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

object InternetUtil {
    var internetState = MutableStateFlow(true)
    fun init(application: Application) {
        val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Timber.e("The default network is now: $network")
                    connectivityManager.getNetworkCapabilities(network)?.let {
                        internetState.value = when {
                            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            //for other device how are able to connect with Ethernet
                            it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            //for check internet over Bluetooth
                            it.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                            else -> false
                        }
                    }
                }

                override fun onLost(network: Network) {
                    Timber.e("The application no longer has a default network. The last default network was $network")
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    Timber.e("The default network changed capabilities: $networkCapabilities")
                }

                override fun onLinkPropertiesChanged(
                    network: Network,
                    linkProperties: LinkProperties
                ) {
                    Timber.e("The default network changed link properties: $linkProperties")
                }
            })
        }
    }

    fun isNetworkAvailable() = internetState.value
}