package code.name.monkey.retromusic.fragments.auth.otp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.CountDownTimer
import android.text.Html
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import code.name.monkey.retromusic.ACCOUNT_LOCKED
import code.name.monkey.retromusic.AuthRequest
import code.name.monkey.retromusic.Constants.ON_OFF_SETTING_TOAST_SUCCESS
import code.name.monkey.retromusic.EMAIL
import code.name.monkey.retromusic.LOGIN_SUCCESS
import code.name.monkey.retromusic.OTP_CONFIRMED
import code.name.monkey.retromusic.OTP_EXPIRED
import code.name.monkey.retromusic.OTP_NOT_VALID
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.activities.MainActivity
import code.name.monkey.retromusic.databinding.FragmentOtpBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.animatedTextChange
import code.name.monkey.retromusic.extensions.handErrorServerProtobuf
import code.name.monkey.retromusic.extensions.hideKeyboard
import code.name.monkey.retromusic.extensions.launchAndCollectIn
import code.name.monkey.retromusic.extensions.showConfirmDialog
import code.name.monkey.retromusic.extensions.showSuccessLoginProtobuf
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.model.auth.UserClient
import code.name.monkey.retromusic.network.handleResult
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.ViewUtil
import code.name.monkey.retromusic.views.custom.otp.OnOtpCompletionListener
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel


class OtpFragment : BaseNormalFragment<FragmentOtpBinding>(FragmentOtpBinding::inflate),
    OnOtpCompletionListener {

    private val viewModel by viewModel<OtpViewModel>()
    private lateinit var countdownTimer: CountDownTimer
    private var countResent: Int = 1
    private val otpValidityDurationInMillis: Long = 60_000
    private val arguments by navArgs<OtpFragmentArgs>()

    override fun onNetworkChanged() {
        val otpMessage = getString(R.string.content_otp_text_view, arguments.email)
        binding.subtitle.animatedTextChange(
            if (isNetworkConnected) Html.fromHtml(
                otpMessage, Html.FROM_HTML_MODE_LEGACY
            ) else getString(
                R.string.disconnect_internet
            )
        )
    }

    override fun initView() {
        binding.otp.setAnimationEnable(true)
        binding.otp.requestFocus()
        binding.otp.setOtpCompletionListener(this)
        binding.toolbar.setNavigationOnClickListener {
            showConfirmDialog(
                context = requireActivity(),
                title = getString(R.string.notification),
                message = getString(R.string.text_confirm_otp),
                textPositiveButton = getString(R.string.out),
                textNegativeButton = getString(R.string.cancel),
                onConfirm = {
                    countdownTimer.cancel()
                    findNavController().navigateUp()
                })
        }
        binding.resent.setOnClickListener(this)
        val otpMessage = getString(R.string.content_otp_text_view, arguments.email)
        binding.subtitle.text = Html.fromHtml(otpMessage, Html.FROM_HTML_MODE_LEGACY)
        startCountdownTimer()
    }

    override fun initObserver() {
        viewModel.verifyOtpState.launchAndCollectIn(viewLifecycleOwner) { result ->
            result.handleResult(onLoading = {
                binding.progressBar.visibility = View.VISIBLE
            }, onError = {
                binding.progressBar.visibility = View.GONE
                it.code?.let {
                    handErrorServerProtobuf(binding.root, it) { errorCode ->
                        when (errorCode) {
                            ACCOUNT_LOCKED -> {
                                findNavController().popBackStack()
                            }

                            OTP_NOT_VALID, OTP_EXPIRED -> {
                                binding.otp.setLineColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        code.name.monkey.appthemehelper.R.color.md_red_500
                                    )
                                )
                                binding.otp.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        code.name.monkey.appthemehelper.R.color.md_red_500
                                    )
                                )
                            }
                        }
                    }
                }
            }, onSuccess = { data ->
                binding.progressBar.visibility = View.GONE

                if (ON_OFF_SETTING_TOAST_SUCCESS) {
                    data.data_.code.let {
                        showSuccessLoginProtobuf(binding.root, it)
                    }
                }

                data.let {
                    if (data.success) {
                        data.data_.let {
                            when (data.data_.code) {
                                LOGIN_SUCCESS -> {
                                    showSuccessLoginProtobuf(binding.root, LOGIN_SUCCESS)
                                    data.data_.details?.data_.let { dataLogin ->
                                        val gson = Gson()
                                        val userClient: UserClient = gson.fromJson(
                                            Login.decryptData(dataLogin.toString()),
                                            UserClient::class.java
                                        )
                                        PreferenceUtil.userClient = userClient
                                    }
                                    val intent = Intent(requireContext(), MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    requireActivity().finish()
                                }

                                OTP_CONFIRMED -> {
                                    val navOptions =
                                        ViewUtil.createNavOptions(true, R.id.otpFragment)
                                    findNavController().navigate(
                                        R.id.setPassFragment, bundleOf(
                                            EMAIL to arguments.email
                                        ), navOptions
                                    )
                                }
                            }
                        }
                    }
                }
            })
        }

        viewModel.reSentOtpState.launchAndCollectIn(viewLifecycleOwner) { result ->
            result.handleResult(onLoading = {
                binding.progressBar.visibility = View.VISIBLE
            }, onError = {
                binding.progressBar.visibility = View.GONE
                it.code?.let {
                    handErrorServerProtobuf(binding.root, it) { errorCode ->
                        when (errorCode) {
                            ACCOUNT_LOCKED -> {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }, onSuccess = { data ->
                binding.progressBar.visibility = View.GONE
                startCountdownTimer()
                countResent++

                if (ON_OFF_SETTING_TOAST_SUCCESS) {
                    data.data_.code.let {
                        showSuccessLoginProtobuf(binding.root, it)
                    }
                }
            })
        }
    }

    override fun getData() {

    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer.cancel()
    }

    override fun onViewClicked(view: View?) {
        when (view) {
            binding.resent -> {
                countdownTimer.cancel()

                val text =
                    "{\"email\" : \"${arguments.email}\" , \"type\" : \"${arguments.otptype}\"}"
                val textEntryPoint = Login.encryptData(text)

                val authRequest = AuthRequest(textEntryPoint)
                val byteArray = authRequest.encode()
                val requestBody =
                    byteArray.toRequestBody("application/x-protobuf".toMediaType())
                viewModel.reSentOtp(requestBody)
            }
        }
    }

    private fun startCountdownTimer() {
        countdownTimer = object : CountDownTimer(otpValidityDurationInMillis, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                val downTime = String.format("%d:%02d", minutes, seconds)
                binding.resent.text = downTime
                binding.resent.isEnabled = false
            }

            override fun onFinish() {
                binding.resent.text = getString(R.string.sent_again)
                binding.resent.isEnabled = true
                if (countResent == 5) {
                    binding.resent.visibility = View.GONE
                }
            }
        }
        countdownTimer.start()
    }

    override fun onOtpCompleted(otp: String?) {
        binding.progressBar.visibility = View.VISIBLE
        hideKeyboard(requireContext(), view)
        val text =
            "{\"email\" : \"${arguments.email}\" , \"otp\" : \"${otp}\" , \"type\" : \"${arguments.otptype}\"}"
        val textEntryPoint = Login.encryptData(text)

        val authRequest = AuthRequest(textEntryPoint)
        val byteArray = authRequest.encode()
        val requestBody =
            byteArray.toRequestBody("application/x-protobuf".toMediaType())
        viewModel.verifyOtp(requestBody)
    }

}