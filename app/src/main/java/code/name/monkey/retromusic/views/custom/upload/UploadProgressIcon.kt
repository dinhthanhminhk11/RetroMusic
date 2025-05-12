package code.name.monkey.retromusic.views.custom.upload

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.extensions.addObserverExt
import code.name.monkey.retromusic.extensions.removeObserverExt
import code.name.monkey.retromusic.util.EventCenter
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.S)
class UploadProgressIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), EventCenter.EventCenterDelegate {

    private var currentColor: Int = ThemeStore.accentColor(context)
    private val iconUpload: LottieAnimationView
    private val progressBar: LinearProgressBar

    init {
        inflate(context, R.layout.view_upload_progress_icon, this)
        iconUpload = findViewById(R.id.iconUpload)
        progressBar = findViewById(R.id.progressBar)

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

    private fun stateClick(allow: Boolean) {
        if (allow != isClickable) {
            isClickable = allow
            isFocusable = allow
        }
    }

    fun startUpload() {
        if (iconUpload.isAnimating) {
            iconUpload.cancelAnimation()
        }
        setAnimationWithAutoColor(R.raw.download_progress)
        iconUpload.repeatCount = ValueAnimator.INFINITE
        iconUpload.playAnimation()
        progressBar.visibility = VISIBLE
        progressBar.setProgress(0f)
        Timber.tag("MinhProgressIcon").d("Started upload animation")
    }

    fun onUploadComplete() {
        if (iconUpload.isAnimating) {
            iconUpload.cancelAnimation()
        }
        setAnimationWithAutoColor(R.raw.download_finish)
        iconUpload.repeatCount = 0
        iconUpload.playAnimation()
        progressBar.setProgress(100f)
        Timber.tag("MinhProgressIcon").d("Completed upload animation")
    }

    fun setAnimationWithAutoColor(animationRes: Int) {
        iconUpload.setAnimation(animationRes)
        iconUpload.addValueCallback(
            KeyPath("**"),
            LottieProperty.COLOR_FILTER,
            LottieValueCallback(SimpleColorFilter(currentColor))
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventCenter.getInstance(0)
            .addObserverExt(this, EventCenter.EventType.UPLOAD_PROGRESS_ACTION)
        EventCenter.getInstance(0)
            .addObserverExt(this, EventCenter.EventType.UPLOAD_ACTION_FAILED)
        EventCenter.getInstance(0)
            .addObserverExt(this, EventCenter.EventType.UPLOAD_START_UPLOAD_PROGRESS)

        val allowed = intArrayOf(
            EventCenter.EventType.UPLOAD_PROGRESS_ACTION.ordinal,
            EventCenter.EventType.UPLOAD_ACTION_FAILED.ordinal,
            EventCenter.EventType.UPLOAD_START_UPLOAD_PROGRESS.ordinal
        )
        EventCenter.getInstance(0).setAnimationInProgress(0, allowed)
        Timber.tag("MinhProgressIcon").d("Observer registered for $this")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventCenter.getInstance(0)
            .removeObserverExt(this, EventCenter.EventType.UPLOAD_PROGRESS_ACTION)
        EventCenter.getInstance(0)
            .removeObserverExt(this, EventCenter.EventType.UPLOAD_ACTION_FAILED)
        EventCenter.getInstance(0)
            .removeObserverExt(this, EventCenter.EventType.UPLOAD_START_UPLOAD_PROGRESS)
        EventCenter.getInstance(0).onAnimationFinish(0)
        Timber.tag("MinhProgressIcon").d("Observer unregistered for $this")
    }

    override fun didReceivedNotification(id: Int, account: Int, vararg args: Any?) {
        Timber.tag("didReceivedNotification")
            .d("UploadProgressIcon Received on thread: ${Thread.currentThread().name}, id=$id, args=${args.contentToString()}")
        if (!isAttachedToWindow) return
        when (id) {
            EventCenter.EventType.UPLOAD_PROGRESS_ACTION.ordinal -> {
                stateClick(false)
                val progress = args.getOrNull(0) as? Int ?: run {
                    Timber.tag("MinhProgressIcon")
                        .e("Invalid progress value: ${args.contentToString()}")
                    return
                }
                Timber.tag("MinhProgressIcon").d("Upload progress: $progress")
                progressBar.setProgress(progress.toFloat())

                if (progress in 1 until 100) {
                    if (!iconUpload.isAnimating) {
                        setAnimationWithAutoColor(R.raw.download_progress)
                        iconUpload.repeatCount = ValueAnimator.INFINITE
                        iconUpload.playAnimation()
                    }
                } else if (progress >= 100) {
                    onUploadComplete()
                    stateClick(true)
                }
            }

            EventCenter.EventType.UPLOAD_ACTION_FAILED.ordinal -> {
                Timber.tag("MinhProgressIcon").w("Upload failed")
                onUploadComplete()
                stateClick(true)
            }

            EventCenter.EventType.UPLOAD_START_UPLOAD_PROGRESS.ordinal -> {
                Timber.tag("MinhProgressIcon").i("Upload started")
                stateClick(false)
                startUpload()
            }
        }
    }
}
