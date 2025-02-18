package code.name.monkey.retromusic.repository

import code.name.monkey.retromusic.db.HistoryEntity
import code.name.monkey.retromusic.db.PlayCountEntity

class RepositoryImpl : Repository {
    override fun historySong(): List<HistoryEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertSongInPlayCount(playCountEntity: PlayCountEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity) {
        TODO("Not yet implemented")
    }
}