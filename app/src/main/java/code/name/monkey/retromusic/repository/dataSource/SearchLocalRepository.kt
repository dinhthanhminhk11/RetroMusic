package code.name.monkey.retromusic.repository.dataSource

import android.content.Context
import code.name.monkey.retromusic.Filter

interface SearchLocalRepository {
    suspend fun searchAll(context: Context, query: String?, filter: Filter): MutableList<Any>
}