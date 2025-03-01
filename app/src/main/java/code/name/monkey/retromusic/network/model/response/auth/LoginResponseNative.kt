package code.name.monkey.retromusic.network.model.response.auth

import com.google.gson.annotations.SerializedName

data class LoginResponseNative(
    @SerializedName("status") var status: Boolean?,
    @SerializedName("message") var message: String?,
    @SerializedName("error") var error: String?,
    @SerializedName("data") var data: String?,
    @SerializedName("code") var code: Int?,
    @SerializedName("type") var type: String?
)