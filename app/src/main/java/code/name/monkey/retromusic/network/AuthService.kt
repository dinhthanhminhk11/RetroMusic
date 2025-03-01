package code.name.monkey.retromusic.network

import code.name.monkey.retromusic.network.model.request.auth.REQLogin
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST(Endpoint.LOGIN)
    suspend fun login(
        @Body reqLogin: REQLogin
    ): Response<LoginResponseNative>

    @POST(Endpoint.REGISTER)
    suspend fun register(
        @Body reqRegister: REQLogin
    ): Response<LoginResponseNative>


    @POST(Endpoint.VERIFY_OTP)
    suspend fun verifyOtp(
        @Body reqVerifyOtp: REQLogin
    ): Response<LoginResponseNative>

    @POST(Endpoint.RESENT_OTP)
    suspend fun reSentOtp(
        @Body reqVerifyOtp: REQLogin
    ): Response<LoginResponseNative>

    @POST(Endpoint.SET_PASSWORD)
    suspend fun setPassword(
        @Body setPassBody: REQLogin
    ): Response<LoginResponseNative>

    @POST(Endpoint.CHECK_ACCOUNT)
    suspend fun checkAccount(
        @Body setPassBody: REQLogin
    ): Response<LoginResponseNative>


    @GET(Endpoint.LOGIN_BY_TOKEN)
    suspend fun loginByToken(
        @Header("access-token-x") token: String
    ): Response<LoginResponseNative>

    @POST(Endpoint.FAKE_LOGIN)
    suspend fun fakeLogin(@Body requestBody: RequestBody): Response<LoginResponseNative>
}