package code.name.monkey.retromusic.model

import com.google.gson.Gson

class BodyRequest(vararg args: Any?) : LinkedHashMap<String, Any?>() {
    init {
        for (i in args.indices step 2) {
            if (i + 1 < args.size && args[i] is String) {
                val key = args[i] as String
                val value = args[i + 1]
                this[key] = value
            }
        }
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}