package code.name.monkey.retromusic.repository.dataSource

import android.database.Cursor
import code.name.monkey.retromusic.model.Song

interface SongLocalRepository {
    fun songs(): List<Song>

    fun songs(cursor: Cursor?): List<Song>

    fun sortedSongs(cursor: Cursor?): List<Song>

    fun songs(query: String): List<Song>

    fun songsByFilePath(filePath: String, ignoreBlacklist: Boolean = false): List<Song>

    fun song(cursor: Cursor?): Song

    fun song(songId: Long): Song
}