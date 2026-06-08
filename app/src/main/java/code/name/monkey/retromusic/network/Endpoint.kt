package code.name.monkey.retromusic.network

object Endpoint {
    const val TEST = "TEST"
    const val LOGIN = "auth/login"
    const val LOGOUT = "auth/logout"
    const val REGISTER = "auth/register"
    const val VERIFY_OTP = "auth/verifyOTP"
    const val RESENT_OTP = "auth/resentOtp"
    const val SET_PASSWORD = "auth/setPassword"
    const val CHECK_ACCOUNT = "auth/checkAccount"
    const val LOGIN_BY_TOKEN = "auth/getUserByToken"
    const val X_ACCESS_TOKEN = "x-access-token"
    const val UPDATE_USER_INFO = "auth/updateUser"


    const val SONG_TEST_UPLOAD_CHUNK = "song/testUploadFileChunk"
    const val SONG_CHECK_FILE = "song/check-file/{fileHash}"
    const val SONG_UPLOAD_CHUNK = "song/upload-chunk/{fileHash}/{chunkIndex}"
    const val SONG_MERGE_FILE = "song/merge-file"

}
