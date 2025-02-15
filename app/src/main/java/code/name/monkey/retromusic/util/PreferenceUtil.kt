package code.name.monkey.retromusic.util


import androidx.core.content.edit
import androidx.preference.PreferenceManager
import code.name.monkey.appthemehelper.util.VersionUtils
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.Constants.COLORED_APP_SHORTCUTS
import code.name.monkey.retromusic.Constants.WALLPAPER_ACCENT

object PreferenceUtil {
    private const val PREF_NAME = "MyPrefs"
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext())


    val wallpaperAccent
        get() = sharedPreferences.getBoolean(
            WALLPAPER_ACCENT,
            VersionUtils.hasOreoMR1() && !VersionUtils.hasS()
        )

    var isColoredAppShortcuts
        get() = sharedPreferences.getBoolean(
            COLORED_APP_SHORTCUTS, true
        )
        set(value) = sharedPreferences.edit {
            putBoolean(COLORED_APP_SHORTCUTS, value)
        }

    fun setDefaultPreferences() {
        val editor = sharedPreferences.edit()
        if (!sharedPreferences.contains(WALLPAPER_ACCENT)) {
            editor.putBoolean(WALLPAPER_ACCENT, VersionUtils.hasOreoMR1() && !VersionUtils.hasS())
        }
        if (!sharedPreferences.contains(COLORED_APP_SHORTCUTS)) {
            editor.putBoolean(COLORED_APP_SHORTCUTS, true)
        }
        editor.apply()
    }
}