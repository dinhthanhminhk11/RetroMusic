package code.name.monkey.retromusic.repository.dataSource

import code.name.monkey.retromusic.model.Genre
import code.name.monkey.retromusic.model.Song

interface GenreLocalRepository {
    fun genres(query: String): List<Genre>

    fun genres(): List<Genre>

    fun songs(genreId: Long): List<Song>

    fun song(genreId: Long): Song
}