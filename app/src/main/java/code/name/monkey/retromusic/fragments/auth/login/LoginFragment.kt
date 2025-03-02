package code.name.monkey.retromusic.fragments.auth.login

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.activities.MainActivity
import code.name.monkey.retromusic.databinding.FragmentLoginBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.animatedTextChange
import code.name.monkey.retromusic.extensions.validateEmail
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.network.model.request.auth.REQLogin
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

        binding.btnContinue.setOnClickListener {
            if (!binding.btnContinue.isEnabled) return@setOnClickListener
            binding.btnContinue.isEnabled = false

            val email = binding.username.text.toString()
            if (validateEmail(email)) {
                binding.progressBar.visibility = View.VISIBLE
                var textencrpt: String
                if (isLoginByPass) {
                    textencrpt =
                        "{\"email\" : \"${binding.username.text.toString()}\" , \"password\" : \"${binding.password.text.toString()}\"}"
                    val textEntryPoint = Login.encryptData(textencrpt)
                    loginViewModel.login(REQLogin(textEntryPoint))
                } else {
                    textencrpt = "{\"email\" : \"${binding.username.text.toString()}\"}"
                    val textEntryPoint = Login.encryptData(textencrpt)
                    loginViewModel.checkAccount(REQLogin(textEntryPoint))
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnContinue.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }, 2000)
        }
    }

    override fun initObserver() {
        loginViewModel.authState.observe(viewLifecycleOwner) { result ->
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
                    //todo save info user
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().finish()
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

                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.isEnabled = true
                    // todo start otp
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