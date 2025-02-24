package code.name.monkey.retromusic.repository.dataSourceImpl

import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns.IS_MUSIC
import code.name.monkey.appthemehelper.util.VersionUtils
import code.name.monkey.retromusic.Constants
import code.name.monkey.retromusic.Constants.baseProjection
import code.name.monkey.retromusic.extensions.getInt
import code.name.monkey.retromusic.extensions.getLong
import code.name.monkey.retromusic.extensions.getString
import code.name.monkey.retromusic.extensions.getStringOrNull
import code.name.monkey.retromusic.helper.SortOrder
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.providers.BlacklistStore
import code.name.monkey.retromusic.repository.dataSource.SongLocalRepository
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.addSelectionValues
import code.name.monkey.retromusic.util.generateBlacklistSelection
import code.name.monkey.retromusic.util.makeSongCursor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.Collator
import javax.inject.Inject

class SongLocalRepositoryImpl @Inject constructor(private val context: Context) :
    SongLocalRepository { // có class thì dùng inject constructor
    override fun songs(): List<Song> {
        return sortedSongs(makeSongCursor(context, null, null))
    }

    override fun songs(cursor: Cursor?): List<Song> {
        val songs = arrayListOf<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    override fun songs(query: String): List<Song> {
        return songs(
            makeSongCursor(
                context,
                MediaStore.Audio.AudioColumns.TITLE + " LIKE ?",
                arrayOf("%$query%")
            )
        )
    }

    override fun sortedSongs(cursor: Cursor?): List<Song> {
        val collator = Collator.getInstance()
        val songs = songs(cursor)
        return when (PreferenceUtil.songSortOrder) {
            SortOrder.SongSortOrder.SONG_A_Z -> {
                songs.sortedWith { s1, s2 -> collator.compare(s1.title, s2.title) }
            }

            SortOrder.SongSortOrder.SONG_Z_A -> {
                songs.sortedWith { s1, s2 -> collator.compare(s2.title, s1.title) }
            }

            SortOrder.SongSortOrder.SONG_ALBUM -> {
                songs.sortedWith { s1, s2 -> collator.compare(s1.albumName, s2.albumName) }
            }

            SortOrder.SongSortOrder.SONG_ALBUM_ARTIST -> {
                songs.sortedWith { s1, s2 -> collator.compare(s1.albumArtist, s2.albumArtist) }
            }

            SortOrder.SongSortOrder.SONG_ARTIST -> {
                songs.sortedWith { s1, s2 -> collator.compare(s1.artistName, s2.artistName) }
            }

            SortOrder.SongSortOrder.COMPOSER -> {
                songs.sortedWith { s1, s2 -> collator.compare(s1.composer, s2.composer) }
            }

            else -> songs
        }
    }

    override fun songsByFilePath(filePath: String, ignoreBlacklist: Boolean): List<Song> {
        return songs(
            makeSongCursor(
                context,
                Constants.DATA + "=?",
                arrayOf(filePath),
                ignoreBlacklist = ignoreBlacklist
            )
        )
    }

    override fun song(cursor: Cursor?): Song {
        val song: Song = if (cursor != null && cursor.moveToFirst()) {
            getSongFromCursorImpl(cursor)
        } else {
            Song.emptySong
        }
        cursor?.close()
        return song
    }

    override fun song(songId: Long): Song {
        return song(
            makeSongCursor(
                context,
                MediaStore.Audio.AudioColumns._ID + "=?",
                arrayOf(songId.toString())
            )
        )
    }

    private fun getSongFromCursorImpl(
        cursor: Cursor
    ): Song {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val title = cursor.getString(MediaStore.Audio.AudioColumns.TITLE)
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK)
        val year = cursor.getInt(MediaStore.Audio.AudioColumns.YEAR)
        val duration = cursor.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val data = cursor.getString(Constants.DATA)
        val dateModified = cursor.getLong(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val albumName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ALBUM)
        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val artistName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ARTIST)
        val composer = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.COMPOSER)
        val albumArtist = cursor.getStringOrNull("album_artist")
        return Song(
            id,
            title,
            trackNumber,
            year,
            duration,
            data,
            dateModified,
            albumId,
            albumName ?: "",
            artistId,
            artistName ?: "",
            composer ?: "",
            albumArtist ?: ""
        )
    }
}