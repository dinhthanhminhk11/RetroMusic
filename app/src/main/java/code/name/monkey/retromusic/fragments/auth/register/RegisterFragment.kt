package code.name.monkey.retromusic.fragments.auth.register

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import code.name.monkey.retromusic.AuthRequest
import code.name.monkey.retromusic.Constants
import code.name.monkey.retromusic.EMAIL
import code.name.monkey.retromusic.OTP_TYPE
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.TYPE_REGISTER
import code.name.monkey.retromusic.databinding.FragmentRegisterBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.animatedTextChange
import code.name.monkey.retromusic.extensions.handErrorServerProtobuf
import code.name.monkey.retromusic.extensions.hideKeyboard
import code.name.monkey.retromusic.extensions.launchAndCollectIn
import code.name.monkey.retromusic.extensions.showSuccessLoginProtobuf
import code.name.monkey.retromusic.extensions.validateEmail
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.network.handleResult
import code.name.monkey.retromusic.util.ViewUtil.navOptionsByMinh
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel


class RegisterFragment :
    BaseNormalFragment<FragmentRegisterBinding>(FragmentRegisterBinding::inflate) {
    private val viewModel by viewModel<RegisterViewModel>()
    private var isEmailValid = false

    override fun onNetworkChanged() {
        updateButtonState()
        binding.subTitle.animatedTextChange(
            if (isNetworkConnected) getString(R.string.message_verify_account) else getString(R.string.disconnect_internet)
        )
    }

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
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
        viewModel.authState.launchAndCollectIn(viewLifecycleOwner) { result ->
            result.handleResult(onLoading = {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnContinue.isEnabled = false
            }, onError = {
                binding.progressBar.visibility = View.GONE
                binding.btnContinue.isEnabled = true
                it.code?.let {
                    handErrorServerProtobuf(binding.root, it)
                }
            }, onSuccess = { data ->
                binding.progressBar.visibility = View.GONE
                binding.btnContinue.isEnabled = true

                if (Constants.ON_OFF_SETTING_TOAST_SUCCESS) {
                    data.data_.code.let {
                        showSuccessLoginProtobuf(binding.root, it)
                    }
                }

                data.data_.details?.let {
                    if (data.data_.details.verified == false) {
                        findNavController().navigate(
                            R.id.otpFragment, bundleOf(
                                OTP_TYPE to TYPE_REGISTER,
                                EMAIL to binding.username.text.toString()
                            ), navOptionsByMinh
                        )
                    }
                }
            })
        }
    }

    override fun getData() {
    }

    override fun onViewClicked(view: View?) {
        when (view) {
            binding.btnContinue -> {
                hideKeyboard(requireContext(), view)
                val email = binding.username.text.toString()
                if (validateEmail(email)) {
                    binding.btnContinue.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    val text = "{\"email\" : \"${binding.username.text.toString()}\"}"
                    val textEntryPoint = Login.encryptData(text)

                    val authRequest = AuthRequest(textEntryPoint)
                    val byteArray = authRequest.encode()
                    val requestBody =
                        byteArray.toRequestBody("application/x-protobuf".toMediaType())
                    viewModel.register(requestBody)
                }
            }
        }
    }

    private fun updateButtonState() {
        binding.btnContinue.isEnabled = isNetworkConnected && isEmailValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearState()
    }
}