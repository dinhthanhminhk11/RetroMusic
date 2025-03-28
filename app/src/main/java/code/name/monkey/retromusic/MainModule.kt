package code.name.monkey.retromusic

import android.content.Context
import androidx.room.Room
import code.name.monkey.retromusic.auto.AutoMusicProvider
import code.name.monkey.retromusic.cast.RetroWebServer
import code.name.monkey.retromusic.db.MIGRATION_23_24
import code.name.monkey.retromusic.db.RetroDatabase
import code.name.monkey.retromusic.fragments.LibraryViewModel
import code.name.monkey.retromusic.fragments.albums.AlbumDetailsViewModel
import code.name.monkey.retromusic.fragments.artists.ArtistDetailsViewModel
import code.name.monkey.retromusic.fragments.auth.login.LoginViewModel
import code.name.monkey.retromusic.fragments.auth.otp.OtpViewModel
import code.name.monkey.retromusic.fragments.auth.register.RegisterViewModel
import code.name.monkey.retromusic.fragments.auth.setpass.SetPassViewModel
import code.name.monkey.retromusic.fragments.genres.GenreDetailsViewModel
import code.name.monkey.retromusic.fragments.other.UserInfoViewModel
import code.name.monkey.retromusic.fragments.playlists.PlaylistDetailsViewModel
import code.name.monkey.retromusic.fragments.settings.MainSettingsViewModel
import code.name.monkey.retromusic.fragments.upload.FileUploader
import code.name.monkey.retromusic.fragments.upload.UploadViewModel
import code.name.monkey.retromusic.model.Genre
import code.name.monkey.retromusic.network.provideAuthService
import code.name.monkey.retromusic.network.provideDefaultCache
import code.name.monkey.retromusic.network.provideLastFmRest
import code.name.monkey.retromusic.network.provideLastFmRetrofit
import code.name.monkey.retromusic.network.provideOkHttp
import code.name.monkey.retromusic.network.provideOkHttpLoginProtobuf
import code.name.monkey.retromusic.network.provideRetrofitLoginProtobuf
import code.name.monkey.retromusic.network.provideSongRemoteService
import code.name.monkey.retromusic.repository.Repository
import code.name.monkey.retromusic.repository.RepositoryImpl
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
import code.name.monkey.retromusic.repository.data_source_impl.AlbumRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.ArtistRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.GenreRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.LastAddedRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.LocalDataRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.PlaylistRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.RoomRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.SearchRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.SongRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.TopPlayedRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.network.AuthRepositoryImpl
import code.name.monkey.retromusic.repository.data_source_impl.network.SongRemoteRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {

    factory {
        provideDefaultCache()
    }
    factory {
        provideOkHttp(get(), get())
    }
    single {
        provideLastFmRetrofit(get())
    }
    single {
        provideLastFmRest(get())
    }
}

val networkRetroSeverLoginModule = module {
    factory {
        provideDefaultCache()
    }
    factory {
        provideOkHttpLoginProtobuf(get(), get())
    }
    single {
        provideRetrofitLoginProtobuf(get())
    }
    single {
        provideAuthService(get())
    }

    single {
        provideSongRemoteService(get())
    }
}

private val roomModule = module {

    single {
        Room.databaseBuilder(androidContext(), RetroDatabase::class.java, "playlist.db")
            .addMigrations(MIGRATION_23_24)
            .build()
    }

    factory {
        get<RetroDatabase>().playlistDao()
    }

    factory {
        get<RetroDatabase>().playCountDao()
    }

    factory {
        get<RetroDatabase>().historyDao()
    }

    single {
        RoomRepositoryImpl(get(), get(), get())
    } bind RoomRepository::class
}
private val autoModule = module {
    single {
        AutoMusicProvider(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
private val mainModule = module {
    single {
        androidContext().contentResolver
    }
    single {
        RetroWebServer(get())
    }
}
private val dataModule = module {
    single {
        RepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    } bind Repository::class

    single {
        SongRepositoryImpl(get())
    } bind SongRepository::class

    single {
        GenreRepositoryImpl(get(), get())
    } bind GenreRepository::class

    single {
        AlbumRepositoryImpl(get(), get())
    } bind AlbumRepository::class

    single {
        ArtistRepositoryImpl(get(), get())
    } bind ArtistRepository::class

    single {
        PlaylistRepositoryImpl(get())
    } bind PlaylistRepository::class

    single {
        TopPlayedRepositoryImpl(get(), get())
    } bind TopPlayedRepository::class

    single {
        LastAddedRepositoryImpl(
            get(),
            get()
        )
    } bind LastAddedRepository::class

    single {
        SearchRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    } bind SearchRepository::class
    single {
        LocalDataRepositoryImpl(get())
    } bind LocalDataRepository::class

    single {
        AuthRepositoryImpl(get())
    } bind AuthRepository::class

    single {
        SongRemoteRepositoryImpl(get())
    } bind SongRemoteRepository::class

    factory { (context: Context) -> FileUploader(context, get()) }

}

private val viewModules = module {

    viewModel {
        LibraryViewModel(get())
    }

    viewModel { (albumId: Long) ->
        AlbumDetailsViewModel(
            get(),
            albumId
        )
    }

    viewModel { (artistId: Long?, artistName: String?) ->
        ArtistDetailsViewModel(
            get(),
            artistId,
            artistName
        )
    }

    viewModel { (playlistId: Long) ->
        PlaylistDetailsViewModel(
            get(),
            playlistId
        )
    }

    viewModel { (genre: Genre) ->
        GenreDetailsViewModel(
            get(),
            genre
        )
    }

    viewModel {
        LoginViewModel(get())
    }

    viewModel {
        RegisterViewModel(get())
    }

    viewModel {
        OtpViewModel(get())
    }

    viewModel {
        SetPassViewModel(get())
    }

    viewModel {
        MainSettingsViewModel(get())
    }

    viewModel {
        UserInfoViewModel(get())
    }

    viewModel {
        UploadViewModel(get())
    }
}

val appModules = listOf(
    mainModule,
    dataModule,
    autoModule,
    viewModules,
    networkModule,
    networkRetroSeverLoginModule,
    roomModule
)