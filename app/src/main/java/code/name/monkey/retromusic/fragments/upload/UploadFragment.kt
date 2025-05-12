package code.name.monkey.retromusic.fragments.upload

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import code.name.monkey.retromusic.FILE_HASH
import code.name.monkey.retromusic.FILE_NAME
import code.name.monkey.retromusic.FILE_SIZE
import code.name.monkey.retromusic.FILE_URI
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.UPLOAD_CHUNKS
import code.name.monkey.retromusic.databinding.FragmentUploadBinding
import code.name.monkey.retromusic.extensions.addObserverExt
import code.name.monkey.retromusic.extensions.removeObserverExt
import code.name.monkey.retromusic.extensions.showConfirmDialog
import code.name.monkey.retromusic.extensions.showToast
import code.name.monkey.retromusic.fragments.base.BaseNormalFragment
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.service.upload.UploadService
import code.name.monkey.retromusic.util.EventCenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest


class UploadFragment : BaseNormalFragment<FragmentUploadBinding>(FragmentUploadBinding::inflate),
    EventCenter.EventCenterDelegate {
    private var imagePath: Uri? = null
    private var fileHash: String? = null
    private var fileName: String? = null
    private var file: File? = null

    private val viewModel by viewModel<UploadViewModel>()

    override fun onNetworkChanged() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            showConfirmDialog(
                context = requireActivity(),
                title = getString(R.string.notification),
                message = getString(R.string.text_confirm_otp),
                textPositiveButton = getString(R.string.out),
                textNegativeButton = getString(R.string.cancel),
                onConfirm = {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            )
        }

        binding.upload.setOnClickListener(this)
        binding.albumCoverContainer.setOnClickListener(this)
        binding.choseFile.setOnClickListener(this)
    }

    override fun initObserver() {
        viewModel.checkFileState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                }

                is Result.Success -> {
                    if (result.data.exists == true) {
                        showToast("FILE exist")
                    } else {
                        startUploadService(
                            fileHash!!,
                            file!!,
                            fileName!!,
                            result.data.uploadedChunks
                        )
                    }
                }

                is Result.Error -> {

                }

                else -> {}
            }
        }

    }

    override fun getData() {

    }

    override fun onViewClicked(view: View?) {
        when (view) {
            binding.albumCoverContainer -> {
                pickNewPhoto()
            }

            binding.upload -> {
                fileHash?.let {
                    viewModel.checkFile(it)
                }
            }

            binding.choseFile -> {
                selectAudioFile()
            }
        }
    }

    private fun pickNewPhoto() {
        ImagePicker.with(this)
            .provider(ImageProvider.GALLERY)
            .cropSquare()
            .compress(1440)
            .createIntent {
                startUpdateImageUploadImageResult.launch(it)
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
            .into(binding.image)
    }

    private fun checkEnableNextButton() {

    }

    private val startUpdateImageUploadImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            saveImage(result) { fileUri ->
                setAndSaveUserImage(fileUri)
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


    private val pickAudioLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                handleAudioFile(it)
            }
        }

    private fun selectAudioFile() {// chose file
        pickAudioLauncher.launch("video/*")
    }

    private fun handleAudioFile(uri: Uri) {
        file = copyFileFromUri(requireContext(), uri)
        fileName = getFileName(requireContext(), uri)

        file?.let {
            fileHash = getFileHash(it)
        }
        val albumArt = getAlbumArt(requireContext(), uri)
        binding.nameFile.text = Editable.Factory.getInstance().newEditable(fileName)
        albumArt?.let {
            binding.choseFile.setImageBitmap(it)
        }

    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "Unknown"
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    private fun getAlbumArt(context: Context, uri: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val embeddedArt: ByteArray? = retriever.embeddedPicture
            retriever.release()
            embeddedArt?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun copyFileFromUri(context: Context, uri: Uri): File? {
        val fileName = getFileName(context, uri)
        val file = File(context.cacheDir, fileName)

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return file
    }

    private fun getFileHash(file: File, algorithm: String = "SHA-256"): String {
        val digest = MessageDigest.getInstance(algorithm)
        FileInputStream(file).use { inputStream ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }


    private fun startUploadService(
        fileHash: String,
        file: File,
        fileName: String,
        listUploaded: ArrayList<Int>
    ) {
        val intent = Intent(requireContext(), UploadService::class.java).apply {
            putExtra(FILE_URI, file.absolutePath)
            putExtra(FILE_HASH, fileHash)
            putExtra(FILE_NAME, fileName)
            putExtra(FILE_SIZE, file.length() / 1024)
            putIntegerArrayListExtra(UPLOAD_CHUNKS, listUploaded)
        }
        requireContext().startService(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventCenter.getInstance(0)
            .addObserverExt(this, EventCenter.EventType.UPLOAD_PROGRESS_ACTION)
        EventCenter.getInstance(0)
            .addObserverExt(this, EventCenter.EventType.UPLOAD_ACTION_FAILED)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventCenter.getInstance(0)
            .removeObserverExt(this, EventCenter.EventType.UPLOAD_PROGRESS_ACTION)
        EventCenter.getInstance(0)
            .removeObserverExt(this, EventCenter.EventType.UPLOAD_ACTION_FAILED)
    }

    @SuppressLint("SetTextI18n")
    override fun didReceivedNotification(id: Int, account: Int, vararg args: Any?) {
        Timber.tag("didReceivedNotification")
            .d("UploadFragment Received on thread: ${Thread.currentThread().name}, id=$id, args=${args.contentToString()}")
        if (!isAdded) return

        when (id) {
            EventCenter.EventType.UPLOAD_PROGRESS_ACTION.ordinal -> {
                val data = args[0] as Int
                binding.genre.setText("$data %")
            }

            EventCenter.EventType.UPLOAD_ACTION_FAILED.ordinal -> {
                showToast("Upload Failed", Toast.LENGTH_SHORT)
            }
        }
    }
}