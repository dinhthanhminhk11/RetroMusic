package code.name.monkey.retromusic.repository.data_source.network

import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

interface AuthRepository {
    suspend fun login(reqLogin: RequestBody): Response<ResponseBody>
    suspend fun logout(reqLogin: RequestBody): Response<ResponseBody>
    suspend fun register(requestBody: RequestBody): Response<ResponseBody>
    suspend fun verifyOtp(reqLogin: RequestBody): Response<ResponseBody>
    suspend fun reSentOtp(reqLogin: RequestBody): Response<ResponseBody>
    suspend fun setPassword(reqLogin: RequestBody): Response<ResponseBody>
    suspend fun checkAccount(reqLogin: RequestBody): Response<ResponseBody>
    suspend fun loginByToken(token: String): Response<LoginResponseNative>
    suspend fun fakeLogin(contact: RequestBody): Response<LoginResponseNative>
}