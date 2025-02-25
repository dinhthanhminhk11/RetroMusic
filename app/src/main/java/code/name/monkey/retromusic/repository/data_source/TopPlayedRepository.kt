
package code.name.monkey.retromusic.repository.data_source

import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.Song

interface TopPlayedRepository {
    fun recentlyPlayedTracks(): List<Song>

    fun topTracks(): List<Song>

    fun notRecentlyPlayedTracks(): List<Song>

    fun topAlbums(): List<Album>

    fun topArtists(): List<Artist>
}
