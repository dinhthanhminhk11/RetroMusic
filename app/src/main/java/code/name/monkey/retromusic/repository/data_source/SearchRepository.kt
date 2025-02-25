package code.name.monkey.retromusic.repository.data_source

import android.content.Context
import code.name.monkey.retromusic.fragments.search.Filter

interface SearchRepository {
    suspend fun searchAll(context: Context, query: String?, filter: Filter): MutableList<Any>
}