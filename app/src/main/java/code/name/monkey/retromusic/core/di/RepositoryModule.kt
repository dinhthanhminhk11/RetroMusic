package code.name.monkey.retromusic.core.di

import android.content.Context
import code.name.monkey.retromusic.repository.dataSource.AlbumLocalRepository
import code.name.monkey.retromusic.repository.dataSource.ArtistLocalRepository
import code.name.monkey.retromusic.repository.dataSource.GenreLocalRepository
import code.name.monkey.retromusic.repository.dataSource.SongLocalRepository
import code.name.monkey.retromusic.repository.dataSourceImpl.AlbumLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.ArtistLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.GenreLocalRepositoryImpl
import code.name.monkey.retromusic.repository.dataSourceImpl.SongLocalRepositoryImpl
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

}