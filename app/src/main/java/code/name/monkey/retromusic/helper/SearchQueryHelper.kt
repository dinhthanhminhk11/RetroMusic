
package code.name.monkey.retromusic.helper

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.util.makeSongCursor
import code.name.monkey.retromusic.util.songs
import org.koin.core.component.KoinComponent

object SearchQueryHelper : KoinComponent {
    private const val TITLE_SELECTION = "lower(" + MediaStore.Audio.AudioColumns.TITLE + ") = ?"
    private const val ALBUM_SELECTION = "lower(" + MediaStore.Audio.AudioColumns.ALBUM + ") = ?"
    private const val ARTIST_SELECTION = "lower(" + MediaStore.Audio.AudioColumns.ARTIST + ") = ?"
    private const val AND = " AND "
    var songs = ArrayList<Song>()

    @JvmStatic
    fun getSongs(context: Context, extras: Bundle): List<Song> {
        val query = extras.getString(SearchManager.QUERY, null)
        val artistName = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST, null)
        val albumName = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM, null)
        val titleName = extras.getString(MediaStore.EXTRA_MEDIA_TITLE, null)

        var songs = listOf<Song>()
        if (artistName != null && albumName != null && titleName != null) {
            songs = songs(
                makeSongCursor(
                    context,
                    ARTIST_SELECTION + AND + ALBUM_SELECTION + AND + TITLE_SELECTION,
                    arrayOf(
                        artistName.lowercase(),
                        albumName.lowercase(),
                        titleName.lowercase()
                    )
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (artistName != null && titleName != null) {
            songs = songs(
                makeSongCursor(
                    context,
                    ARTIST_SELECTION + AND + TITLE_SELECTION,
                    arrayOf(
                        artistName.lowercase(),
                        titleName.lowercase()
                    )
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (albumName != null && titleName != null) {
            songs = songs(
                makeSongCursor(
                    context,
                    ALBUM_SELECTION + AND + TITLE_SELECTION,
                    arrayOf(
                        albumName.lowercase(),
                        titleName.lowercase()
                    )
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (artistName != null) {
            songs = songs(
                makeSongCursor(
                    context,
                    ARTIST_SELECTION,
                    arrayOf(artistName.lowercase())
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (albumName != null) {
            songs = songs(
                makeSongCursor(
                    context,
                    ALBUM_SELECTION,
                    arrayOf(albumName.lowercase())
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (titleName != null) {
            songs = songs(
                makeSongCursor(
                    context,
                    TITLE_SELECTION,
                    arrayOf(titleName.lowercase())
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        songs = songs(
            makeSongCursor(
                context,
                ARTIST_SELECTION,
                arrayOf(query.lowercase())
            )
        )

        if (songs.isNotEmpty()) {
            return songs
        }
        songs = songs(
            makeSongCursor(
                context,
                ALBUM_SELECTION,
                arrayOf(query.lowercase())
            )
        )
        if (songs.isNotEmpty()) {
            return songs
        }
        songs = songs(
            makeSongCursor(
                context,
                TITLE_SELECTION,
                arrayOf(query.lowercase())
            )
        )
        return songs.ifEmpty { ArrayList() }
    }
}
