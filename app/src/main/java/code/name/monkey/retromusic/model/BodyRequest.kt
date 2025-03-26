package code.name.monkey.retromusic.model

import com.google.gson.Gson

class BodyRequest(vararg args: Any?) {
    private val data: MutableMap<String, Any?> = LinkedHashMap()

    init {
        for (i in args.indices step 2) {
            if (i + 1 < args.size && args[i] is String) {
                val key = args[i] as String
                val value = args[i + 1]
                if (value is BodyRequest) {
                    data[key] = value.data
                } else {
                    data[key] = value
                }
            }
        }
    }

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(data)
    }
}