package code.name.monkey.retromusic.repository.data_source

import code.name.monkey.retromusic.model.Contributor

interface LocalDataRepository {
    fun contributors(): List<Contributor>
}
