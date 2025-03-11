package code.name.monkey.retromusic.network.model.response.auth


data class ResponseDataAuth(
    val success: Boolean,
    val data: Data
)

data class Data(
    val code: String,
    val message: String,
    val details: String
)