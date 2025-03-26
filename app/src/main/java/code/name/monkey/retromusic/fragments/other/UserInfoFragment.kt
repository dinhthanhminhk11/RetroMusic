package code.name.monkey.retromusic.fragments.other

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import code.name.monkey.retromusic.Constants
import code.name.monkey.retromusic.Constants.USER_BANNER
import code.name.monkey.retromusic.Constants.USER_PROFILE
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.UPDATE_SUCCESS
import code.name.monkey.retromusic.databinding.FragmentUserInfoBinding
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.extensions.accentColor
import code.name.monkey.retromusic.extensions.applyToolbar
import code.name.monkey.retromusic.extensions.handErrorServerProtobuf
import code.name.monkey.retromusic.extensions.loadImageAvatar
import code.name.monkey.retromusic.extensions.showSuccessLoginProtobuf
import code.name.monkey.retromusic.extensions.showToast
import code.name.monkey.retromusic.fragments.LibraryViewModel
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.model.auth.UserClient
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.util.ImageUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.PreferenceUtil.userClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class UserInfoFragment :
    BaseNormalFragment<FragmentUserInfoBinding>(FragmentUserInfoBinding::inflate) {
    private val libraryViewModel: LibraryViewModel by activityViewModel()
    private var imagePath: Uri? = null
    private var imagePathBanner: Uri? = null
    private val viewModel by viewModel<UserInfoViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            duration = 300L
            scrimColor = Color.TRANSPARENT
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onNetworkChanged() {
        binding.toolbar.let {
            it.title =
                if (isNetworkConnected) getString(R.string.profile) else getString(R.string.disconnect_internet)
        }
    }

    override fun initView() {
        applyToolbar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.email?.isEnabled = false
        binding.next.visibility = GONE

        binding.nameContainer.accentColor()
        binding.emailContainer?.accentColor()
        binding.phoneCOntainer?.accentColor()
        binding.next.accentColor()
        binding.name.setText(if (userClient.fullName.isNullOrEmpty()) "" else userClient.fullName)
        binding.email?.setText(if (userClient.email.isNullOrEmpty()) "" else userClient.email)
        binding.phone?.setText(if (userClient.phone.isNullOrEmpty()) "" else userClient.phone)
        binding.userImage.setOnClickListener(this)

        binding.bannerImage.setOnClickListener(this)

        binding.next.setOnClickListener(this)

        loadProfile()
        postponeEnterTransition()
        view?.doOnPreDraw {
            startPostponedEnterTransition()
        }
        libraryViewModel.getFabMargin().observe(viewLifecycleOwner) {
            binding.next.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = it
            }
        }
        setupInputListeners()
    }


    override fun getData() {

    }

    override fun onViewClicked(view: View?) {
        when (view) {
            binding.next -> {
                val dataMap = mutableMapOf<String, String>()

                binding.name.text?.toString()?.takeIf { it.isNotBlank() }?.let {
                    dataMap["fullName"] = it
                }

                binding.phone?.text?.toString()?.takeIf { it.isNotBlank() }?.let {
                    dataMap["phone"] = it
                }

                val textEncrypt = JSONObject(dataMap as Map<*, *>?).toString()

                val dataRequest: RequestBody = RequestBody.create(
                    "text/plain".toMediaTypeOrNull(), Login.encryptData(textEncrypt)
                )
                val imageFilePart: MultipartBody.Part? = if (imagePath != null) {
                    val imageFile = File(imagePath?.path.toString())
                    val imageRequestBody =
                        RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                    MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
                } else {
                    null
                }

                val imageBannerFilePart: MultipartBody.Part? = if (imagePathBanner != null) {
                    val imageBannerFile = File(imagePathBanner?.path.toString())
                    val imageBannerRequestBody = RequestBody.create(
                        "image/*".toMediaTypeOrNull(),
                        imageBannerFile
                    )
                    MultipartBody.Part.createFormData(
                        "imageBanner",
                        imageBannerFile.name,
                        imageBannerRequestBody
                    )
                } else {
                    null
                }
                viewModel.updateUserInfo(
                    Login.encryptData(userClient.accessToken.toString()),
                    dataRequest,
                    imageFilePart,
                    imageBannerFilePart
                )
            }

            binding.bannerImage -> showBannerImageOptions()
            binding.userImage -> showUserImageOptions()
        }
    }

    override fun initObserver() {
        viewModel.authState.observe(viewLifecycleOwner) { result ->
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
                        result.data.data.code.let {
                            showSuccessLoginProtobuf(binding.root, it)
                        }
                    }

                    result.data.let {
                        if (result.data.success) {
                            result.data.data.let {
                                when (result.data.data.code) {
                                    UPDATE_SUCCESS -> {
                                        showSuccessLoginProtobuf(binding.root, UPDATE_SUCCESS)
                                        result.data.data.details.let { dataLogin ->
                                            val gson = Gson()
                                            val userClient: UserClient =
                                                gson.fromJson(
                                                    Login.decryptData(dataLogin),
                                                    UserClient::class.java
                                                )
                                            PreferenceUtil.userClient = userClient
                                        }
                                        findNavController().navigateUp()
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun showBannerImageOptions() {
        val list = requireContext().resources.getStringArray(R.array.image_settings_options)
        MaterialAlertDialogBuilder(requireContext()).setTitle("Banner Image")
            .setItems(list) { _, which ->
                when (which) {
                    0 -> selectBannerImage()
                    1 -> {
                        val appDir = requireContext().filesDir
                        val file = File(appDir, USER_BANNER)
                        file.delete()
                        loadProfile()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .create()
            .show()
    }

    private fun showUserImageOptions() {
        val list = requireContext().resources.getStringArray(R.array.image_settings_options)
        MaterialAlertDialogBuilder(requireContext()).setTitle("Profile Image")
            .setItems(list) { _, which ->
                when (which) {
                    0 -> pickNewPhoto()
                    1 -> {
                        val appDir = requireContext().filesDir
                        val file = File(appDir, USER_PROFILE)
                        file.delete()
                        loadProfile()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .create()
            .show()
    }

    private fun loadProfile() {
        loadImageAvatar(requireActivity(), userClient.imageBanner, binding.bannerImage)
        loadImageAvatar(requireActivity(), userClient.image, binding.userImage)
    }

    private fun selectBannerImage() {
        ImagePicker.with(this)
            .compress(1440)
            .provider(ImageProvider.GALLERY)
            .crop(16f, 9f)
            .createIntent {
                startForBannerImageResult.launch(it)
            }
    }

    private fun pickNewPhoto() {
        ImagePicker.with(this)
            .provider(ImageProvider.GALLERY)
            .cropSquare()
            .compress(1440)
            .createIntent {
                startForProfileImageResult.launch(it)
            }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            saveImage(result) { fileUri ->
                setAndSaveUserImage(fileUri)
            }
        }

    private val startForBannerImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            saveImage(result) { fileUri ->
                setAndSaveBannerImage(fileUri)
            }
        }

    private fun saveImage(result: ActivityResult, doIfResultOk: (uri: Uri) -> Unit) {
        val resultCode = result.resultCode
        val data = result.data
        when (resultCode) {
            Activity.RESULT_OK -> {
                data?.data?.let { uri ->
                    doIfResultOk(uri)
                }
            }

            ImagePicker.RESULT_ERROR -> {
                showToast(ImagePicker.getError(data))
            }

            else -> {
                showToast("Task Cancelled")
            }
        }
    }

    private fun setAndSaveBannerImage(fileUri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(fileUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(object : RequestListener<Bitmap> {
                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    resource?.let { imagePathBanner = fileUri }
                    checkEnableNextButton()
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    return false
                }
            })
            .into(binding.bannerImage)
    }

    private fun saveImage(bitmap: Bitmap, fileName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val appDir = requireContext().filesDir
            val file = File(appDir, fileName)
            var successful: Boolean
            file.outputStream().buffered().use {
                successful = ImageUtil.resizeBitmap(bitmap, 2048)
                    .compress(Bitmap.CompressFormat.WEBP, 100, it)
            }
            if (successful) {
                withContext(Dispatchers.Main) {
                    showToast(R.string.message_updated)
                }
            }
        }
    }

    private fun setAndSaveUserImage(fileUri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(fileUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(object : RequestListener<Bitmap> {
                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    resource?.let {
                        imagePath = fileUri
                    }
                    checkEnableNextButton()
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    return false
                }
            })
            .into(binding.userImage)
    }

    private fun setupInputListeners() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkEnableNextButton()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.name.addTextChangedListener(textWatcher)
        binding.phone?.addTextChangedListener(textWatcher)
    }

    private fun checkEnableNextButton() {
        val isNameChanged =
            !binding.name.text.isNullOrBlank() && binding.name.text.toString() != userClient.fullName
        val isPhoneChanged =
            !binding.phone?.text.isNullOrBlank() && binding.phone?.text.toString() != userClient.phone
        val isImageChanged = imagePath != null
        val isBannerChanged = imagePathBanner != null

        binding.next.visibility =
            if (isNameChanged || isPhoneChanged || isImageChanged || isBannerChanged) VISIBLE else GONE

    }
}
