package code.name.monkey.retromusic.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.MaterialValueHelper
import code.name.monkey.retromusic.R
import com.airbnb.lottie.LottieAnimationView

object ViewUtil {

    const val RETRO_MUSIC_ANIM_TIME = 1000

    fun setProgressDrawable(progressSlider: SeekBar, newColor: Int, thumbTint: Boolean = false) {

        if (thumbTint) {
            progressSlider.thumbTintList = ColorStateList.valueOf(newColor)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val layerDrawable = progressSlider.progressDrawable as LayerDrawable
            val progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress)
            progressDrawable.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(newColor, SRC_IN)
        } else {
            progressSlider.progressTintList = ColorStateList.valueOf(newColor)
        }
    }


    fun setProgressDrawable(progressSlider: ProgressBar, newColor: Int) {

        val layerDrawable = progressSlider.progressDrawable as LayerDrawable

        val progress = layerDrawable.findDrawableByLayerId(android.R.id.progress)
        progress.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(newColor, SRC_IN)

        val background = layerDrawable.findDrawableByLayerId(android.R.id.background)
        val primaryColor =
            ATHUtil.resolveColor(progressSlider.context, android.R.attr.windowBackground)
        background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            MaterialValueHelper.getPrimaryDisabledTextColor(
                progressSlider.context,
                ColorUtil.isColorLight(primaryColor)
            ), SRC_IN
        )

        val secondaryProgress = layerDrawable.findDrawableByLayerId(android.R.id.secondaryProgress)
        secondaryProgress?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                ColorUtil.withAlpha(
                    newColor,
                    0.65f
                ), SRC_IN
            )
    }

    fun hitTest(v: View, x: Int, y: Int): Boolean {
        val tx = (v.translationX + 0.5f).toInt()
        val ty = (v.translationY + 0.5f).toInt()
        val left = v.left + tx
        val right = v.right + tx
        val top = v.top + ty
        val bottom = v.bottom + ty

        return x in left..right && y >= top && y <= bottom
    }

    fun convertDpToPixel(dp: Float, resources: Resources): Float {
        val metrics = resources.displayMetrics
        return dp * metrics.density
    }

    fun animateEmptyVisibility(
        view: View,
        show: Boolean,
        progressView: View? = null,
        iconEmpty: LottieAnimationView
    ) {
        if (show) {
            if (view.visibility != View.VISIBLE) {
                view.animate().cancel()
                view.alpha = 0f
                view.scaleX = 0.8f
                view.scaleY = 0.8f
                view.visibility = View.VISIBLE
                view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()

                progressView?.apply {
                    visibility = View.VISIBLE
                    alpha = 1f
                }
                if (!iconEmpty.isAnimating) {
                    iconEmpty.playAnimation()
                }
            }
        } else {
            if (view.visibility == View.VISIBLE) {
                view.animate().cancel()
                view.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(150)
                    .withEndAction { view.visibility = View.GONE }
                    .start()

                progressView?.animate()?.apply {
                    setListener(null)
                    cancel()
                    setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            progressView.visibility = View.GONE
                        }
                    })
                    alpha(0f).setDuration(150).start()
                }
            }
        }
    }

    fun createNavOptions(replace: Boolean, popUpToId: Int? = null): NavOptions {
        return navOptions {
            launchSingleTop = false
            anim {
                enter = R.anim.retro_fragment_open_enter
                exit = R.anim.retro_fragment_open_exit
                popEnter = R.anim.retro_fragment_close_enter
                popExit = R.anim.retro_fragment_close_exit
            }
            if (replace && popUpToId != null) {
                popUpTo(popUpToId) { inclusive = true }
            }
        }
    }

    val navOptions by lazy {
        navOptions {
            launchSingleTop = false
            anim {
                enter = R.anim.retro_fragment_open_enter
                exit = R.anim.retro_fragment_open_exit
                popEnter = R.anim.retro_fragment_close_enter
                popExit = R.anim.retro_fragment_close_exit
            }
        }
    }

    val navOptionsByMinh by lazy {
        navOptions {
            launchSingleTop = false
            anim {
                enter = R.anim.fragment_slide_left_enter
                exit = R.anim.fragment_slide_left_exit
                popEnter = R.anim.fragment_slide_right_enter
                popExit = R.anim.fragment_slide_right_exit
            }
        }
    }
}