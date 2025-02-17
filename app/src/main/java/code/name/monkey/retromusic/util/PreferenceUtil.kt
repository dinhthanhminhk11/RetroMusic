package code.name.monkey.retromusic.util


import androidx.core.content.edit
import androidx.preference.PreferenceManager
import code.name.monkey.appthemehelper.util.VersionUtils
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.Constants.BLACK_THEME
import code.name.monkey.retromusic.Constants.COLORED_APP_SHORTCUTS
import code.name.monkey.retromusic.Constants.GENERAL_THEME
import code.name.monkey.retromusic.Constants.KEEP_SCREEN_ON
import code.name.monkey.retromusic.Constants.MATERIAL_YOU
import code.name.monkey.retromusic.Constants.SHOW_WHEN_LOCKED
import code.name.monkey.retromusic.Constants.TOGGLE_FULL_SCREEN
import code.name.monkey.retromusic.Constants.WALLPAPER_ACCENT
import code.name.monkey.retromusic.extensions.getStringOrDefault
import code.name.monkey.retromusic.util.theme.ThemeMode

object PreferenceUtil {
    private const val PREF_NAME = "MyPrefs"
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext())
    val isScreenOnEnabled get() = sharedPreferences.getBoolean(KEEP_SCREEN_ON, false)

    val isFullScreenMode
        get() = sharedPreferences.getBoolean(
            TOGGLE_FULL_SCREEN, false
        )
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

    val materialYou
        get() = sharedPreferences.getBoolean(MATERIAL_YOU, VersionUtils.hasS())

    fun getGeneralThemeValue(isSystemDark: Boolean): ThemeMode {
        val themeMode: String =
            sharedPreferences.getStringOrDefault(GENERAL_THEME, "auto")
        return if (isBlackMode && isSystemDark && themeMode != "light") {
            ThemeMode.BLACK
        } else {
            if (isBlackMode && themeMode == "dark") {
                ThemeMode.BLACK
            } else {
                when (themeMode) {
                    "light" -> ThemeMode.LIGHT
                    "dark" -> ThemeMode.DARK
                    "auto" -> ThemeMode.AUTO
                    else -> ThemeMode.AUTO
                }
            }
        }
    }

    private val isBlackMode
        get() = sharedPreferences.getBoolean(
            BLACK_THEME, false
        )

    val isShowWhenLockedEnabled get() = sharedPreferences.getBoolean(SHOW_WHEN_LOCKED, false)

}