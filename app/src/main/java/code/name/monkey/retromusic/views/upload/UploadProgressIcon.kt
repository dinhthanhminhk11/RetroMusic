package code.name.monkey.retromusic.views.upload

import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.UPLOAD_ACTION_FAILED
import code.name.monkey.retromusic.UPLOAD_PROGRESS
import code.name.monkey.retromusic.UPLOAD_PROGRESS_ACTION
import code.name.monkey.retromusic.UPLOAD_START_UPLOAD_PROGRESS
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

@RequiresApi(Build.VERSION_CODES.S)
class UploadProgressIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var currentColor: Int
    private var iconUpload: LottieAnimationView
    private var progressBar: LinearProgressBar

    init {
        inflate(context, R.layout.view_upload_progress_icon, this)
        iconUpload = findViewById(R.id.iconUpload)
        progressBar = findViewById(R.id.progressBar)

        currentColor = ThemeStore.accentColor(context)

        iconUpload.scaleY = -1f
        setAnimationWithAutoColor(R.raw.download_finish)
        iconUpload.progress = 1f
        progressBar.setProgress(100f)

        background = RippleDrawable(
            context.obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.colorControlHighlight))
                .use {
                    it.getColorStateList(0)
                } as ColorStateList,
            null,
            MaterialShapeDrawable(ShapeAppearanceModel.builder().setAllCornerSizes(50f).build())
        )

    }

    fun startUpload() {
        setAnimationWithAutoColor(R.raw.download_progress)
        iconUpload.repeatCount = ValueAnimator.INFINITE
        iconUpload.playAnimation()
        progressBar.visibility = VISIBLE
        progressBar.setProgress(0f)
    }

    fun onUploadComplete() {
        setAnimationWithAutoColor(R.raw.download_finish)
        iconUpload.repeatCount = 0
        iconUpload.playAnimation()
        progressBar.setProgress(100f)
    }

    private fun stateClick(allow: Boolean) {
        if (allow != isClickable) {
            isClickable = allow
            isFocusable = allow
        }
    }

    val uploadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                UPLOAD_PROGRESS_ACTION -> {
                    stateClick(false)
                    val progress = intent.getIntExtra(UPLOAD_PROGRESS, 0)
                    progressBar.setProgress(progress.toFloat())

                    if (progress > 0 && !iconUpload.isAnimating) {
                        Log.e("MinhProgressIcon", "iconUpload.isAnimating ");
                        setAnimationWithAutoColor(R.raw.download_progress)
                        iconUpload.playAnimation()
                    }

                    if (progress >= 100) {
                        onUploadComplete()
                        stateClick(true)
                    }
                }

                UPLOAD_ACTION_FAILED -> {
                    onUploadComplete()
                    stateClick(true)
                }

                UPLOAD_START_UPLOAD_PROGRESS -> {
                    stateClick(false)
                    startUpload()
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val intentFilter = IntentFilter().apply {
            addAction(UPLOAD_PROGRESS_ACTION)
            addAction(UPLOAD_ACTION_FAILED)
            addAction(UPLOAD_START_UPLOAD_PROGRESS)
        }


        ContextCompat.registerReceiver(
            context,
            uploadReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.unregisterReceiver(uploadReceiver)
    }

    fun setAnimationWithAutoColor(animationRes: Int) {
        iconUpload.setAnimation(animationRes)
        iconUpload.addValueCallback(
            KeyPath("**"),
            LottieProperty.COLOR_FILTER,
            LottieValueCallback(SimpleColorFilter(currentColor))
        )
    }
}


