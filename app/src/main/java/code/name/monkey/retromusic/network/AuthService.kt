package code.name.monkey.retromusic.network

import code.name.monkey.retromusic.network.model.request.auth.REQLogin
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST(Endpoint.LOGIN)
    suspend fun login(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @POST(Endpoint.LOGOUT)
    suspend fun logout(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @POST(Endpoint.REGISTER)
    suspend fun register(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>


    @POST(Endpoint.VERIFY_OTP)
    suspend fun verifyOtp(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @POST(Endpoint.RESENT_OTP)
    suspend fun reSentOtp(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @POST(Endpoint.SET_PASSWORD)
    suspend fun setPassword(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @POST(Endpoint.CHECK_ACCOUNT)
    suspend fun checkAccount(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>


    @GET(Endpoint.LOGIN_BY_TOKEN)
    suspend fun loginByToken(
        @Header("access-token-x") token: String
    ): Response<LoginResponseNative>

    @POST(Endpoint.FAKE_LOGIN)
    suspend fun fakeLogin(@Body requestBody: RequestBody): Response<LoginResponseNative>
}