package code.name.monkey.retromusic.db.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface PlayCountDao {
    @Query("DELETE FROM SongEntity WHERE id =:songId")
    fun deleteSong(songId: Long)
}