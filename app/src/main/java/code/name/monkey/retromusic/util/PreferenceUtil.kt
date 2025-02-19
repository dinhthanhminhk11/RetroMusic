package code.name.monkey.retromusic.util


import androidx.core.content.edit
import androidx.preference.PreferenceManager
import code.name.monkey.appthemehelper.util.VersionUtils
import code.name.monkey.retromusic.ALBUM_DETAIL_SONG_SORT_ORDER
import code.name.monkey.retromusic.ALBUM_SONG_SORT_ORDER
import code.name.monkey.retromusic.ALBUM_SORT_ORDER
import code.name.monkey.retromusic.ARTIST_ALBUM_SORT_ORDER
import code.name.monkey.retromusic.ARTIST_DETAIL_SONG_SORT_ORDER
import code.name.monkey.retromusic.ARTIST_SONG_SORT_ORDER
import code.name.monkey.retromusic.ARTIST_SORT_ORDER
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.BLACK_THEME
import code.name.monkey.retromusic.COLORED_APP_SHORTCUTS
import code.name.monkey.retromusic.FILTER_SONG
import code.name.monkey.retromusic.GENERAL_THEME
import code.name.monkey.retromusic.GENRE_SORT_ORDER
import code.name.monkey.retromusic.INITIALIZED_BLACKLIST
import code.name.monkey.retromusic.KEEP_SCREEN_ON
import code.name.monkey.retromusic.MATERIAL_YOU
import code.name.monkey.retromusic.PLAYLIST_SORT_ORDER
import code.name.monkey.retromusic.RECENTLY_PLAYED_CUTOFF
import code.name.monkey.retromusic.SHOW_WHEN_LOCKED
import code.name.monkey.retromusic.SONG_SORT_ORDER
import code.name.monkey.retromusic.TOGGLE_FULL_SCREEN
import code.name.monkey.retromusic.WALLPAPER_ACCENT
import code.name.monkey.retromusic.WHITELIST_MUSIC

import code.name.monkey.retromusic.extensions.getStringOrDefault
import code.name.monkey.retromusic.helper.SortOrder
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


    var songSortOrder
        get() = sharedPreferences.getStringOrDefault(
            SONG_SORT_ORDER,
            SortOrder.SongSortOrder.SONG_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(SONG_SORT_ORDER, value)
        }

    val isWhiteList: Boolean
        get() = sharedPreferences.getBoolean(WHITELIST_MUSIC, false)
    val filterLength get() = sharedPreferences.getInt(FILTER_SONG, 20)

    var isInitializedBlacklist
        get() = sharedPreferences.getBoolean(
            INITIALIZED_BLACKLIST, false
        )
        set(value) = sharedPreferences.edit {
            putBoolean(INITIALIZED_BLACKLIST, value)
        }

    var albumSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ALBUM_SORT_ORDER,
            SortOrder.AlbumSortOrder.ALBUM_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(ALBUM_SORT_ORDER, value)
        }

    var albumDetailSongSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ALBUM_DETAIL_SONG_SORT_ORDER,
            SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST
        )
        set(value) = sharedPreferences.edit { putString(ALBUM_DETAIL_SONG_SORT_ORDER, value) }

    val albumSongSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ALBUM_SONG_SORT_ORDER,
            SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST
        )

    val genreSortOrder
        get() = sharedPreferences.getStringOrDefault(
            GENRE_SORT_ORDER,
            SortOrder.GenreSortOrder.GENRE_A_Z
        )

    var artistDetailSongSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ARTIST_DETAIL_SONG_SORT_ORDER,
            SortOrder.ArtistSongSortOrder.SONG_A_Z
        )
        set(value) = sharedPreferences.edit { putString(ARTIST_DETAIL_SONG_SORT_ORDER, value) }

    var artistAlbumSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ARTIST_ALBUM_SORT_ORDER,
            SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR
        )
        set(value) = sharedPreferences.edit {
            putString(ARTIST_ALBUM_SORT_ORDER, value)
        }

    var artistSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ARTIST_SORT_ORDER,
            SortOrder.ArtistSortOrder.ARTIST_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(ARTIST_SORT_ORDER, value)
        }

    val artistSongSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ARTIST_SONG_SORT_ORDER,
            SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST
        )

    fun getRecentlyPlayedCutoffTimeMillis(): Long {
        val calendarUtil = CalendarUtil()
        val interval: Long = when (sharedPreferences.getString(RECENTLY_PLAYED_CUTOFF, "")) {
            "today" -> calendarUtil.elapsedToday
            "this_week" -> calendarUtil.elapsedWeek
            "past_seven_days" -> calendarUtil.getElapsedDays(7)
            "past_three_months" -> calendarUtil.getElapsedMonths(3)
            "this_year" -> calendarUtil.elapsedYear
            "this_month" -> calendarUtil.elapsedMonth
            else -> calendarUtil.elapsedMonth
        }
        return System.currentTimeMillis() - interval
    }

    var playlistSortOrder
        get() = sharedPreferences.getStringOrDefault(
            PLAYLIST_SORT_ORDER,
            SortOrder.PlaylistSortOrder.PLAYLIST_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(PLAYLIST_SORT_ORDER, value)
        }
}

enum class CoverLyricsType {
    REPLACE_COVER, OVER_COVER
}