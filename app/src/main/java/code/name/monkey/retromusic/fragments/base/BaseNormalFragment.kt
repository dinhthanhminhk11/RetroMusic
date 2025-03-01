package code.name.monkey.retromusic.fragments.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import timber.log.Timber

abstract class BaseNormalFragment<T : ViewBinding>(private val bindingInflater: (layoutInflater: LayoutInflater) -> T) :
    Fragment(), View.OnClickListener {
    var _binding: T? = null
    protected val binding get() = _binding!!
    var TAG: String = this.javaClass.simpleName

    private var connectivityManager: ConnectivityManager? = null
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            requireActivity().runOnUiThread {
                onNetworkChanged(true)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            requireActivity().runOnUiThread {
                onNetworkChanged(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("BaseFragment => onViewCreated")
        registerNetworkCallback()
        initView()
        initObserver()
    }

    override fun onClick(v: View?) {
        val isClickAble = true
        Timber.d("on click view : isClickAble = $isClickAble, View : ${v.toString()}")
        if (isClickAble) {
            onViewClicked(v)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        unregisterNetworkCallback()
    }

    private fun registerNetworkCallback() {
        connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager?.registerNetworkCallback(request, networkCallback)
    }

    private fun unregisterNetworkCallback() {
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    protected abstract fun onNetworkChanged(isConnected: Boolean)

    open fun initArgs() {}
    abstract fun initView()
    abstract fun initObserver()
    abstract fun getData()
    abstract fun onViewClicked(view: View?)
}