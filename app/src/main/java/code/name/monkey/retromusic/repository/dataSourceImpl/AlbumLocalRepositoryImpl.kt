package code.name.monkey.retromusic.repository.dataSourceImpl

import android.content.Context
import android.provider.MediaStore
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.repository.dataSource.AlbumLocalRepository
import code.name.monkey.retromusic.repository.dataSource.SongLocalRepository
import code.name.monkey.retromusic.util.getSongLoaderSortOrder
import code.name.monkey.retromusic.util.makeSongCursor
import code.name.monkey.retromusic.util.sortAlbumSongs
import code.name.monkey.retromusic.util.splitIntoAlbums
import javax.inject.Inject

class AlbumLocalRepositoryImpl @Inject constructor(
    private val context: Context,
    private val songRepository: SongLocalRepository
) : AlbumLocalRepository {
    override fun albums(): List<Album> {
        val songs = songRepository.songs(
            makeSongCursor(
                context,
                null,
                null,
                getSongLoaderSortOrder()
            )
        )
        return splitIntoAlbums(songs)
    }

    override fun albums(query: String): List<Album> {
        val songs = songRepository.songs(
            makeSongCursor(
                context,
                MediaStore.Audio.AudioColumns.ALBUM + " LIKE ?",
                arrayOf("%$query%"),
                getSongLoaderSortOrder()
            )
        )
        return splitIntoAlbums(songs)
    }

    override fun album(albumId: Long): Album {
        val cursor = makeSongCursor(
            context,
            MediaStore.Audio.AudioColumns.ALBUM_ID + "=?",
            arrayOf(albumId.toString()),
            getSongLoaderSortOrder()
        )
        val songs = songRepository.songs(cursor)
        val album = Album(albumId, songs)
        return sortAlbumSongs(album)
    }
}