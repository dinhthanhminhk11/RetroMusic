package code.name.monkey.retromusic.service

import android.support.v4.media.session.PlaybackStateCompat

class MusicService {
    companion object {
        val TAG: String = MusicService::class.java.simpleName
        const val RETRO_MUSIC_PACKAGE_NAME = "code.name.monkey.retromusic"
        const val MUSIC_PACKAGE_NAME = "com.android.music"
        const val ACTION_TOGGLE_PAUSE = "$RETRO_MUSIC_PACKAGE_NAME.togglepause"
        const val ACTION_PLAY = "$RETRO_MUSIC_PACKAGE_NAME.play"
        const val ACTION_PLAY_PLAYLIST = "$RETRO_MUSIC_PACKAGE_NAME.play.playlist"
        const val ACTION_PAUSE = "$RETRO_MUSIC_PACKAGE_NAME.pause"
        const val ACTION_STOP = "$RETRO_MUSIC_PACKAGE_NAME.stop"
        const val ACTION_SKIP = "$RETRO_MUSIC_PACKAGE_NAME.skip"
        const val ACTION_REWIND = "$RETRO_MUSIC_PACKAGE_NAME.rewind"
        const val ACTION_QUIT = "$RETRO_MUSIC_PACKAGE_NAME.quitservice"
        const val ACTION_PENDING_QUIT = "$RETRO_MUSIC_PACKAGE_NAME.pendingquitservice"
        const val INTENT_EXTRA_PLAYLIST = RETRO_MUSIC_PACKAGE_NAME + "intentextra.playlist"
        const val INTENT_EXTRA_SHUFFLE_MODE =
            "$RETRO_MUSIC_PACKAGE_NAME.intentextra.shufflemode"
        const val APP_WIDGET_UPDATE = "$RETRO_MUSIC_PACKAGE_NAME.appwidgetupdate"
        const val EXTRA_APP_WIDGET_NAME = RETRO_MUSIC_PACKAGE_NAME + "app_widget_name"

        // Do not change these three strings as it will break support with other apps (e.g. last.fm
        // scrobbling)
        const val META_CHANGED = "$RETRO_MUSIC_PACKAGE_NAME.metachanged"
        const val QUEUE_CHANGED = "$RETRO_MUSIC_PACKAGE_NAME.queuechanged"
        const val PLAY_STATE_CHANGED = "$RETRO_MUSIC_PACKAGE_NAME.playstatechanged"
        const val FAVORITE_STATE_CHANGED = "$RETRO_MUSIC_PACKAGE_NAME.favoritestatechanged"
        const val REPEAT_MODE_CHANGED = "$RETRO_MUSIC_PACKAGE_NAME.repeatmodechanged"
        const val SHUFFLE_MODE_CHANGED = "$RETRO_MUSIC_PACKAGE_NAME.shufflemodechanged"
        const val MEDIA_STORE_CHANGED = "$RETRO_MUSIC_PACKAGE_NAME.mediastorechanged"
        const val CYCLE_REPEAT = "$RETRO_MUSIC_PACKAGE_NAME.cyclerepeat"
        const val TOGGLE_SHUFFLE = "$RETRO_MUSIC_PACKAGE_NAME.toggleshuffle"
        const val TOGGLE_FAVORITE = "$RETRO_MUSIC_PACKAGE_NAME.togglefavorite"
        const val SAVED_POSITION = "POSITION"
        const val SAVED_POSITION_IN_TRACK = "POSITION_IN_TRACK"
        const val SAVED_SHUFFLE_MODE = "SHUFFLE_MODE"
        const val SAVED_REPEAT_MODE = "REPEAT_MODE"
        const val SHUFFLE_MODE_NONE = 0
        const val SHUFFLE_MODE_SHUFFLE = 1
        const val REPEAT_MODE_NONE = 0
        const val REPEAT_MODE_ALL = 1
        const val REPEAT_MODE_THIS = 2
        private const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SEEK_TO)
    }

}