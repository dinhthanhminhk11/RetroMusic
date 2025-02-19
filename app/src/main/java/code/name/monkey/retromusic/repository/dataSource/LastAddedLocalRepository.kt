package code.name.monkey.retromusic.repository.dataSource

import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.Song

interface LastAddedLocalRepository {
    fun recentSongs(): List<Song>

    fun recentAlbums(): List<Album>

    fun recentArtists(): List<Artist>
}