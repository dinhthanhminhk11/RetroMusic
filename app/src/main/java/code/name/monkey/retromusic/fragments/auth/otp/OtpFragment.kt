package code.name.monkey.retromusic.fragments.auth.otp

import android.os.CountDownTimer
import android.text.Html
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import code.name.monkey.retromusic.AuthRequest
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentOtpBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.animatedTextChange
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.views.custom.otp.OnOtpCompletionListener
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel


class OtpFragment : BaseNormalFragment<FragmentOtpBinding>(FragmentOtpBinding::inflate),
    OnOtpCompletionListener {

    private val viewModel by viewModel<OtpViewModel>()
    private var isNetworkConnected = false;
    private lateinit var countdownTimer: CountDownTimer
    private var countResent: Int = 1;
    private val otpValidityDurationInMillis: Long = 60_000
    private val arguments by navArgs<OtpFragmentArgs>()


    override fun initArgs() {
        super.initArgs()
    }

    override fun onNetworkChanged(isConnected: Boolean) {
        isNetworkConnected = isConnected
        val otpMessage = getString(R.string.content_otp_text_view, arguments.email)
        binding.subtitle.animatedTextChange(
            if (isNetworkConnected) Html.fromHtml(
                otpMessage,
                Html.FROM_HTML_MODE_LEGACY
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
            findNavController().popBackStack()
        }
        binding.resent.setOnClickListener(this)
        val otpMessage = getString(R.string.content_otp_text_view, arguments.email)
        binding.subtitle.text = Html.fromHtml(otpMessage, Html.FROM_HTML_MODE_LEGACY)
        startCountdownTimer()
    }

    override fun initObserver() {
        viewModel.verifyOtpState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    //todo confirm otp
                }
            }
        }

        viewModel.reSentOtpState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    //todo fail resent
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    startCountdownTimer()
                    countResent++;
                }
            }
        }
    }

    override fun getData() {

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
                    RequestBody.create("application/x-protobuf".toMediaType(), byteArray)
                viewModel.reSentOtp(requestBody)
            }
        }
    }

    private fun startCountdownTimer() {
        countdownTimer = object : CountDownTimer(otpValidityDurationInMillis, 1000) {
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
        val text =
            "{\"email\" : \"${arguments.email}\" , \"otp\" : \"${otp}\" , \"type\" : \"${arguments.otptype}\"}"
        val textEntryPoint = Login.encryptData(text)

        val authRequest = AuthRequest(textEntryPoint)
        val byteArray = authRequest.encode()
        val requestBody =
            RequestBody.create("application/x-protobuf".toMediaType(), byteArray)
        viewModel.verifyOtp(requestBody)
    }

}