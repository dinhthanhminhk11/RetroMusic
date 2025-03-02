package code.name.monkey.retromusic.fragments.auth.register

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentRegisterBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.animatedTextChange
import code.name.monkey.retromusic.extensions.validateEmail
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment


class RegisterFragment :
    BaseNormalFragment<FragmentRegisterBinding>(FragmentRegisterBinding::inflate) {

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

        binding.btnContinue.setOnClickListener {
            val email = binding.username.text.toString()
            if (validateEmail(email)) {
                binding.btnContinue.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
                val text =
                    "{\"email\" : \"${binding.username.text.toString()}\"}"
                val textEntryPoint = Login.encryptData(text)
            }
        }
    }

    override fun initObserver() {
    }

    override fun getData() {
    }

    override fun onViewClicked(view: View?) {

    }

    private fun updateButtonState() {
        binding.btnContinue.isEnabled = isNetworkConnected && isEmailValid
    }
}