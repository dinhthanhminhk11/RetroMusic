package code.name.monkey.retromusic.core.di

import android.content.Context
import code.name.monkey.retromusic.db.dao.HistoryDao
import code.name.monkey.retromusic.db.dao.PlayCountDao
import code.name.monkey.retromusic.db.dao.PlaylistDao
import code.name.monkey.retromusic.repository.Repository
import code.name.monkey.retromusic.repository.RepositoryImpl
import code.name.monkey.retromusic.repository.dataSource.AlbumLocalRepository
import code.name.monkey.retromusic.repository.dataSource.ArtistLocalRepository
import code.name.monkey.retromusic.repository.dataSource.GenreLocalRepository
import code.name.monkey.retromusic.repository.dataSource.LastAddedLocalRepository
import code.name.monkey.retromusic.repository.dataSource.PlaylistLocalRepository
import code.name.monkey.retromusic.repository.dataSource.RoomRepository
import code.name.monkey.retromusic.repository.dataSource.SearchLocalRepository
import code.name.monkey.retromusic.repository.dataSource.SongLocalRepository
import code.name.monkey.retromusic.repository.dataSource.TopPlayedLocalRepository
import code.name.monkey.retromusic.repository.dataSourceImpl.AlbumLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.ArtistLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.GenreLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.LastAddedLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.PlaylistLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.RoomRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.SearchLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.SongLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.TopPlayedLocalRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideSongLocalRepository(
        @ApplicationContext context: Context
    ): SongLocalRepository {
        return SongLocalRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideAlbumLocalRepository(
        @ApplicationContext context: Context,
        songRepository: SongLocalRepository
    ): AlbumLocalRepository {
        return AlbumLocalRepositoryImpl(context, songRepository)
    }

    @Singleton
    @Provides
    fun provideGenreLocalRepository(
        @ApplicationContext context: Context,
        songRepository: SongLocalRepository
    ): GenreLocalRepository {
        return GenreLocalRepositoryImpl(context, songRepository)
    }

    @Singleton
    @Provides
    fun provideArtistLocalRepository(
        @ApplicationContext context: Context,
        songRepository: SongLocalRepositoryImpl
    ): ArtistLocalRepository {
        return ArtistLocalRepositoryImpl(context, songRepository)
    }

    @Singleton
    @Provides
    fun providePlayListLocalRepository(
        @ApplicationContext context: Context
    ): PlaylistLocalRepository {
        return PlaylistLocalRepositoryImpl(context.contentResolver)
    }

    @Singleton
    @Provides
    fun provideTopPlayerLocalRepository(
        @ApplicationContext context: Context,
        songRepository: SongLocalRepository
    ): TopPlayedLocalRepository {
        return TopPlayedLocalRepositoryImpl(
            context,
            songRepository
        )
    }

    @Singleton
    @Provides
    fun provideRoomRepository(
        playlistDao: PlaylistDao,
        playCountDao: PlayCountDao,
        historyDao: HistoryDao
    ): RoomRepository {
        return RoomRepositoryImpl(
            playlistDao, playCountDao, historyDao
        )
    }

    @Singleton
    @Provides
    fun provideSearchLocalRepository(
        songRepository: SongLocalRepository,
        albumRepository: AlbumLocalRepository,
        artistRepository: ArtistLocalRepository,
        roomRepository: RoomRepository,
        genreRepository: GenreLocalRepository
    ): SearchLocalRepository {
        return SearchLocalRepositoryImpl(
            songRepository, albumRepository, artistRepository, roomRepository, genreRepository
        )
    }

    @Singleton
    @Provides
    fun provideLastAddedLocalRepository(
        @ApplicationContext context: Context,
        songRepository: SongLocalRepositoryImpl
    ): LastAddedLocalRepository {
        return LastAddedLocalRepositoryImpl(
            context, songRepository
        )
    }

    @Singleton
    @Provides
    fun provideRepository(
        @ApplicationContext context: Context,
        songRepository: SongLocalRepository,
        albumRepository: AlbumLocalRepository,
        artistRepository: ArtistLocalRepository,
        genreRepository: GenreLocalRepository,
        lastAddedRepository: LastAddedLocalRepository,
        playlistRepository: PlaylistLocalRepository,
        searchRepository: SearchLocalRepository,
        topPlayedRepository: TopPlayedLocalRepository,
        roomRepository: RoomRepository
    ): Repository {
        return RepositoryImpl(
            context,
            songRepository,
            albumRepository,
            artistRepository,
            genreRepository,
            lastAddedRepository,
            playlistRepository,
            searchRepository,
            topPlayedRepository,
            roomRepository
        )
    }

}