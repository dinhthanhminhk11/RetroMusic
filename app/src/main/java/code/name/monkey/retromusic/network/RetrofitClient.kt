package code.name.monkey.retromusic.network

import android.content.Context
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.BuildConfig
import code.name.monkey.retromusic.network.conversion.LyricsConverterFactory
import code.name.monkey.retromusic.util.Utility.getCountryCode
import code.name.monkey.retromusic.util.Utility.getDeviceId
import code.name.monkey.retromusic.util.Utility.getLanguageCode
import code.name.monkey.retromusic.util.Utility.getUserAgent
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.wire.WireConverterFactory
import java.io.File
import java.util.Collections
import java.util.concurrent.TimeUnit


fun provideDefaultCache(): Cache? {
    val cacheDir = File(App.getContext().cacheDir.absolutePath, "/okhttp-lastfm/")
    if (cacheDir.mkdirs() || cacheDir.isDirectory) {
        return Cache(cacheDir, 1024 * 1024 * 10)
    }
    return null
}

fun logInterceptor(): Interceptor {
    val loggingInterceptor = HttpLoggingInterceptor()
    if (BuildConfig.DEBUG) {
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    } else {
        // disable retrofit log on release
        loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
    }
    return loggingInterceptor
}

fun headerInterceptor(context: Context): Interceptor {
    return Interceptor {
        val original = it.request()
        val request = original.newBuilder()
            .header("User-Agent", context.packageName)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .method(original.method, original.body)
            .build()
        it.proceed(request)
    }
}

fun provideOkHttp(context: Context, cache: Cache): OkHttpClient {
    return OkHttpClient.Builder()
        .addNetworkInterceptor(logInterceptor())
        .addInterceptor(headerInterceptor(context))
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .cache(cache)
        .build()
}

fun provideLastFmRetrofit(client: OkHttpClient): Retrofit {
    val gson = GsonBuilder()
        .setLenient()
        .create()
    return Retrofit.Builder()
        .baseUrl("https://ws.audioscrobbler.com/2.0/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory { request -> client.newCall(request) }
        .build()
}

fun provideLastFmRest(retrofit: Retrofit): LastFMService {
    return retrofit.create(LastFMService::class.java)
}

fun headerInterceptorLoginProtobuf(
    deviceId: String,
    countryCode: String,
    languageCode: String
): Interceptor {
    return Interceptor { chain ->
        val request = chain.request()
            .newBuilder().apply {
                header("xinternalgatewayauthnotrequired", "true")
                header("user-agent", getUserAgent("Retro-Android", "8.79.1732129784"))
                header("Device-ID", deviceId)
                header("tg-loc-country", countryCode)
                header("tg-loc-language", languageCode)
            }.build()

        chain.proceed(request)
    }
}

fun provideOkHttpLoginProtobuf(context: Context, cache: Cache): OkHttpClient {
    val deviceId = getDeviceId(context)
    val countryCode = getCountryCode(context)
    val languageCode = getLanguageCode()
    return OkHttpClient.Builder().also { client ->
        client.retryOnConnectionFailure(true)
        client.addInterceptor(headerInterceptorLoginProtobuf(deviceId, countryCode, languageCode))
        client.addNetworkInterceptor(logInterceptor())
        client.connectTimeout(30, TimeUnit.SECONDS)
        client.readTimeout(30, TimeUnit.SECONDS)
        client.protocols(Collections.singletonList(Protocol.HTTP_1_1))
        client.cache(cache)
    }.build()
}

fun provideRetrofitLoginProtobuf(client: OkHttpClient): Retrofit {
    val gson = GsonBuilder()
        .setLenient()
        .create()
    return Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(WireConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

fun provideAuthService(retrofit: Retrofit): AuthService {
    return retrofit.create(AuthService::class.java)
}

fun provideSongRemoteService(retrofit: Retrofit): SongService {
    return retrofit.create(SongService::class.java)
}

fun provideDeezerRest(retrofit: Retrofit): DeezerService {
    val newBuilder = retrofit.newBuilder()
        .baseUrl("https://api.deezer.com/")
        .build()
    return newBuilder.create(DeezerService::class.java)
}

fun provideLyrics(retrofit: Retrofit): LyricsRestService {
    val newBuilder = retrofit.newBuilder()
        .baseUrl("https://makeitpersonal.co")
        .addConverterFactory(LyricsConverterFactory())
        .build()
    return newBuilder.create(LyricsRestService::class.java)
}