package code.name.monkey.retromusic.fragments.auth

import android.view.View
import androidx.core.text.parseAsHtml
import androidx.navigation.fragment.findNavController
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentSplashBinding
import code.name.monkey.retromusic.extensions.accentColor
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.util.ViewUtil


class SplashFragment : BaseNormalFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {


    override fun initView() {
        setupTitle()
    }

    override fun initObserver() {

    }

    override fun getData() {

    }

    override fun onViewClicked(view: View?) {
        when (view) {
            binding.layoutButtonSplash.login -> {
                findNavController().navigate(
                    R.id.homeLoginFragment,
                    null,
                    ViewUtil.navOptionsByMinh
                )
            }

            binding.layoutButtonSplash.register -> {
                findNavController().navigate(
                    R.id.homeRegisterFragment,
                    null,
                    ViewUtil.navOptionsByMinh
                )
            }
        }
    }

    private fun setupTitle() {
        val color = accentColor()
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        val appName =
            getString(
                R.string.message_welcome_splash,
                "<b>Retro <span  style='color:$hexColor';>Music</span></b>"
            )
                .parseAsHtml()
        binding.layoutLogoSplash.title.text = appName

        binding.layoutButtonSplash.login.text = getString(R.string.login_splash)
        binding.layoutButtonSplash.register.text = getString(R.string.register_splash)

        binding.layoutButtonSplash.login.setOnClickListener(this)
        binding.layoutButtonSplash.register.setOnClickListener(this)
    }


}