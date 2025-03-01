@file:Suppress("UNUSED_PARAMETER", "unused")

package code.name.monkey.retromusic.extensions

import android.content.Context
import android.view.Menu
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson

fun Context.setUpMediaRouteButton(menu: Menu) {}

fun FragmentActivity.installLanguageAndRecreate(code: String, onInstallComplete: () -> Unit) {
    onInstallComplete()
}

fun Context.goToProVersion() {}

fun Context.installSplitCompat() {}

fun Any?.toJson(): String = if (this == null) "null" else Gson().toJson(this)