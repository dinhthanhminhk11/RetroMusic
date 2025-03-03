@file:Suppress("UNUSED_PARAMETER", "unused")

package code.name.monkey.retromusic.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson

fun Context.setUpMediaRouteButton(menu: Menu) {}

fun FragmentActivity.installLanguageAndRecreate(code: String, onInstallComplete: () -> Unit) {
    onInstallComplete()
}

fun Context.goToProVersion() {}

fun Context.installSplitCompat() {}

fun Any?.toJson(): String = if (this == null) "null" else Gson().toJson(this)

fun validateEmail(email: String): Boolean {
    if (email.isEmpty()) {
        return false
    }
    val emailPattern = android.util.Patterns.EMAIL_ADDRESS
    return emailPattern.matcher(email).matches()
}

fun MaterialTextView.animatedTextChange(newText: CharSequence, duration: Long = 200) {
    if (this.text == newText) return

    this.animate()
        .translationY(-5f)
        .alpha(0f) // Mờ dần
        .setDuration(duration)
        .withEndAction {
            this.text = newText
            this.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .start()
        }.start()
}

fun View.fadeVisibility(isVisible: Boolean, duration: Long = 200) {
    if (isVisible) {
        this.apply {
            alpha = 0f
            translationY = 10f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(duration)
                .setListener(null)
                .start()
        }
    } else {
        this.animate()
            .alpha(0f)
            .translationY(10f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                }
            })
            .start()
    }
}

