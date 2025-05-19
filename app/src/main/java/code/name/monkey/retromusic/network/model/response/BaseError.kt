package code.name.monkey.retromusic.network.model.response

import com.google.gson.annotations.SerializedName

data class BaseError(
    @SerializedName("error_code")
    val errorCode: Int? = null,
    val status: Int? = null,
    val message: String? = null
)