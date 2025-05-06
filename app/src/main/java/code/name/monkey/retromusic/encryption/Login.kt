package code.name.monkey.retromusic.encryption

import androidx.annotation.Keep
import timber.log.Timber

@Keep
object Login {
    init {
        System.loadLibrary("retromusicplayer")
    }

    @JvmStatic
    external fun init(key: String, iv: String)

    @JvmStatic
    private external fun encryptNative(input: String): String

    @JvmStatic
    private external fun decryptNative(input: String): ByteArray

    @JvmStatic
    external fun checksum(input: String): String

    @JvmStatic
    fun encryptData(input: String): String {
        return encryptNative(input)
    }

    @JvmStatic
    fun decryptData(input: String): String {
        return String(decryptNative(input))
    }

    @JvmStatic
    fun load() {
        Timber.tag("LoginNative").i("Loaded")
    }
}
