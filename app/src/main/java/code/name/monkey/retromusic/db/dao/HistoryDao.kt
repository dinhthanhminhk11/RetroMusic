package code.name.monkey.retromusic.db.dao

import androidx.room.Dao
import androidx.room.Upsert
import code.name.monkey.retromusic.db.HistoryEntity

@Dao
interface HistoryDao {
    companion object {
        private const val HISTORY_LIMIT = 100
    }
    @Upsert
    suspend fun upsertSongInHistory(historyEntity: HistoryEntity)
}