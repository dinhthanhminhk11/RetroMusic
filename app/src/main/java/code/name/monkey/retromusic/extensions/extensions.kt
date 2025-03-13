@file:Suppress("UNUSED_PARAMETER", "unused")

package code.name.monkey.retromusic.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import code.name.monkey.retromusic.ACCOUNT_CAN_LOGIN
import code.name.monkey.retromusic.ACCOUNT_CAN_NOT_LOGIN
import code.name.monkey.retromusic.ACCOUNT_LOCKED
import code.name.monkey.retromusic.DATA_MISSING
import code.name.monkey.retromusic.DATA_NOT_DECRYPT
import code.name.monkey.retromusic.EMAIL_ALREADY_EXISTS
import code.name.monkey.retromusic.EMAIL_DOSE_NOT_EXISTS
import code.name.monkey.retromusic.EMAIL_MISSING
import code.name.monkey.retromusic.EMAIL_NOT_FORMAT
import code.name.monkey.retromusic.INVALID_PROTOBUF
import code.name.monkey.retromusic.LOGIN_ERROR
import code.name.monkey.retromusic.LOGIN_SUCCESS
import code.name.monkey.retromusic.LOGOUT_SUCCESS
import code.name.monkey.retromusic.OTP_CONFIRMED
import code.name.monkey.retromusic.OTP_EXPIRED
import code.name.monkey.retromusic.OTP_LIMIT
import code.name.monkey.retromusic.OTP_NOT_VALID
import code.name.monkey.retromusic.OTP_NOT_VERIFIED
import code.name.monkey.retromusic.OTP_RECENT_SUCCESS
import code.name.monkey.retromusic.OTP_SEND_FAIL
import code.name.monkey.retromusic.PASSWORD_NOT_SET
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.SERVER_ERROR
import code.name.monkey.retromusic.SET_PASS_SUCCESS
import code.name.monkey.retromusic.UPDATE_SUCCESS
import code.name.monkey.retromusic.USER_REGISTER_SUCCESS
import com.google.android.material.snackbar.Snackbar
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

fun View.showSnackBar(message: String, actionText: String? = null, action: (() -> Unit)? = null) {
    val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    if (actionText != null && action != null) {
        snackBar.setAction(actionText) { action() }
    }
    snackBar.show()
}

fun handErrorServerProtobuf(view: View, errorCode: String, action: ((String) -> Unit)? = null) {
    val message = when (errorCode) {
        INVALID_PROTOBUF -> R.string.invalid_protobuf_format
        DATA_MISSING -> R.string.data_misssing
        DATA_NOT_DECRYPT -> R.string.invalid_decrypted_data_fomat
        EMAIL_MISSING -> R.string.email_missing
        EMAIL_NOT_FORMAT -> R.string.email_not_format
        EMAIL_ALREADY_EXISTS -> R.string.email_already_exists
        OTP_SEND_FAIL -> R.string.otp_send_fail
        ACCOUNT_LOCKED -> R.string.account_locked
        OTP_LIMIT -> R.string.otp_limit
        EMAIL_DOSE_NOT_EXISTS -> R.string.email_dose_not_exits
        OTP_NOT_VALID -> R.string.otp_not_valid
        OTP_EXPIRED -> R.string.otp_expired
        SERVER_ERROR -> R.string.server_error
        PASSWORD_NOT_SET -> R.string.password_not_set
        LOGIN_ERROR -> R.string.login_error
        OTP_NOT_VERIFIED -> R.string.otp_not_verified
        ACCOUNT_CAN_NOT_LOGIN -> R.string.unverified_account_require_authentication

        else -> R.string.unknow_error
    }

    view.showSnackBar(message = view.context.getString(message))
    action?.invoke(errorCode)
}

fun showSuccessLoginProtobuf(view: View, successCode: String) {
    val message = when (successCode) {
        USER_REGISTER_SUCCESS -> R.string.user_register_success
        OTP_RECENT_SUCCESS -> R.string.otp_recent_success
        LOGIN_SUCCESS -> R.string.login_success
        OTP_CONFIRMED -> R.string.otp_confirmed
        ACCOUNT_CAN_LOGIN -> R.string.account_can_login
        ACCOUNT_CAN_NOT_LOGIN -> R.string.account_can_not_login
        SET_PASS_SUCCESS -> R.string.set_pass_success
        LOGOUT_SUCCESS -> R.string.logout_successs
        UPDATE_SUCCESS -> R.string.update_success
        else -> R.string.unknow_success
    }

    view.showSnackBar(message = view.context.getString(message))
}

fun hideKeyboard(context: Context, view: View?) {
    if (view != null) {
        val imm =
            context.getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun showConfirmDialog(
    context: Context,
    title: String,
    message: String,
    textPositiveButton: String,
    textNegativeButton: String,
    onConfirm: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val dialog = AlertDialog.Builder(context, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(textPositiveButton) { _, _ -> onConfirm() }
        .setNegativeButton(textNegativeButton) { dialog, _ ->
            onCancel?.invoke()
            dialog.dismiss()
        }
        .create()

    dialog.setOnShowListener {
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
    }

    dialog.show()
}


