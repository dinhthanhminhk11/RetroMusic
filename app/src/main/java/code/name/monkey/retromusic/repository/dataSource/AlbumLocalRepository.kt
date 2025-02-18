package code.name.monkey.retromusic.repository.dataSource

import code.name.monkey.retromusic.model.Album

interface AlbumLocalRepository {
    fun albums(): List<Album>

    fun albums(query: String): List<Album>

    fun album(albumId: Long): Album
}