package code.name.monkey.retromusic.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import java.util.Locale

object Utility {
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context?): String {
        return Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getUserAgent(appName: String, appVersion: String): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE

        return "$appName/$appVersion ($manufacturer; $model) Android/$androidVersion"
    }

    fun getAndroidVersion(): String {
        val sdkInt = Build.VERSION.SDK_INT
        return Build.VERSION.RELEASE
    }

    fun getCountryCode(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simCountry = telephonyManager.simCountryIso
        if (!simCountry.isNullOrEmpty()) {
            return simCountry.uppercase(Locale.getDefault())
        }
        return Locale.getDefault().country.uppercase(Locale.getDefault())
    }

    fun getLanguageCode(): String {
        return Locale.getDefault().language
    }

}