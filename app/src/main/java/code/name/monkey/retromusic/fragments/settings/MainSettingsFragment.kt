package code.name.monkey.retromusic.fragments.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.retromusic.AuthRequest
import code.name.monkey.retromusic.Constants
import code.name.monkey.retromusic.LOGIN_SUCCESS
import code.name.monkey.retromusic.LOGOUT_SUCCESS
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.activities.auth.AuthActivity
import code.name.monkey.retromusic.databinding.FragmentMainSettingsBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.drawAboveSystemBarsWithPadding
import code.name.monkey.retromusic.extensions.goToProVersion
import code.name.monkey.retromusic.extensions.handErrorServerProtobuf
import code.name.monkey.retromusic.extensions.showConfirmDialog
import code.name.monkey.retromusic.extensions.showSuccessLoginProtobuf
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.PreferenceUtil.clearUser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainSettingsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentMainSettingsBinding? = null
    private val binding get() = _binding!!

    private val mainSettingsViewModel by viewModel<MainSettingsViewModel>()
    override fun onClick(view: View) {
        findNavController().navigate(
            when (view.id) {
                R.id.generalSettings -> R.id.action_mainSettingsFragment_to_themeSettingsFragment
                R.id.audioSettings -> R.id.action_mainSettingsFragment_to_audioSettings
                R.id.personalizeSettings -> R.id.action_mainSettingsFragment_to_personalizeSettingsFragment
                R.id.imageSettings -> R.id.action_mainSettingsFragment_to_imageSettingFragment
                R.id.notificationSettings -> R.id.action_mainSettingsFragment_to_notificationSettingsFragment
                R.id.otherSettings -> R.id.action_mainSettingsFragment_to_otherSettingsFragment
                R.id.nowPlayingSettings -> R.id.action_mainSettingsFragment_to_nowPlayingSettingsFragment
                else -> R.id.action_mainSettingsFragment_to_themeSettingsFragment
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.generalSettings.setOnClickListener(this)
        binding.audioSettings.setOnClickListener(this)
        binding.nowPlayingSettings.setOnClickListener(this)
        binding.personalizeSettings.setOnClickListener(this)
        binding.imageSettings.setOnClickListener(this)
        binding.notificationSettings.setOnClickListener(this)
        binding.otherSettings.setOnClickListener(this)
        binding.logout.setOnClickListener {
            showConfirmDialog(context = requireActivity(),
                title = getString(R.string.notification),
                message = getString(R.string.text_logout),
                textPositiveButton = getString(R.string.logout),
                textNegativeButton = getString(R.string.cancel),
                onConfirm = {
                    val textEncrypt =
                        "{\"token\" : \"${PreferenceUtil.userClient.accessToken.toString()}\"}"
                    val textEntryPoint = Login.encryptData(textEncrypt)

                    val authRequest = AuthRequest(textEntryPoint)
                    val byteArray = authRequest.encode()
                    val requestBody =
                        RequestBody.create("application/x-protobuf".toMediaType(), byteArray)
                    mainSettingsViewModel.logout(requestBody)
                }
            )
        }

        binding.buyProContainer.apply {
            isGone = PreferenceUtil.isProAccount
            setOnClickListener {
                requireContext().goToProVersion()
            }
        }
        binding.buyPremium.setOnClickListener {
            showConfirmDialog(context = requireActivity(),
                title = getString(R.string.notification),
                message = getString(R.string.text_primeum),
                textPositiveButton = getString(R.string.confirm),
                textNegativeButton = getString(R.string.cancel),
                onConfirm = {

                }
            )
        }
        ThemeStore.accentColor(requireContext()).let {
            binding.buyPremium.setTextColor(it)
            binding.diamondIcon.imageTintList = ColorStateList.valueOf(it)
        }

        binding.container.drawAboveSystemBarsWithPadding()
        initObserver()
    }

    private fun initObserver() {
        mainSettingsViewModel.authState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {

                }

                is Result.Error -> {
                    result.code?.let {
                        handErrorServerProtobuf(binding.root, it)
                    }
                }

                is Result.Success -> {
                    if (Constants.ON_OFF_SETTING_TOAST_SUCCESS) {
                        result.data.data_.code.let {
                            showSuccessLoginProtobuf(binding.root, it)
                        }
                    }

                    result.data.let {
                        if (result.data.success) {
                            result.data.data_.let {
                                when (result.data.data_.code) {
                                    LOGOUT_SUCCESS -> {
                                        showSuccessLoginProtobuf(binding.root, LOGIN_SUCCESS)
                                        clearUser()
                                        PreferenceUtil.userClient
                                        val intent =
                                            Intent(requireContext(), AuthActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
