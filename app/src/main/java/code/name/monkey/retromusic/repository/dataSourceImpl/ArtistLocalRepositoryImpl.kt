package code.name.monkey.retromusic.repository.dataSourceImpl

import android.content.Context
import android.provider.MediaStore
import code.name.monkey.retromusic.ALBUM_ARTIST
import code.name.monkey.retromusic.helper.SortOrder
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.repository.dataSource.ArtistLocalRepository
import code.name.monkey.retromusic.repository.dataSource.SongLocalRepository
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.makeSongCursor
import code.name.monkey.retromusic.util.splitIntoAlbums
import code.name.monkey.retromusic.util.splitIntoArtists
import java.text.Collator
import javax.inject.Inject

class ArtistLocalRepositoryImpl @Inject constructor(
    private val context: Context,
    private val songRepository: SongLocalRepository,
) : ArtistLocalRepository {
    private fun getSongLoaderSortOrder(): String {
        return PreferenceUtil.artistSortOrder + ", " +
                PreferenceUtil.artistAlbumSortOrder + ", " +
                PreferenceUtil.artistSongSortOrder
    }

    override fun artist(artistId: Long): Artist {
        if (artistId == Artist.VARIOUS_ARTISTS_ID) {
            // Get Various Artists
            val songs = songRepository.songs(
                makeSongCursor(
                    context,
                    null,
                    null,
                    getSongLoaderSortOrder()
                )
            )
            val albums = splitIntoAlbums(songs)
                .filter { it.albumArtist == Artist.VARIOUS_ARTISTS_DISPLAY_NAME }
            return Artist(Artist.VARIOUS_ARTISTS_ID, albums)
        }

        val songs = songRepository.songs(
            makeSongCursor(
                context,
                MediaStore.Audio.AudioColumns.ARTIST_ID + "=?",
                arrayOf(artistId.toString()),
                getSongLoaderSortOrder()
            )
        )
        return Artist(artistId, splitIntoAlbums(songs))
    }

    override fun albumArtist(artistName: String): Artist {
        if (artistName == Artist.VARIOUS_ARTISTS_DISPLAY_NAME) {
            // Get Various Artists
            val songs = songRepository.songs(
                makeSongCursor(
                    context,
                    null,
                    null,
                    getSongLoaderSortOrder()
                )
            )
            val albums = splitIntoAlbums(songs)
                .filter { it.albumArtist == Artist.VARIOUS_ARTISTS_DISPLAY_NAME }
            return Artist(Artist.VARIOUS_ARTISTS_ID, albums, true)
        }

        val songs = songRepository.songs(
            makeSongCursor(
                context,
                "album_artist" + "=?",
                arrayOf(artistName),
                getSongLoaderSortOrder()
            )
        )
        return Artist(artistName, splitIntoAlbums(songs), true)
    }

    override fun artists(): List<Artist> {
        val songs = songRepository.songs(
            makeSongCursor(
                context,
                null, null,
                getSongLoaderSortOrder()
            )
        )
        val artists = splitIntoArtists(splitIntoAlbums(songs))
        return sortArtists(artists)
    }

    override fun albumArtists(): List<Artist> {
        val songs = songRepository.songs(
            makeSongCursor(
                context,
                null,
                null,
                "lower($ALBUM_ARTIST)" +
                        if (PreferenceUtil.artistSortOrder == SortOrder.ArtistSortOrder.ARTIST_A_Z) "" else " DESC"
            )
        )
        val artists = splitIntoAlbumArtists(splitIntoAlbums(songs))
        return sortArtists(artists)
    }

    override fun albumArtists(query: String): List<Artist> {
        val songs = songRepository.songs(
            makeSongCursor(
                context,
                "album_artist" + " LIKE ?",
                arrayOf("%$query%"),
                getSongLoaderSortOrder()
            )
        )
        val artists = splitIntoAlbumArtists(splitIntoAlbums(songs))
        return sortArtists(artists)
    }

    override fun artists(query: String): List<Artist> {
        val songs = songRepository.songs(
            makeSongCursor(
                context,
                MediaStore.Audio.AudioColumns.ARTIST + " LIKE ?",
                arrayOf("%$query%"),
                getSongLoaderSortOrder()
            )
        )
        val artists = splitIntoArtists(splitIntoAlbums(songs))
        return sortArtists(artists)
    }


    private fun splitIntoAlbumArtists(albums: List<Album>): List<Artist> {
        return albums.groupBy { it.albumArtist }
            .filter {
                !it.key.isNullOrEmpty()
            }
            .map {
                val currentAlbums = it.value
                if (currentAlbums.isNotEmpty()) {
                    if (currentAlbums[0].albumArtist == Artist.VARIOUS_ARTISTS_DISPLAY_NAME) {
                        Artist(Artist.VARIOUS_ARTISTS_ID, currentAlbums, true)
                    } else {
                        Artist(currentAlbums[0].artistId, currentAlbums, true)
                    }
                } else {
                    Artist.empty
                }
            }
    }


    private fun sortArtists(artists: List<Artist>): List<Artist> {
        val collator = Collator.getInstance()
        return when (PreferenceUtil.artistSortOrder) {
            SortOrder.ArtistSortOrder.ARTIST_A_Z -> {
                artists.sortedWith { a1, a2 -> collator.compare(a1.name, a2.name) }
            }

            SortOrder.ArtistSortOrder.ARTIST_Z_A -> {
                artists.sortedWith { a1, a2 -> collator.compare(a2.name, a1.name) }
            }

            else -> artists
        }
    }
}