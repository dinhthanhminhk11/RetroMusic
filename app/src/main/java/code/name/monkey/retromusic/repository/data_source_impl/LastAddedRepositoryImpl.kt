package code.name.monkey.retromusic.repository.data_source_impl

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.repository.data_source.AlbumRepository
import code.name.monkey.retromusic.repository.data_source.ArtistRepository
import code.name.monkey.retromusic.repository.data_source.LastAddedRepository
import code.name.monkey.retromusic.repository.data_source.SongRepository
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.makeSongCursor
import code.name.monkey.retromusic.util.splitIntoAlbums
import code.name.monkey.retromusic.util.splitIntoArtists

class LastAddedRepositoryImpl(
    private val context: Context,
    private val songRepository: SongRepository
) : LastAddedRepository {
    override fun recentSongs(): List<Song> {
        return songRepository.songs(makeLastAddedCursor())
    }

    override fun recentAlbums(): List<Album> {
        return splitIntoAlbums(recentSongs(), sorted = false)
    }

    override fun recentArtists(): List<Artist> {
        return splitIntoArtists(recentAlbums())
    }

    private fun makeLastAddedCursor(): Cursor? {
        val cutoff = PreferenceUtil.lastAddedCutoff
        return makeSongCursor(
            context,
            MediaStore.Audio.Media.DATE_ADDED + ">?",
            arrayOf(cutoff.toString()),
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )
    }
}
