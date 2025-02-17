package code.name.monkey.retromusic.db

import androidx.room.Database
import androidx.room.RoomDatabase
import code.name.monkey.retromusic.db.dao.HistoryDao
import code.name.monkey.retromusic.db.dao.PlayCountDao
import code.name.monkey.retromusic.db.dao.PlaylistDao

@Database(
    entities = [PlaylistEntity::class, SongEntity::class, HistoryEntity::class, PlayCountEntity::class],
    version = 24,
    exportSchema = false
)
abstract class RetroDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun playCountDao(): PlayCountDao
    abstract fun historyDao(): HistoryDao
}
