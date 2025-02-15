package code.name.monkey.retromusic

import android.app.Application
import androidx.preference.PreferenceManager
import cat.ereza.customactivityoncrash.config.CaocConfig
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.VersionUtils
import code.name.monkey.retromusic.activities.ErrorActivity
import code.name.monkey.retromusic.appshortcuts.DynamicShortcutManager
import code.name.monkey.retromusic.billing.BillingManager
import code.name.monkey.retromusic.encryption.Login
import code.name.monkey.retromusic.helper.WallpaperAccentManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    lateinit var billingManager: BillingManager
    private val wallpaperAccentManager = WallpaperAccentManager(this)

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (!ThemeStore.isConfigured(this, 3)) {
            ThemeStore.editTheme(this)
                .accentColorRes(code.name.monkey.appthemehelper.R.color.md_deep_orange_A400)
                .coloredNavigationBar(true)
                .commit()
        }
        wallpaperAccentManager.init()

        if (VersionUtils.hasNougatMR())
            DynamicShortcutManager(this).initDynamicShortcuts()

        billingManager = BillingManager(this)

        CaocConfig.Builder.create().errorActivity(ErrorActivity::class.java)
            .restartActivity(MainActivity::class.java).apply()

        PreferenceManager.setDefaultValues(this, R.xml.pref_now_playing_screen, false)
        Login.init(BuildConfig.KEY_128, BuildConfig.IV_128)
    }

    override fun onTerminate() {
        super.onTerminate()
        billingManager.release()
        wallpaperAccentManager.release()
    }

    companion object {
        private var instance: App? = null

        fun getContext(): App {
            return instance!!
        }

        fun isProVersion(): Boolean {
            return BuildConfig.DEBUG || instance?.billingManager!!.isProVersion
        }
    }
}