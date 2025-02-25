@file:JvmName("SongUtils") //  top level function nên phải jvm name mowis goij dc sang java
package code.name.monkey.retromusic.util

import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.provider.MediaStore
import code.name.monkey.appthemehelper.util.VersionUtils
import code.name.monkey.retromusic.Constants
import code.name.monkey.retromusic.extensions.getInt
import code.name.monkey.retromusic.extensions.getLong
import code.name.monkey.retromusic.extensions.getString
import code.name.monkey.retromusic.extensions.getStringOrNull
import code.name.monkey.retromusic.helper.SortOrder
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.providers.BlacklistStore
import java.text.Collator

@JvmOverloads
fun makeSongCursor(
    context: Context,
    selection: String?,
    selectionValues: Array<String>?,
    sortOrder: String = PreferenceUtil.songSortOrder,
    ignoreBlacklist: Boolean = false
): Cursor? {
    var selectionFinal = selection
    var selectionValuesFinal = selectionValues
    if (!ignoreBlacklist) {
        selectionFinal = if (selection != null && selection.trim { it <= ' ' } != "") {
            "${MediaStore.Audio.AudioColumns.IS_MUSIC} AND $selectionFinal"
        } else {
            MediaStore.Audio.AudioColumns.IS_MUSIC
        }

        // Whitelist
        if (PreferenceUtil.isWhiteList) {
            selectionFinal =
                selectionFinal + " AND " + Constants.DATA + " LIKE ?"
            selectionValuesFinal = addSelectionValues(
                selectionValuesFinal, arrayListOf(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).canonicalPath
                )
            )
        } else {
            // Blacklist
            val paths = BlacklistStore.getInstance(context).paths
            if (paths.isNotEmpty()) {
                selectionFinal = generateBlacklistSelection(selectionFinal, paths.size)
                selectionValuesFinal = addSelectionValues(selectionValuesFinal, paths)
            }
        }

        selectionFinal =
            selectionFinal + " AND " + MediaStore.Audio.Media.DURATION + ">= " + (PreferenceUtil.filterLength * 1000)
    }
    val uri = if (VersionUtils.hasQ()) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }
    return try {
        context.contentResolver.query(
            uri,
            Constants.baseProjection,
            selectionFinal,
            selectionValuesFinal,
            sortOrder
        )
    } catch (ex: SecurityException) {
        return null
    }
}

fun addSelectionValues(
    selectionValues: Array<String>?,
    paths: ArrayList<String>
): Array<String> {
    var selectionValuesFinal = selectionValues
    if (selectionValuesFinal == null) {
        selectionValuesFinal = emptyArray()
    }
    val newSelectionValues = Array(selectionValuesFinal.size + paths.size) {
        "n = $it"
    }
    System.arraycopy(selectionValuesFinal, 0, newSelectionValues, 0, selectionValuesFinal.size)
    for (i in selectionValuesFinal.size until newSelectionValues.size) {
        newSelectionValues[i] = paths[i - selectionValuesFinal.size] + "%"
    }
    return newSelectionValues
}

fun generateBlacklistSelection(
    selection: String?,
    pathCount: Int
): String {
    val newSelection = StringBuilder(
        if (selection != null && selection.trim { it <= ' ' } != "") "$selection AND " else "")
    newSelection.append(Constants.DATA + " NOT LIKE ?")
    for (i in 0 until pathCount - 1) {
        newSelection.append(" AND " + Constants.DATA + " NOT LIKE ?")
    }
    return newSelection.toString()
}

fun splitIntoAlbums(
    songs: List<Song>,
    sorted: Boolean = true
): List<Album> {
    val grouped = songs.groupBy { it.albumId }.map { Album(it.key, it.value) }
    if (!sorted) return grouped
    val collator = Collator.getInstance()
    return when (PreferenceUtil.albumSortOrder) {
        SortOrder.AlbumSortOrder.ALBUM_A_Z -> {
            grouped.sortedWith { a1, a2 -> collator.compare(a1.title, a2.title) }
        }

        SortOrder.AlbumSortOrder.ALBUM_Z_A -> {
            grouped.sortedWith { a1, a2 -> collator.compare(a2.title, a1.title) }
        }

        SortOrder.AlbumSortOrder.ALBUM_ARTIST -> {
            grouped.sortedWith { a1, a2 -> collator.compare(a1.albumArtist, a2.albumArtist) }
        }

        SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS -> {
            grouped.sortedByDescending { it.songCount }
        }

        else -> grouped
    }
}

fun sortAlbumSongs(album: Album): Album {
    val collator = Collator.getInstance()
    val songs = when (PreferenceUtil.albumDetailSongSortOrder) {
        SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST -> album.songs.sortedWith { o1, o2 ->
            o1.trackNumber.compareTo(o2.trackNumber)
        }

        SortOrder.AlbumSongSortOrder.SONG_A_Z -> {
            album.songs.sortedWith { o1, o2 -> collator.compare(o1.title, o2.title) }
        }

        SortOrder.AlbumSongSortOrder.SONG_Z_A -> {
            album.songs.sortedWith { o1, o2 -> collator.compare(o2.title, o1.title) }
        }

        SortOrder.AlbumSongSortOrder.SONG_DURATION -> album.songs.sortedWith { o1, o2 ->
            o1.duration.compareTo(o2.duration)
        }

        else -> throw IllegalArgumentException("invalid ${PreferenceUtil.albumDetailSongSortOrder}")
    }
    return album.copy(songs = songs)
}

fun getSongLoaderSortOrder(): String {
    var albumSortOrder = PreferenceUtil.albumSortOrder
    if (albumSortOrder == SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS)
        albumSortOrder = SortOrder.AlbumSortOrder.ALBUM_A_Z
    return albumSortOrder + ", " +
            PreferenceUtil.albumSongSortOrder
}


fun splitIntoArtists(albums: List<Album>): List<Artist> {
    return albums.groupBy { it.artistId }
        .map { Artist(it.key, it.value) }
}

fun songs(cursor: Cursor?): List<Song> {
    val songs = arrayListOf<Song>()
    if (cursor != null && cursor.moveToFirst()) {
        do {
            songs.add(getSongFromCursorImpl(cursor))
        } while (cursor.moveToNext())
    }
    cursor?.close()
    return songs
}

fun getSongFromCursorImpl(
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