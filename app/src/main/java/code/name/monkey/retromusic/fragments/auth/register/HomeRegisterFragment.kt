package code.name.monkey.retromusic.fragments.auth.register

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.parseAsHtml
import androidx.navigation.fragment.findNavController
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentHomeRegisterBinding
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.util.ViewUtil
import com.google.android.material.snackbar.Snackbar


class HomeRegisterFragment :
    BaseNormalFragment<FragmentHomeRegisterBinding>(FragmentHomeRegisterBinding::inflate) {
    override fun onNetworkChanged() {

    }

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        setupTitle()
    }

    override fun initObserver() {

    }

    override fun getData() {

    }

    override fun onViewClicked(view: View?) {
        when (view) {
            binding.layoutButtonSplash.register -> {
                findNavController().navigate(
                    R.id.registerFragment,
                    null,
                    ViewUtil.navOptionsByMinh
                )
            }

            binding.layoutButtonSplash.login -> {
                Snackbar.make(
                    binding.root,
                    getString(R.string.message_develop),
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            binding.loginTextView -> {
                findNavController().navigate(
                    R.id.loginFragment, null,
                    ViewUtil.navOptionsByMinh
                )
            }
        }
    }

    private fun setupTitle() {
        val appName =
            getString(
                R.string.message_welcome_splash_register
            )
                .parseAsHtml()
        binding.layoutLogoSplash.title.text = appName

        binding.layoutButtonSplash.login.text = getString(R.string.phone_button_splash)
        binding.layoutButtonSplash.register.text = getString(R.string.email_button_splash)
        binding.layoutButtonSplash.register.icon =
            ContextCompat.getDrawable(requireContext(), R.drawable.round_mail_outline_24)
        binding.layoutButtonSplash.login.icon =
            ContextCompat.getDrawable(requireContext(), R.drawable.round_phone_iphone_24)

        binding.layoutButtonSplash.login.setOnClickListener(this)
        binding.layoutButtonSplash.register.setOnClickListener(this)
        binding.loginTextView.setOnClickListener(this)
    }

}