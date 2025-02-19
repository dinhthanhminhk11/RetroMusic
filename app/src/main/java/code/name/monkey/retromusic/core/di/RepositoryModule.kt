package code.name.monkey.retromusic.core.di

import android.content.Context
import code.name.monkey.retromusic.db.dao.HistoryDao
import code.name.monkey.retromusic.db.dao.PlayCountDao
import code.name.monkey.retromusic.db.dao.PlaylistDao
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
        songRepository: SongLocalRepositoryImpl
    ): AlbumLocalRepository {
        return AlbumLocalRepositoryImpl(songRepository)
    }

    @Singleton
    @Provides
    fun provideGenreLocalRepository(
        @ApplicationContext context: Context,
        songRepository: SongLocalRepositoryImpl
    ): GenreLocalRepository {
        return GenreLocalRepositoryImpl(context.contentResolver, songRepository)
    }

    @Singleton
    @Provides
    fun provideArtistLocalRepository(
        songRepository: SongLocalRepositoryImpl,
        albumRepository: AlbumLocalRepositoryImpl
    ): ArtistLocalRepository {
        return ArtistLocalRepositoryImpl(songRepository, albumRepository)
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
        songRepository: SongLocalRepositoryImpl,
        albumRepository: AlbumLocalRepositoryImpl,
        artistRepository: ArtistLocalRepositoryImpl
    ): TopPlayedLocalRepository {
        return TopPlayedLocalRepositoryImpl(
            context,
            songRepository,
            albumRepository,
            artistRepository
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
        songRepository: SongLocalRepositoryImpl,
        albumRepository: AlbumLocalRepositoryImpl,
        artistRepository: ArtistLocalRepositoryImpl,
        roomRepository: RoomRepository,
        genreRepository: GenreLocalRepositoryImpl
    ): SearchLocalRepository {
        return SearchLocalRepositoryImpl(
            songRepository, albumRepository, artistRepository, roomRepository, genreRepository
        )
    }

    @Singleton
    @Provides
    fun provideLastAddedLocalRepository(
        songRepository: SongLocalRepositoryImpl,
        albumRepository: AlbumLocalRepositoryImpl,
        artistRepository: ArtistLocalRepositoryImpl
    ): LastAddedLocalRepository {
        return LastAddedLocalRepositoryImpl(
            songRepository, albumRepository, artistRepository
        )
    }


}