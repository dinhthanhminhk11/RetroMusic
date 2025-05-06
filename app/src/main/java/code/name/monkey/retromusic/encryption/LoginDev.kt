package code.name.monkey.retromusic.encryption

import androidx.annotation.Keep

@Keep
object LoginDev {
    init {
        Login.load()
    }

    @JvmStatic
    external fun encryptKey(input: String): String
}
