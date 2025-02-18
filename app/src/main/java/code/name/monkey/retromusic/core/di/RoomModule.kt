package code.name.monkey.retromusic.core.di

import android.content.Context
import androidx.room.Room
import code.name.monkey.retromusic.db.MIGRATION_23_24
import code.name.monkey.retromusic.db.RetroDatabase
import code.name.monkey.retromusic.db.dao.HistoryDao
import code.name.monkey.retromusic.db.dao.PlayCountDao
import code.name.monkey.retromusic.db.dao.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RetroDatabase {
        return Room.databaseBuilder(
            context,
            RetroDatabase::class.java,
            "playlist.db"
        ).addMigrations(MIGRATION_23_24)
            .build()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: RetroDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun providePlayCountDao(database: RetroDatabase): PlayCountDao {
        return database.playCountDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: RetroDatabase): PlaylistDao {
        return database.playlistDao()
    }

}