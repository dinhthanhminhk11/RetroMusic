package code.name.monkey.retromusic.repository.data_source_impl.network

import code.name.monkey.retromusic.network.AuthService
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import code.name.monkey.retromusic.repository.data_source.network.AuthRepository
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

class AuthRepositoryImpl(private val authService: AuthService) : AuthRepository {
    override suspend fun login(reqLogin: RequestBody): Response<ResponseBody> =
        authService.login(reqLogin)

    override suspend fun logout(reqLogin: RequestBody): Response<ResponseBody> =
        authService.logout(reqLogin)

    override suspend fun register(requestBody: RequestBody): Response<ResponseBody> =
        authService.register(requestBody)

    override suspend fun verifyOtp(reqLogin: RequestBody): Response<ResponseBody> =
        authService.verifyOtp(reqLogin)

    override suspend fun reSentOtp(reqLogin: RequestBody): Response<ResponseBody> =
        authService.reSentOtp(reqLogin)

    override suspend fun setPassword(reqLogin: RequestBody): Response<ResponseBody> =
        authService.setPassword(reqLogin)

    override suspend fun checkAccount(reqLogin: RequestBody): Response<ResponseBody> =
        authService.checkAccount(reqLogin)

    override suspend fun loginByToken(token: String): Response<ResponseBody> =
        authService.loginByToken(token)

}