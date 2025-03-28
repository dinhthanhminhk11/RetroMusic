package code.name.monkey.retromusic.network.model.response.file

class ResponseFile(
    val exists: Boolean? = null,
    val uploadedChunks: ArrayList<Int> = arrayListOf(),
    val filePath: String? = null,
    val code: String? = null
)