package code.name.monkey.retromusic.repository.data_source.network

import code.name.monkey.retromusic.network.Resource
import code.name.monkey.retromusic.network.model.request.auth.REQLogin
import code.name.monkey.retromusic.network.model.response.auth.LoginResponseNative

interface AuthRepository {
    suspend fun login(reqLogin: REQLogin): Resource<LoginResponseNative>
}