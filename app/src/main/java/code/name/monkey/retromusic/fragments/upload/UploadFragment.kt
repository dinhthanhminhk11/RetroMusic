package code.name.monkey.retromusic.fragments.upload

import android.view.View
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentUploadBinding
import code.name.monkey.retromusic.extensions.showConfirmDialog
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment


class UploadFragment : BaseNormalFragment<FragmentUploadBinding>(FragmentUploadBinding::inflate) {
    override fun onNetworkChanged() {

    }

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            showConfirmDialog(context = requireActivity(),
                title = getString(R.string.notification),
                message = getString(R.string.text_confirm_otp),
                textPositiveButton = getString(R.string.out),
                textNegativeButton = getString(R.string.cancel),
                onConfirm = {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            )
        }
    }

    override fun initObserver() {

    }

    override fun getData() {

    }

    override fun onViewClicked(view: View?) {
    }

}