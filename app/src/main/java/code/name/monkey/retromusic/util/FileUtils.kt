package code.name.monkey.retromusic.util

import android.os.Environment
import java.io.File

object FileUtils {

}

@Suppress("Deprecation")
fun getExternalStorageDirectory(): File {
    return Environment.getExternalStorageDirectory()
}

@Suppress("Deprecation")
fun getExternalStoragePublicDirectory(type: String): File {
    return Environment.getExternalStoragePublicDirectory(type)
}