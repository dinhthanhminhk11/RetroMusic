package code.name.monkey.retromusic.repository.dataSourceImpl

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.repository.dataSource.LastAddedLocalRepository
import code.name.monkey.retromusic.repository.dataSource.SongLocalRepository
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.makeSongCursor
import code.name.monkey.retromusic.util.splitIntoAlbums
import code.name.monkey.retromusic.util.splitIntoArtists

class LastAddedLocalRepositoryImpl(
    private val context: Context,
    private val songRepository: SongLocalRepository
) : LastAddedLocalRepository {
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