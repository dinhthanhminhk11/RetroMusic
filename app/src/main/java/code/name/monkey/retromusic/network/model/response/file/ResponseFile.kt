package code.name.monkey.retromusic.network.model.response.file

class ResponseFile(
    val exists: Boolean? = null,
    val uploadedChunks: ArrayList<String> = arrayListOf(),
    val filePath: String? = null
)