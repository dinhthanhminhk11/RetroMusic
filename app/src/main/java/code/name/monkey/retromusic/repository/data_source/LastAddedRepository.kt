
package code.name.monkey.retromusic.repository.data_source

import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.Song


interface LastAddedRepository {
    fun recentSongs(): List<Song>

    fun recentAlbums(): List<Album>

    fun recentArtists(): List<Artist>
}
