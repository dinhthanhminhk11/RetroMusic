package code.name.monkey.retromusic.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import code.name.monkey.retromusic.db.PlayCountEntity

@Dao
interface PlayCountDao {
    @Upsert
    fun upsertSongInPlayCount(playCountEntity: PlayCountEntity)

    @Delete
    fun deleteSongInPlayCount(playCountEntity: PlayCountEntity)

    @Query("SELECT * FROM PlayCountEntity WHERE id =:songId LIMIT 1")
    fun findSongExistInPlayCount(songId: Long): PlayCountEntity?

    @Query("SELECT * FROM PlayCountEntity ORDER BY play_count DESC")
    fun playCountSongs(): List<PlayCountEntity>

    @Query("DELETE FROM SongEntity WHERE id =:songId")
    fun deleteSong(songId: Long)
}