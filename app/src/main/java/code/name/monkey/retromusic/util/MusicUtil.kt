package code.name.monkey.retromusic.util

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import code.name.monkey.retromusic.model.Artist

object MusicUtil {

    fun getSongFileUri(songId: Long): Uri {
        return ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            songId
        )
    }

    @JvmStatic
    fun getMediaStoreAlbumCoverUri(albumId: Long): Uri {
        val sArtworkUri = "content://media/external/audio/albumart".toUri()
        return ContentUris.withAppendedId(sArtworkUri, albumId)
    }

    fun isVariousArtists(artistName: String?): Boolean {
        if (artistName.isNullOrEmpty()) {
            return false
        }
        if (artistName == Artist.VARIOUS_ARTISTS_DISPLAY_NAME) {
            return true
        }
        return false
    }

    fun isArtistNameUnknown(artistName: String?): Boolean {
        if (artistName.isNullOrEmpty()) {
            return false
        }
        if (artistName == Artist.UNKNOWN_ARTIST_DISPLAY_NAME) {
            return true
        }
        val tempName = artistName.trim { it <= ' ' }.lowercase()
        return tempName == "unknown" || tempName == "<unknown>"
    }


}