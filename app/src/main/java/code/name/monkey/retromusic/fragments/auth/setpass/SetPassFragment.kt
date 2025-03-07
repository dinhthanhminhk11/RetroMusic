package code.name.monkey.retromusic.fragments.auth.setpass

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import code.name.monkey.retromusic.AuthRequest
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentSetPassBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.animatedTextChange
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.network.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel


class SetPassFragment :
    BaseNormalFragment<FragmentSetPassBinding>(FragmentSetPassBinding::inflate) {
    private val viewModel by viewModel<SetPassViewModel>()
    private var isNetworkConnected = false;
    private var isPasswordValid = false;
    private var email: String? = null;

    companion object {
        const val EMAIL = "EMAIL"
        fun newInstance(email: String? = null): SetPassFragment {
            val fragment = SetPassFragment()
            val args = Bundle()
            args.putString(EMAIL, email)
            fragment.arguments = args
            return fragment
        }
    }

    override fun initArgs() {
        super.initArgs()
        email = arguments?.getString(EMAIL)
    }

    override fun onNetworkChanged(isConnected: Boolean) {
        isNetworkConnected = isConnected
        updateButtonState()
        binding.subTitle.animatedTextChange(
            if (isNetworkConnected) getString(R.string.set_password_content) else getString(R.string.disconnect_internet)
        )
    }

    private fun updateButtonState() {
        binding.btnContinue.isEnabled = isNetworkConnected && isPasswordValid
    }

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnContinue.isEnabled = false
        binding.password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isPasswordValid = (s?.length ?: 0) >= 8
                updateButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnContinue.setOnClickListener(this)
    }

    override fun initObserver() {
        viewModel.authState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnContinue.isEnabled = true
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.isEnabled = true
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.isEnabled = true
                    //todo login success

                }
            }
        }
        viewModel.setPassState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnContinue.isEnabled = true
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.isEnabled = true
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.isEnabled = true
                    //todo START login

                }
            }
        }
    }

    override fun getData() {
    }

    override fun onViewClicked(view: View?) {
        when (view) {
            binding.btnContinue -> {
                if (!binding.btnContinue.isEnabled) return
                binding.progressBar.visibility = View.VISIBLE
                binding.btnContinue.isEnabled = false
                val text =
                    "{\"email\" : \"${email}\" , \"password\" : \"${binding.password.text.toString()}\"}"
                val textEntryPoint = Login.encryptData(text)

                val authRequest = AuthRequest(textEntryPoint)
                val byteArray = authRequest.encode()
                val requestBody =
                    RequestBody.create("application/x-protobuf".toMediaType(), byteArray)
                viewModel.setPassword(requestBody)
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.btnContinue.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }, 2000)
            }
        }
    }

}