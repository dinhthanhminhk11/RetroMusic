package code.name.monkey.retromusic.fragments.auth.setpass

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import code.name.monkey.retromusic.AuthRequest
import code.name.monkey.retromusic.Constants.ON_OFF_SETTING_TOAST_SUCCESS
import code.name.monkey.retromusic.LOGIN_SUCCESS
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.activities.MainActivity
import code.name.monkey.retromusic.databinding.FragmentSetPassBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.animatedTextChange
import code.name.monkey.retromusic.extensions.handErrorServerProtobuf
import code.name.monkey.retromusic.extensions.hideKeyboard
import code.name.monkey.retromusic.extensions.launchAndCollectIn
import code.name.monkey.retromusic.extensions.showConfirmDialog
import code.name.monkey.retromusic.extensions.showSuccessLoginProtobuf
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.model.auth.UserClient
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.network.handleResult
import code.name.monkey.retromusic.util.PreferenceUtil
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel


class SetPassFragment :
    BaseNormalFragment<FragmentSetPassBinding>(FragmentSetPassBinding::inflate) {
    private val viewModel by viewModel<SetPassViewModel>()
    private var isPasswordValid = false
    private val arguments by navArgs<SetPassFragmentArgs>()

    override fun onNetworkChanged() {
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
            showConfirmDialog(
                context = requireActivity(),
                title = getString(R.string.notification),
                message = getString(R.string.text_confirm_setPass),
                textPositiveButton = getString(R.string.out),
                textNegativeButton = getString(R.string.cancel),
                onConfirm = {
                    findNavController().navigateUp()
                })
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
        viewModel.authState.launchAndCollectIn(viewLifecycleOwner) { result ->
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

                    if (ON_OFF_SETTING_TOAST_SUCCESS) {
                        result.data.data_.code.let {
                            showSuccessLoginProtobuf(binding.root, it)
                        }
                    }

                    result.data.let {
                        if (result.data.success) {
                            result.data.data_.let {
                                when (result.data.data_.code) {
                                    LOGIN_SUCCESS -> {
                                        showSuccessLoginProtobuf(binding.root, LOGIN_SUCCESS)

                                        result.data.data_.details?.data_.let { dataLogin ->
                                            val gson = Gson()
                                            val userClient: UserClient = gson.fromJson(
                                                Login.decryptData(dataLogin.toString()),
                                                UserClient::class.java
                                            )
                                            PreferenceUtil.userClient = userClient
                                        }

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

                is Result.Empty -> {

                }
            }
        }
        viewModel.setPassState.launchAndCollectIn(viewLifecycleOwner) { result ->
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
                //todo START login
                if (ON_OFF_SETTING_TOAST_SUCCESS) {
                    data.data_.code.let {
                        showSuccessLoginProtobuf(binding.root, it)
                    }
                }
                showConfirmDialog(
                    context = requireActivity(),
                    title = getString(R.string.notification),
                    message = getString(R.string.text_confirm_setPass_login),
                    textPositiveButton = getString(R.string.agree),
                    textNegativeButton = getString(R.string.cancel),
                    onConfirm = {
                        val textEncrypt =
                            "{\"email\" : \"${arguments.email}\" , \"password\" : \"${binding.password.text.toString()}\"}"
                        val textEntryPoint = Login.encryptData(textEncrypt)

                        val authRequest = AuthRequest(textEntryPoint)
                        val byteArray = authRequest.encode()
                        val requestBody =
                            byteArray.toRequestBody("application/x-protobuf".toMediaType())
                        viewModel.login(requestBody)
                    },
                    onCancel = {
                        findNavController().navigateUp()
                    })
            })
        }
    }

    override fun getData() {
    }

    override fun onViewClicked(view: View?) {
        when (view) {
            binding.btnContinue -> {
                hideKeyboard(requireContext(), view)
                binding.progressBar.visibility = View.VISIBLE
                binding.btnContinue.isEnabled = false
                val text =
                    "{\"email\" : \"${arguments.email}\" , \"password\" : \"${binding.password.text.toString()}\"}"
                val textEntryPoint = Login.encryptData(text)

                val authRequest = AuthRequest(textEntryPoint)
                val byteArray = authRequest.encode()
                val requestBody = byteArray.toRequestBody("application/x-protobuf".toMediaType())
                viewModel.setPassword(requestBody)
            }
        }
    }

}