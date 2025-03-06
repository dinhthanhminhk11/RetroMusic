package code.name.monkey.retromusic.repository.data_source_impl.network

import code.name.monkey.retromusic.SuccessResponse
import code.name.monkey.retromusic.network.AuthService
import code.name.monkey.retromusic.network.model.request.auth.REQLogin
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import code.name.monkey.retromusic.repository.data_source.network.AuthRepository
import okhttp3.RequestBody
import retrofit2.Response

class AuthRepositoryImpl(private val authService: AuthService) : AuthRepository {
    override suspend fun login(reqLogin: REQLogin): Response<LoginResponseNative> =
        authService.login(reqLogin)

    override suspend fun register(requestBody: RequestBody): Response<SuccessResponse> =
        authService.register(requestBody)

    override suspend fun verifyOtp(reqLogin: REQLogin): Response<LoginResponseNative> =
        authService.verifyOtp(reqLogin)

    override suspend fun reSentOtp(reqLogin: REQLogin): Response<LoginResponseNative> =
        authService.reSentOtp(reqLogin)

    override suspend fun setPassword(reqLogin: REQLogin): Response<LoginResponseNative> =
        authService.setPassword(reqLogin)

    override suspend fun checkAccount(reqLogin: REQLogin): Response<LoginResponseNative> =
        authService.checkAccount(reqLogin)

    override suspend fun loginByToken(token: String): Response<LoginResponseNative> =
        authService.loginByToken(token)

    override suspend fun fakeLogin(contact: RequestBody): Response<LoginResponseNative> =
        authService.fakeLogin(contact)

}