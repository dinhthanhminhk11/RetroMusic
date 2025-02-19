package code.name.monkey.retromusic.repository.dataSource

import android.content.Context
import androidx.lifecycle.LiveData
import code.name.monkey.retromusic.db.HistoryEntity
import code.name.monkey.retromusic.db.PlayCountEntity
import code.name.monkey.retromusic.db.PlaylistEntity
import code.name.monkey.retromusic.db.PlaylistWithSongs
import code.name.monkey.retromusic.db.SongEntity
import code.name.monkey.retromusic.model.Song

interface RoomRepository {
    fun historySongs(): List<HistoryEntity>
    fun favoritePlaylistLiveData(favorite: String): LiveData<List<SongEntity>>
    fun observableHistorySongs(): LiveData<List<HistoryEntity>>
    fun getSongs(playListId: Long): LiveData<List<SongEntity>>
    suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long
    suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity>
    suspend fun playlists(): List<PlaylistEntity>
    suspend fun playlistWithSongs(): List<PlaylistWithSongs>
    suspend fun insertSongs(songs: List<SongEntity>)
    suspend fun deletePlaylistEntities(playlistEntities: List<PlaylistEntity>)
    suspend fun renamePlaylistEntity(playlistId: Long, name: String)
    suspend fun deleteSongsInPlaylist(songs: List<SongEntity>)
    suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>)
    suspend fun favoritePlaylist(favorite: String): PlaylistEntity
    suspend fun isFavoriteSong(songEntity: SongEntity): List<SongEntity>
    suspend fun removeSongFromPlaylist(songEntity: SongEntity)
    suspend fun upsertSongInHistory(currentSong: Song)
    suspend fun favoritePlaylistSongs(favorite: String): List<SongEntity>
    suspend fun upsertSongInPlayCount(playCountEntity: PlayCountEntity)
    suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity)
    suspend fun deleteSongInHistory(songId: Long)
    suspend fun clearSongHistory()
    suspend fun findSongExistInPlayCount(songId: Long): PlayCountEntity?
    suspend fun playCountSongs(): List<PlayCountEntity>
    suspend fun deleteSongs(songs: List<Song>)
    suspend fun isSongFavorite(context: Context, songId: Long): Boolean
    fun checkPlaylistExists(playListId: Long): LiveData<Boolean>
    fun getPlaylist(playlistId: Long): LiveData<PlaylistWithSongs>
}