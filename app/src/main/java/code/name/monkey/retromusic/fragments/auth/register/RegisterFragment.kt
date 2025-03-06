package code.name.monkey.retromusic.fragments.auth.register

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import code.name.monkey.retromusic.AuthRequest
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentRegisterBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.animatedTextChange
import code.name.monkey.retromusic.extensions.validateEmail
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.network.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel


class RegisterFragment :
    BaseNormalFragment<FragmentRegisterBinding>(FragmentRegisterBinding::inflate) {
    private val viewModel by viewModel<RegisterViewModel>()
    private var isNetworkConnected = false;
    private var isEmailValid = false;

    override fun onNetworkChanged(isConnected: Boolean) {
        isNetworkConnected = isConnected
        updateButtonState()
        binding.subTitle.animatedTextChange(
            if (isNetworkConnected) getString(R.string.message_verify_account) else getString(R.string.disconnect_internet)
        )
    }

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnContinue.isEnabled = false
        binding.username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                isEmailValid = validateEmail(email)
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
                    //todo START OTP

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
                val email = binding.username.text.toString()
                if (validateEmail(email)) {
                    binding.btnContinue.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    val text =
                        "{\"email\" : \"${binding.username.text.toString()}\"}"
                    val textEntryPoint = Login.encryptData(text)

                    val authRequest = AuthRequest(textEntryPoint)
                    val byteArray = authRequest.encode()
                    val requestBody =
                        RequestBody.create("application/x-protobuf".toMediaType(), byteArray)
                    viewModel.register(requestBody)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.btnContinue.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }, 2000)
            }
        }
    }

    private fun updateButtonState() {
        binding.btnContinue.isEnabled = isNetworkConnected && isEmailValid
    }
}