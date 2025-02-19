package code.name.monkey.retromusic.repository.dataSourceImpl

import android.content.ContentResolver
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import code.name.monkey.retromusic.Constants
import code.name.monkey.retromusic.extensions.getInt
import code.name.monkey.retromusic.extensions.getLong
import code.name.monkey.retromusic.extensions.getString
import code.name.monkey.retromusic.extensions.getStringOrNull
import code.name.monkey.retromusic.model.Playlist
import code.name.monkey.retromusic.model.PlaylistSong
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.repository.dataSource.PlaylistLocalRepository

class PlaylistLocalRepositoryImpl(private val contentResolver: ContentResolver) :
    PlaylistLocalRepository {
    override fun playlist(cursor: Cursor?): Playlist {
        return cursor.use {
            if (cursor?.moveToFirst() == true) {
                getPlaylistFromCursorImpl(cursor)
            } else {
                Playlist.empty
            }
        }
    }

    override fun playlist(playlistName: String): Playlist {
        return playlist(
            makePlaylistCursor(
                MediaStore.Audio.PlaylistsColumns.NAME + "=?",
                arrayOf(playlistName)
            )
        )
    }

    override fun playlist(playlistId: Long): Playlist {
        return playlist(
            makePlaylistCursor(
                BaseColumns._ID + "=?",
                arrayOf(playlistId.toString())
            )
        )
    }

    override fun searchPlaylist(query: String): List<Playlist> {
        return playlists(
            makePlaylistCursor(
                MediaStore.Audio.PlaylistsColumns.NAME + "=?",
                arrayOf(query)
            )
        )
    }

    override fun playlists(): List<Playlist> {
        return playlists(makePlaylistCursor(null, null))
    }

    override fun playlists(cursor: Cursor?): List<Playlist> {
        val playlists = mutableListOf<Playlist>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlists.add(getPlaylistFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return playlists
    }

    override fun favoritePlaylist(playlistName: String): List<Playlist> {
        return playlists(
            makePlaylistCursor(
                MediaStore.Audio.PlaylistsColumns.NAME + "=?",
                arrayOf(playlistName)
            )
        )
    }

    override fun deletePlaylist(playlistId: Long) {
        val localUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val localStringBuilder = StringBuilder()
        localStringBuilder.append("_id IN (")
        localStringBuilder.append(playlistId)
        localStringBuilder.append(")")
        contentResolver.delete(localUri, localStringBuilder.toString(), null)
    }

    private fun getPlaylistFromCursorImpl(
        cursor: Cursor
    ): Playlist {
        val id = cursor.getLong(0)
        val name = cursor.getString(1)
        return if (name != null) {
            Playlist(id, name)
        } else {
            Playlist.empty
        }
    }

    override fun playlistSongs(playlistId: Long): List<Song> {
        val songs = arrayListOf<Song>()
        if (playlistId == -1L) return songs
        val cursor = makePlaylistSongCursor(playlistId)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getPlaylistSongFromCursorImpl(cursor, playlistId))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    private fun getPlaylistSongFromCursorImpl(cursor: Cursor, playlistId: Long): PlaylistSong {
        val id = cursor.getLong(MediaStore.Audio.Playlists.Members.AUDIO_ID)
        val title = cursor.getString(MediaStore.Audio.AudioColumns.TITLE)
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK)
        val year = cursor.getInt(MediaStore.Audio.AudioColumns.YEAR)
        val duration = cursor.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val data = cursor.getString(Constants.DATA)
        val dateModified = cursor.getLong(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val albumName = cursor.getString(MediaStore.Audio.AudioColumns.ALBUM)
        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val artistName = cursor.getString(MediaStore.Audio.AudioColumns.ARTIST)
        val idInPlaylist = cursor.getLong(MediaStore.Audio.Playlists.Members._ID)
        val composer = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.COMPOSER)
        val albumArtist = cursor.getStringOrNull("album_artist")
        return PlaylistSong(
            id,
            title,
            trackNumber,
            year,
            duration,
            data,
            dateModified,
            albumId,
            albumName,
            artistId,
            artistName,
            playlistId,
            idInPlaylist,
            composer ?: "",
            albumArtist
        )
    }

    private fun makePlaylistCursor(
        selection: String?,
        values: Array<String>?
    ): Cursor? {
        return contentResolver.query(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            arrayOf(
                BaseColumns._ID, /* 0 */
                MediaStore.Audio.PlaylistsColumns.NAME /* 1 */
            ),
            selection,
            values,
            MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER
        )
    }


    private fun makePlaylistSongCursor(playlistId: Long): Cursor? {
        return contentResolver.query(
            MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
            arrayOf(
                MediaStore.Audio.Playlists.Members.AUDIO_ID, // 0
                MediaStore.Audio.AudioColumns.TITLE, // 1
                MediaStore.Audio.AudioColumns.TRACK, // 2
                MediaStore.Audio.AudioColumns.YEAR, // 3
                MediaStore.Audio.AudioColumns.DURATION, // 4
                Constants.DATA, // 5
                MediaStore.Audio.AudioColumns.DATE_MODIFIED, // 6
                MediaStore.Audio.AudioColumns.ALBUM_ID, // 7
                MediaStore.Audio.AudioColumns.ALBUM, // 8
                MediaStore.Audio.AudioColumns.ARTIST_ID, // 9
                MediaStore.Audio.AudioColumns.ARTIST, // 10
                MediaStore.Audio.Playlists.Members._ID,//11
                MediaStore.Audio.AudioColumns.COMPOSER,//12
                "album_artist"//13
            ), Constants.IS_MUSIC, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER
        )
    }
}