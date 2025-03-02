package code.name.monkey.retromusic.repository.data_source.network

import code.name.monkey.retromusic.network.model.request.auth.REQLogin
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import okhttp3.RequestBody
import retrofit2.Response
interface AuthRepository {
    suspend fun login(reqLogin: REQLogin): Response<LoginResponseNative>
    suspend fun register(reqLogin: REQLogin): Response<LoginResponseNative>
    suspend fun verifyOtp(reqLogin: REQLogin): Response<LoginResponseNative>
    suspend fun reSentOtp(reqLogin: REQLogin): Response<LoginResponseNative>
    suspend fun setPassword(reqLogin: REQLogin): Response<LoginResponseNative>
    suspend fun checkAccount(reqLogin: REQLogin): Response<LoginResponseNative>
    suspend fun loginByToken(token: String): Response<LoginResponseNative>
    suspend fun fakeLogin(contact: RequestBody): Response<LoginResponseNative>
}