package code.name.monkey.retromusic.fragments.auth.login

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import code.name.monkey.retromusic.ACCOUNT_CAN_LOGIN
import code.name.monkey.retromusic.ACCOUNT_CAN_NOT_LOGIN
import code.name.monkey.retromusic.ACCOUNT_LOCKED
import code.name.monkey.retromusic.AuthRequest
import code.name.monkey.retromusic.EMAIL
import code.name.monkey.retromusic.LOGIN_SUCCESS
import code.name.monkey.retromusic.OTP_CONFIRMED
import code.name.monkey.retromusic.OTP_TYPE
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.TYPE_REGISTER
import code.name.monkey.retromusic.activities.MainActivity
import code.name.monkey.retromusic.databinding.FragmentLoginBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.animatedTextChange
import code.name.monkey.retromusic.extensions.handErrorServerProtobuf
import code.name.monkey.retromusic.extensions.showSuccessLoginProtobuf
import code.name.monkey.retromusic.extensions.validateEmail
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.util.ViewUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseNormalFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {
    private val loginViewModel by viewModel<LoginViewModel>()
    private var isLoginByPass = true
    private var isNetworkConnected = false
    private var isEmailValid = false
    private var isPasswordValid = false
    override fun onNetworkChanged(isConnected: Boolean) {
        isNetworkConnected = isConnected
        updateButtonState()
        updateSubTitleState()
    }

    override fun initView() {
        binding.loginTextView.setOnClickListener(this)
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
        loginViewModel.authState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnContinue.isEnabled = false
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.isEnabled = true
                    result.code?.let {
                        handErrorServerProtobuf(binding.root, it)
                    }
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.isEnabled = true
                    result.data.let {
                        if (result.data.success) {
                            result.data.data_.let {
                                when (result.data.data_.code) {
                                    LOGIN_SUCCESS -> {
                                        showSuccessLoginProtobuf(binding.root, LOGIN_SUCCESS)
                                        // TODO: save info user
                                        val intent =
                                            Intent(requireContext(), MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        loginViewModel.accountState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnContinue.isEnabled = true
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.isEnabled = true
                    result.code?.let {
                        handErrorServerProtobuf(binding.root, it) { errorCode ->
                            when (errorCode) {
                                ACCOUNT_CAN_NOT_LOGIN -> {
                                    findNavController().navigate(
                                        R.id.otpFragment,
                                        bundleOf(
                                            OTP_TYPE to TYPE_REGISTER,
                                            EMAIL to binding.username.text.toString()
                                        ),
                                        ViewUtil.navOptions
                                    )
                                }
                            }
                        }
                    }
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.isEnabled = true
                    result.data.let {
                        if (result.data.success) {
                            result.data.data_.let {
                                when (result.data.data_.code) {
                                    ACCOUNT_CAN_LOGIN -> {
                                        showSuccessLoginProtobuf(binding.root, LOGIN_SUCCESS)
                                        // TODO: save info user
                                        val intent =
                                            Intent(requireContext(), MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getData() {

    }

    override fun onViewClicked(view: View?) {
        when (view) {
            binding.loginTextView -> {
                isLoginByPass = !isLoginByPass
                binding.loginTextView.text =
                    if (isLoginByPass) getString(R.string.login_by_otp) else getString(R.string.login_by_pass)
                updateSubTitleState()
                binding.passwordContainer.visibility =
                    if (isLoginByPass) View.VISIBLE else View.GONE
                updateButtonState()
            }

            binding.btnContinue -> {
                if (!binding.btnContinue.isEnabled) return
                binding.btnContinue.isEnabled = false

                val email = binding.username.text.toString()
                if (validateEmail(email)) {
                    binding.progressBar.visibility = View.VISIBLE
                    var textencrpt: String
                    if (isLoginByPass) {
                        textencrpt =
                            "{\"email\" : \"${binding.username.text.toString()}\" , \"password\" : \"${binding.password.text.toString()}\"}"
                        val textEntryPoint = Login.encryptData(textencrpt)

                        val authRequest = AuthRequest(textEntryPoint)
                        val byteArray = authRequest.encode()
                        val requestBody =
                            RequestBody.create("application/x-protobuf".toMediaType(), byteArray)
                        loginViewModel.login(requestBody)
                    } else {
                        textencrpt = "{\"email\" : \"${binding.username.text.toString()}\"}"
                        val textEntryPoint = Login.encryptData(textencrpt)
                        val authRequest = AuthRequest(textEntryPoint)
                        val byteArray = authRequest.encode()
                        val requestBody =
                            RequestBody.create("application/x-protobuf".toMediaType(), byteArray)
                        loginViewModel.checkAccount(requestBody)
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    binding.btnContinue.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }, 2000)
            }
        }
    }

    private fun updateButtonState() {
        val checkEnable = isNetworkConnected && isEmailValid && (!isLoginByPass || isPasswordValid)
        if (binding.btnContinue.isEnabled != checkEnable) {
            binding.btnContinue.isEnabled = checkEnable
        }
    }

    private fun updateSubTitleState() {
        binding.subTitle.animatedTextChange(
            if (isNetworkConnected) {
                if (isLoginByPass) getString(R.string.enter_form) else getString(R.string.enter_form_login_by_otp)
            } else getString(R.string.disconnect_internet)
        )
    }

}