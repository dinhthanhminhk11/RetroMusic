package code.name.monkey.retromusic.repository.dataSourceImpl

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.db.HistoryEntity
import code.name.monkey.retromusic.db.PlayCountEntity
import code.name.monkey.retromusic.db.PlaylistEntity
import code.name.monkey.retromusic.db.PlaylistWithSongs
import code.name.monkey.retromusic.db.SongEntity
import code.name.monkey.retromusic.db.dao.HistoryDao
import code.name.monkey.retromusic.db.dao.PlayCountDao
import code.name.monkey.retromusic.db.dao.PlaylistDao
import code.name.monkey.retromusic.db.toHistoryEntity
import code.name.monkey.retromusic.helper.SortOrder.PlaylistSortOrder.Companion.PLAYLIST_A_Z
import code.name.monkey.retromusic.helper.SortOrder.PlaylistSortOrder.Companion.PLAYLIST_SONG_COUNT
import code.name.monkey.retromusic.helper.SortOrder.PlaylistSortOrder.Companion.PLAYLIST_SONG_COUNT_DESC
import code.name.monkey.retromusic.helper.SortOrder.PlaylistSortOrder.Companion.PLAYLIST_Z_A
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.repository.dataSource.RoomRepository
import code.name.monkey.retromusic.util.PreferenceUtil

class RoomRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playCountDao: PlayCountDao,
    private val historyDao: HistoryDao
) : RoomRepository {
    override fun historySongs(): List<HistoryEntity> = historyDao.historySongs()

    override fun favoritePlaylistLiveData(favorite: String): LiveData<List<SongEntity>> =
        playlistDao.favoritesSongsLiveData(favorite)

    override fun observableHistorySongs(): LiveData<List<HistoryEntity>> =
        historyDao.observableHistorySongs()

    override fun getSongs(playListId: Long): LiveData<List<SongEntity>> =
        playlistDao.songsFromPlaylist(playListId)

    @WorkerThread
    override suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long =
        playlistDao.createPlaylist(playlistEntity)

    @WorkerThread
    override suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity> =
        playlistDao.playlist(playlistName)

    override fun checkPlaylistExists(playListId: Long): LiveData<Boolean> =
        playlistDao.checkPlaylistExists(playListId)

    @WorkerThread
    override suspend fun playlists(): List<PlaylistEntity> = playlistDao.playlists()

    @WorkerThread
    override suspend fun playlistWithSongs(): List<PlaylistWithSongs> =
        when (PreferenceUtil.playlistSortOrder) {
            PLAYLIST_A_Z ->
                playlistDao.playlistsWithSongs().sortedBy {
                    it.playlistEntity.playlistName
                }

            PLAYLIST_Z_A -> playlistDao.playlistsWithSongs()
                .sortedByDescending {
                    it.playlistEntity.playlistName
                }

            PLAYLIST_SONG_COUNT -> playlistDao.playlistsWithSongs().sortedBy { it.songs.size }
            PLAYLIST_SONG_COUNT_DESC -> playlistDao.playlistsWithSongs()
                .sortedByDescending { it.songs.size }

            else -> playlistDao.playlistsWithSongs().sortedBy {
                it.playlistEntity.playlistName
            }
        }

    @WorkerThread
    override suspend fun insertSongs(songs: List<SongEntity>) {
        playlistDao.insertSongsToPlaylist(songs)
    }

    override suspend fun deletePlaylistEntities(playlistEntities: List<PlaylistEntity>) =
        playlistDao.deletePlaylists(playlistEntities)

    override suspend fun renamePlaylistEntity(playlistId: Long, name: String) =
        playlistDao.renamePlaylist(playlistId, name)

    override suspend fun deleteSongsInPlaylist(songs: List<SongEntity>) {
        songs.forEach {
            playlistDao.deleteSongFromPlaylist(it.playlistCreatorId, it.id)
        }
    }

    override suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>) =
        playlists.forEach {
            playlistDao.deletePlaylistSongs(it.playListId)
        }

    override suspend fun favoritePlaylist(favorite: String): PlaylistEntity {
        val playlist: PlaylistEntity? = playlistDao.playlist(favorite).firstOrNull()
        return if (playlist != null) {
            playlist
        } else {
            createPlaylist(PlaylistEntity(playlistName = favorite))
            playlistDao.playlist(favorite).first()
        }
    }

    override suspend fun isFavoriteSong(songEntity: SongEntity): List<SongEntity> =
        playlistDao.isSongExistsInPlaylist(
            songEntity.playlistCreatorId,
            songEntity.id
        )

    override suspend fun removeSongFromPlaylist(songEntity: SongEntity) =
        playlistDao.deleteSongFromPlaylist(songEntity.playlistCreatorId, songEntity.id)

    override suspend fun upsertSongInHistory(currentSong: Song) =
        historyDao.upsertSongInHistory(currentSong.toHistoryEntity(System.currentTimeMillis()))

    override suspend fun favoritePlaylistSongs(favorite: String): List<SongEntity> =
        if (playlistDao.playlist(favorite).isNotEmpty()) playlistDao.favoritesSongs(
            playlistDao.playlist(favorite).first().playListId
        ) else emptyList()

    override suspend fun upsertSongInPlayCount(playCountEntity: PlayCountEntity) =
        playCountDao.upsertSongInPlayCount(playCountEntity)

    override suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity) =
        playCountDao.deleteSongInPlayCount(playCountEntity)

    override suspend fun deleteSongInHistory(songId: Long) {
        historyDao.deleteSongInHistory(songId)
    }

    override suspend fun clearSongHistory() {
        historyDao.clearHistory()
    }

    override suspend fun findSongExistInPlayCount(songId: Long): PlayCountEntity? =
        playCountDao.findSongExistInPlayCount(songId)

    override suspend fun playCountSongs(): List<PlayCountEntity> =
        playCountDao.playCountSongs()

    override suspend fun deleteSongs(songs: List<Song>) = songs.forEach {
        playCountDao.deleteSong(it.id)
    }

    override suspend fun isSongFavorite(context: Context, songId: Long): Boolean {
        return playlistDao.isSongExistsInPlaylist(
            playlistDao.playlist(context.getString(R.string.favorites)).firstOrNull()?.playListId
                ?: -1,
            songId
        ).isNotEmpty()
    }

    @WorkerThread
    override fun getPlaylist(playlistId: Long): LiveData<PlaylistWithSongs> =
        playlistDao.getPlaylist(playlistId)


}