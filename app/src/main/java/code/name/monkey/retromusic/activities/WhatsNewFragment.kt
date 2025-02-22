package code.name.monkey.retromusic.activities

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.FragmentActivity
import code.name.monkey.retromusic.BuildConfig
import code.name.monkey.retromusic.util.PreferenceUtil.lastVersion
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class WhatsNewFragment : BottomSheetDialogFragment() {
    companion object {

        const val TAG = "WhatsNewFragment"
        private fun colorToCSS(color: Int): String {
            return String.format(
                Locale.getDefault(),
                "rgba(%d, %d, %d, %d)",
                Color.red(color),
                Color.green(color),
                Color.blue(color),
                Color.alpha(color)
            ) // on API 29, WebView doesn't load with hex colors
        }

        private fun setChangelogRead(context: Context) {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersion = PackageInfoCompat.getLongVersionCode(pInfo)
                lastVersion = currentVersion
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

        fun showChangeLog(activity: FragmentActivity) {
            val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            val currentVersion = PackageInfoCompat.getLongVersionCode(pInfo)
            if (currentVersion > lastVersion && !BuildConfig.DEBUG) {
                val changelogBottomSheet = WhatsNewFragment()
                changelogBottomSheet.show(activity.supportFragmentManager, TAG)
            }
        }
    }
}