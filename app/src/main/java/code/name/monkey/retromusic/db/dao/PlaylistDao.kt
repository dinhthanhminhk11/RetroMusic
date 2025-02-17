package code.name.monkey.retromusic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import code.name.monkey.retromusic.db.PlaylistEntity


@Dao
interface PlaylistDao {
    @Insert
    suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long
}