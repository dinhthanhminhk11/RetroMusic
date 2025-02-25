
package code.name.monkey.retromusic.repository.data_source

import code.name.monkey.retromusic.model.Album


interface AlbumRepository {
    fun albums(): List<Album>

    fun albums(query: String): List<Album>

    fun album(albumId: Long): Album
}
