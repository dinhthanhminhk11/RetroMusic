package code.name.monkey.retromusic.repository.data_source_impl.network

import android.content.Context
import code.name.monkey.retromusic.network.AuthService
import code.name.monkey.retromusic.network.Resource
import code.name.monkey.retromusic.network.model.request.auth.REQLogin
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative
import code.name.monkey.retromusic.repository.data_source.BaseDataSource
import code.name.monkey.retromusic.repository.data_source.network.AuthRepository

class AuthRepositoryImpl(private val authService: AuthService) : AuthRepository, BaseDataSource() {
    override suspend fun login(reqLogin: REQLogin): Resource<LoginResponseNative> = safeApiCall {
        authService.login(reqLogin)
    }

}