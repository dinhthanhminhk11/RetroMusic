package code.name.monkey.retromusic.model.auth

data class UserClient(
    val id: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val image: String? = null,
    val imageBanner: String? = null,
    val accessToken: String? = null
)