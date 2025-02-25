package code.name.monkey.retromusic.repository.data_source_impl

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import code.name.monkey.retromusic.Constants
import code.name.monkey.retromusic.extensions.getLong
import code.name.monkey.retromusic.extensions.getStringOrNull
import code.name.monkey.retromusic.model.Genre
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.repository.data_source.GenreRepository
import code.name.monkey.retromusic.repository.data_source.SongRepository
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.makeSongCursor

class GenreRepositoryImpl(
    private val context: Context,
    private val songRepository: SongRepository
) : GenreRepository {

    override fun genres(query: String): List<Genre> {
        return getGenresFromCursor(makeGenreCursor(query))
    }

    override fun genres(): List<Genre> {
        return getGenresFromCursor(makeGenreCursor())
    }

    override fun songs(genreId: Long): List<Song> {
        // The genres table only stores songs that have a genre specified,
        // so we need to get songs without a genre a different way.
        return if (genreId == -1L) {
            getSongsWithNoGenre()
        } else songRepository.songs(makeGenreSongCursor(genreId))
    }

    override fun song(genreId: Long): Song {
        return songRepository.song(makeGenreSongCursor(genreId))
    }

    private fun getSongCount(genreId: Long): Int {
        context.contentResolver.query(
            MediaStore.Audio.Genres.Members.getContentUri("external", genreId),
            null,
            null,
            null,
            null
        ).use {
            return it?.count ?: 0
        }
    }

    private fun getGenreFromCursor(cursor: Cursor): Genre {
        val id = cursor.getLong(MediaStore.Audio.Genres._ID)
        val name = cursor.getStringOrNull(MediaStore.Audio.Genres.NAME)
        val songCount = getSongCount(id)
        return Genre(id, name ?: "", songCount)
    }

    private fun getSongsWithNoGenre(): List<Song> {
        val selection =
            BaseColumns._ID + " NOT IN " + "(SELECT " + MediaStore.Audio.Genres.Members.AUDIO_ID + " FROM audio_genres_map)"
        return songRepository.songs(makeSongCursor(context, selection, null))
    }

    private fun makeGenreSongCursor(genreId: Long): Cursor? {
        return try {
            context.contentResolver.query(
                MediaStore.Audio.Genres.Members.getContentUri("external", genreId),
                Constants.baseProjection,
                Constants.IS_MUSIC,
                null,
                PreferenceUtil.songSortOrder
            )
        } catch (e: SecurityException) {
            return null
        }
    }

    private fun getGenresFromCursor(cursor: Cursor?): ArrayList<Genre> {
        val genres = arrayListOf<Genre>()
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    val genre = getGenreFromCursor(cursor)
                    if (genre.songCount > 0) {
                        genres.add(genre)
                    }
                } while (cursor.moveToNext())
            }
        }
        return genres
    }

    private fun makeGenreCursor(): Cursor? {
        val projection = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
        return try {
            context.contentResolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                PreferenceUtil.genreSortOrder
            )
        } catch (e: SecurityException) {
            return null
        }
    }

    private fun makeGenreCursor(query: String): Cursor? {
        val projection = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
        return try {
            context.contentResolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Audio.Genres.NAME + " = ?",
                arrayOf(query),
                PreferenceUtil.genreSortOrder
            )
        } catch (e: SecurityException) {
            return null
        }
    }
}
