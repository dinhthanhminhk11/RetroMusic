package code.name.monkey.retromusic.model

import code.name.monkey.retromusic.repository.dataSource.LastAddedLocalRepository
import code.name.monkey.retromusic.repository.dataSource.SongLocalRepository
import code.name.monkey.retromusic.repository.dataSource.TopPlayedLocalRepository
import javax.inject.Inject

abstract class AbsCustomPlaylist(
    id: Long,
    name: String
) : Playlist(id, name) {

    @Inject
    protected lateinit var songRepository: SongLocalRepository

    @Inject
    protected lateinit var topPlayedRepository: TopPlayedLocalRepository

    @Inject
    protected lateinit var lastAddedRepository: LastAddedLocalRepository

    abstract fun songs(): List<Song>
}