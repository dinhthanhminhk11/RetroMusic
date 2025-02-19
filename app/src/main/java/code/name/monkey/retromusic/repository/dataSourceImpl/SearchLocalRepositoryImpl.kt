package code.name.monkey.retromusic.repository.dataSourceImpl

import android.content.Context
import code.name.monkey.retromusic.Filter
import code.name.monkey.retromusic.repository.dataSource.SearchLocalRepository

class SearchLocalRepositoryImpl() : SearchLocalRepository {
    override suspend fun searchAll(
        context: Context,
        query: String?,
        filter: Filter
    ): MutableList<Any> {
        TODO("Not yet implemented")
    }
}