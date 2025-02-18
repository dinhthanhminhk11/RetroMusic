package code.name.monkey.retromusic

import android.provider.BaseColumns
import android.provider.MediaStore

object Constants {
    const val WALLPAPER_ACCENT = "wallpaper_accent"
    const val COLORED_APP_SHORTCUTS = "colored_app_shortcuts"
    const val KEEP_SCREEN_ON = "keep_screen_on"
    const val TOGGLE_FULL_SCREEN = "toggle_full_screen"
    const val MATERIAL_YOU = "material_you"
    const val GENERAL_THEME = "general_theme"
    const val BLACK_THEME = "black_theme"
    const val SHOW_WHEN_LOCKED = "show_when_locked"
    const val DATA = "_data"
    const val SONG_SORT_ORDER = "song_sort_order"
    const val ALBUM_ARTIST = "album_artist"
    const val WHITELIST_MUSIC = "whitelist_music"
    const val FILTER_SONG = "filter_song"
    const val INITIALIZED_BLACKLIST = "initialized_blacklist"
    const val ALBUM_SORT_ORDER = "album_sort_order"
    const val ALBUM_DETAIL_SONG_SORT_ORDER = "album_detail_song_sort_order"
    const val ALBUM_SONG_SORT_ORDER = "album_song_sort_order"
    const val GENRE_SORT_ORDER = "genre_sort_order"
    @Suppress("Deprecation")
    val baseProjection = arrayOf(
        BaseColumns._ID, // 0
        MediaStore.Audio.AudioColumns.TITLE, // 1
        MediaStore.Audio.AudioColumns.TRACK, // 2
        MediaStore.Audio.AudioColumns.YEAR, // 3
        MediaStore.Audio.AudioColumns.DURATION, // 4
        DATA, // 5
        MediaStore.Audio.AudioColumns.DATE_MODIFIED, // 6
        MediaStore.Audio.AudioColumns.ALBUM_ID, // 7
        MediaStore.Audio.AudioColumns.ALBUM, // 8
        MediaStore.Audio.AudioColumns.ARTIST_ID, // 9
        MediaStore.Audio.AudioColumns.ARTIST, // 10
        MediaStore.Audio.AudioColumns.COMPOSER, // 11
        ALBUM_ARTIST // 12
    )
}