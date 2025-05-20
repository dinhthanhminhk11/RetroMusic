package code.name.monkey.retromusic.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import code.name.monkey.retromusic.FAVOURITES
import code.name.monkey.retromusic.GENRES
import code.name.monkey.retromusic.PLAYLISTS
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.RECENT_ALBUMS
import code.name.monkey.retromusic.RECENT_ARTISTS
import code.name.monkey.retromusic.SuccessResponse
import code.name.monkey.retromusic.TOP_ALBUMS
import code.name.monkey.retromusic.TOP_ARTISTS
import code.name.monkey.retromusic.db.HistoryEntity
import code.name.monkey.retromusic.db.PlayCountEntity
import code.name.monkey.retromusic.db.PlaylistEntity
import code.name.monkey.retromusic.db.PlaylistWithSongs
import code.name.monkey.retromusic.db.SongEntity
import code.name.monkey.retromusic.db.fromHistoryToSongs
import code.name.monkey.retromusic.db.toSong
import code.name.monkey.retromusic.extensions.responseToResource
import code.name.monkey.retromusic.extensions.responseToResourceProtobufFlow
import code.name.monkey.retromusic.fragments.search.Filter
import code.name.monkey.retromusic.model.AbsCustomPlaylist
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.BodyRequest
import code.name.monkey.retromusic.model.Contributor
import code.name.monkey.retromusic.model.Genre
import code.name.monkey.retromusic.model.Home
import code.name.monkey.retromusic.model.Playlist
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.model.smartplaylist.NotPlayedPlaylist
import code.name.monkey.retromusic.network.LastFMService
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.network.model.LastFmAlbum
import code.name.monkey.retromusic.network.model.LastFmArtist
import code.name.monkey.retromusic.network.model.response.auth.ResponseDataAuth
import code.name.monkey.retromusic.network.model.response.file.ResponseFile
import code.name.monkey.retromusic.repository.data_source.AlbumRepository
import code.name.monkey.retromusic.repository.data_source.ArtistRepository
import code.name.monkey.retromusic.repository.data_source.GenreRepository
import code.name.monkey.retromusic.repository.data_source.LastAddedRepository
import code.name.monkey.retromusic.repository.data_source.LocalDataRepository
import code.name.monkey.retromusic.repository.data_source.PlaylistRepository
import code.name.monkey.retromusic.repository.data_source.RoomRepository
import code.name.monkey.retromusic.repository.data_source.SearchRepository
import code.name.monkey.retromusic.repository.data_source.SongRepository
import code.name.monkey.retromusic.repository.data_source.TopPlayedRepository
import code.name.monkey.retromusic.repository.data_source.network.AuthRepository
import code.name.monkey.retromusic.repository.data_source.network.SongRemoteRepository
import code.name.monkey.retromusic.util.logE
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class RepositoryImpl(
    private val context: Context,
    private val lastFMService: LastFMService,
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val genreRepository: GenreRepository,
    private val lastAddedRepository: LastAddedRepository,
    private val playlistRepository: PlaylistRepository,
    private val searchRepository: SearchRepository,
    private val topPlayedRepository: TopPlayedRepository,
    private val roomRepository: RoomRepository,
    private val localDataRepository: LocalDataRepository,
    private val authRepository: AuthRepository,
    private val songRemoteRepository: SongRemoteRepository
) : Repository {

    override suspend fun deleteSongs(songs: List<Song>) = roomRepository.deleteSongs(songs)

    override suspend fun contributor(): List<Contributor> = localDataRepository.contributors()

    override suspend fun searchSongs(query: String): List<Song> = songRepository.songs(query)

    override suspend fun searchAlbums(query: String): List<Album> = albumRepository.albums(query)

    override suspend fun isSongFavorite(songId: Long): Boolean =
        roomRepository.isSongFavorite(context, songId)

    override fun getSongByGenre(genreId: Long): Song = genreRepository.song(genreId)

    override suspend fun searchArtists(query: String): List<Artist> =
        artistRepository.artists(query)

    override suspend fun fetchAlbums(): List<Album> = albumRepository.albums()

    override suspend fun albumByIdAsync(albumId: Long): Album = albumRepository.album(albumId)

    override fun albumById(albumId: Long): Album = albumRepository.album(albumId)

    override suspend fun fetchArtists(): List<Artist> = artistRepository.artists()

    override suspend fun albumArtists(): List<Artist> = artistRepository.albumArtists()

    override suspend fun artistById(artistId: Long): Artist = artistRepository.artist(artistId)

    override suspend fun albumArtistByName(name: String): Artist =
        artistRepository.albumArtist(name)

    override suspend fun recentArtists(): List<Artist> = lastAddedRepository.recentArtists()

    override suspend fun recentAlbums(): List<Album> = lastAddedRepository.recentAlbums()

    override suspend fun topArtists(): List<Artist> = topPlayedRepository.topArtists()

    override suspend fun topAlbums(): List<Album> = topPlayedRepository.topAlbums()

    override suspend fun fetchLegacyPlaylist(): List<Playlist> = playlistRepository.playlists()

    override suspend fun fetchGenres(): List<Genre> = genreRepository.genres()

    override suspend fun allSongs(): List<Song> = songRepository.songs()

    override suspend fun search(query: String?, filter: Filter): MutableList<Any> =
        searchRepository.searchAll(context, query, filter)

    override suspend fun getPlaylistSongs(playlist: Playlist): List<Song> =
        if (playlist is AbsCustomPlaylist) {
            playlist.songs()
        } else {
            PlaylistSongsLoader.getPlaylistSongList(context, playlist.id)
        }

    override suspend fun getGenre(genreId: Long): List<Song> = genreRepository.songs(genreId)

    override suspend fun artistInfo(
        name: String,
        lang: String?,
        cache: String?,
    ): Result<LastFmArtist> {
        return try {
            Result.Success(lastFMService.artistInfo(name, lang, cache))
        } catch (e: Exception) {
            logE(e)
            Result.Error(error = e)
        }
    }

    override suspend fun albumInfo(
        artist: String,
        album: String,
    ): Result<LastFmAlbum> {
        return try {
            val lastFmAlbum = lastFMService.albumInfo(artist, album)
            Result.Success(lastFmAlbum)
        } catch (e: Exception) {
            logE(e)
            Result.Error(error = e)
        }
    }

    override suspend fun homeSections(): List<Home> {
        val homeSections = mutableListOf<Home>()
        val sections: List<Home> = listOf(
            topArtistsHome(),
            topAlbumsHome(),
            recentArtistsHome(),
            recentAlbumsHome(),
            favoritePlaylistHome()
        )
        for (section in sections) {
            if (section.arrayList.isNotEmpty()) {
                homeSections.add(section)
            }
        }
        return homeSections
    }


    override suspend fun playlist(playlistId: Long) = playlistRepository.playlist(playlistId)

    override suspend fun fetchPlaylistWithSongs(): List<PlaylistWithSongs> =
        roomRepository.playlistWithSongs()

    override fun getPlaylist(playlistId: Long): LiveData<PlaylistWithSongs> =
        roomRepository.getPlaylist(playlistId)

    override fun login(reqLogin: RequestBody): Flow<Result<SuccessResponse>> =
        responseToResourceProtobufFlow(
            call = { authRepository.login(reqLogin) }, SuccessResponse.ADAPTER
        )

    override fun logout(reqLogin: RequestBody): Flow<Result<SuccessResponse>> =
        responseToResourceProtobufFlow(
            call = { authRepository.logout(reqLogin) }, SuccessResponse.ADAPTER
        )

    override fun register(requestBody: RequestBody): Flow<Result<SuccessResponse>> {
        return responseToResourceProtobufFlow(
            { authRepository.register(requestBody) }, SuccessResponse.ADAPTER
        )
    }

    override fun verifyOtp(reqLogin: RequestBody): Flow<Result<SuccessResponse>> =
        responseToResourceProtobufFlow(
            call = { authRepository.verifyOtp(reqLogin) }, SuccessResponse.ADAPTER
        )

    override fun reSentOtp(reqLogin: RequestBody): Flow<Result<SuccessResponse>> =
        responseToResourceProtobufFlow(
            call = { authRepository.reSentOtp(reqLogin) },
            SuccessResponse.ADAPTER
        )

    override fun setPassword(reqLogin: RequestBody): Flow<Result<SuccessResponse>> =
        responseToResourceProtobufFlow(
            call = { authRepository.setPassword(reqLogin) },
            SuccessResponse.ADAPTER
        )

    override fun checkAccount(reqLogin: RequestBody): Flow<Result<SuccessResponse>> =
        responseToResourceProtobufFlow(
            call = { authRepository.checkAccount(reqLogin) }, SuccessResponse.ADAPTER
        )

    override fun loginByToken(token: String): Flow<Result<SuccessResponse>> =
        responseToResourceProtobufFlow(
            call = { authRepository.loginByToken(token) },
            SuccessResponse.ADAPTER
        )

    override suspend fun updateUserInfo(
        token: String,
        data: RequestBody?,
        image: MultipartBody.Part?,
        imageBanner: MultipartBody.Part?
    ): Result<ResponseDataAuth> = responseToResource(
        authRepository.updateUserInfo(
            token, data, image, imageBanner
        )
    )

    override suspend fun checkFile(hashFile: String): Result<ResponseFile> =
        responseToResource(songRemoteRepository.checkFile(hashFile))

    override suspend fun uploadChunk(
        fileHash: String, chunkIndex: Int, file: MultipartBody.Part
    ): Result<Unit> =
        responseToResource(songRemoteRepository.uploadChunk(fileHash, chunkIndex, file))

    override suspend fun mergeFile(fileHash: BodyRequest): Result<Unit> =
        responseToResource(songRemoteRepository.mergeFile(fileHash))


    override suspend fun playlistSongs(playlistWithSongs: PlaylistWithSongs): List<Song> =
        playlistWithSongs.songs.map {
            it.toSong()
        }

    override fun playlistSongs(playListId: Long): LiveData<List<SongEntity>> =
        roomRepository.getSongs(playListId)

    override suspend fun insertSongs(songs: List<SongEntity>) = roomRepository.insertSongs(songs)

    override suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity> =
        roomRepository.checkPlaylistExists(playlistName)

    override fun checkPlaylistExists(playListId: Long): LiveData<Boolean> =
        roomRepository.checkPlaylistExists(playListId)

    override suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long =
        roomRepository.createPlaylist(playlistEntity)

    override suspend fun fetchPlaylists(): List<PlaylistEntity> = roomRepository.playlists()

    override suspend fun deleteRoomPlaylist(playlists: List<PlaylistEntity>) =
        roomRepository.deletePlaylistEntities(playlists)

    override suspend fun renameRoomPlaylist(playlistId: Long, name: String) =
        roomRepository.renamePlaylistEntity(playlistId, name)

    override suspend fun deleteSongsInPlaylist(songs: List<SongEntity>) =
        roomRepository.deleteSongsInPlaylist(songs)

    override suspend fun removeSongFromPlaylist(songEntity: SongEntity) =
        roomRepository.removeSongFromPlaylist(songEntity)

    override suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>) =
        roomRepository.deletePlaylistSongs(playlists)

    override suspend fun favoritePlaylist(): PlaylistEntity =
        roomRepository.favoritePlaylist(context.getString(R.string.favorites))

    override suspend fun isFavoriteSong(songEntity: SongEntity): List<SongEntity> =
        roomRepository.isFavoriteSong(songEntity)

    override suspend fun upsertSongInHistory(currentSong: Song) =
        roomRepository.upsertSongInHistory(currentSong)

    override suspend fun favoritePlaylistSongs(): List<SongEntity> =
        roomRepository.favoritePlaylistSongs(context.getString(R.string.favorites))

    override suspend fun recentSongs(): List<Song> = lastAddedRepository.recentSongs()

    override suspend fun topPlayedSongs(): List<Song> = topPlayedRepository.topTracks()

    override suspend fun upsertSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.upsertSongInPlayCount(playCountEntity)

    override suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.deleteSongInPlayCount(playCountEntity)

    override suspend fun deleteSongInHistory(songId: Long) =
        roomRepository.deleteSongInHistory(songId)

    override suspend fun clearSongHistory() {
        roomRepository.clearSongHistory()
    }

    override suspend fun findSongExistInPlayCount(songId: Long): PlayCountEntity? =
        roomRepository.findSongExistInPlayCount(songId)

    override suspend fun playCountSongs(): List<PlayCountEntity> = roomRepository.playCountSongs()

    override fun observableHistorySongs(): LiveData<List<Song>> =
        roomRepository.observableHistorySongs().map {
            it.fromHistoryToSongs()
        }

    override fun historySong(): List<HistoryEntity> = roomRepository.historySongs()

    override fun favorites(): LiveData<List<SongEntity>> =
        roomRepository.favoritePlaylistLiveData(context.getString(R.string.favorites))

    override suspend fun suggestions(): List<Song> {
        return NotPlayedPlaylist().songs().shuffled().takeIf {
            it.size > 9
        } ?: emptyList()
    }

    override suspend fun genresHome(): Home {
        val genres = genreRepository.genres().shuffled()
        return Home(genres, GENRES, R.string.genres)
    }

    override suspend fun playlists(): Home {
        val playlist = playlistRepository.playlists()
        return Home(playlist, PLAYLISTS, R.string.playlists)
    }

    override suspend fun recentArtistsHome(): Home {
        val artists = lastAddedRepository.recentArtists().take(5)
        return Home(artists, RECENT_ARTISTS, R.string.recent_artists)
    }

    override suspend fun recentAlbumsHome(): Home {
        val albums = lastAddedRepository.recentAlbums().take(5)
        return Home(albums, RECENT_ALBUMS, R.string.recent_albums)
    }

    override suspend fun topAlbumsHome(): Home {
        val albums = topPlayedRepository.topAlbums().take(5)
        return Home(albums, TOP_ALBUMS, R.string.top_albums)
    }

    override suspend fun topArtistsHome(): Home {
        val artists = topPlayedRepository.topArtists().take(5)
        return Home(artists, TOP_ARTISTS, R.string.top_artists)
    }

    override suspend fun favoritePlaylistHome(): Home {
        val songs = favoritePlaylistSongs().map {
            it.toSong()
        }
        return Home(songs, FAVOURITES, R.string.favorites)
    }
}