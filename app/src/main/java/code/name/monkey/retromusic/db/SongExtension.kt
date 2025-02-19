package code.name.monkey.retromusic.db

import code.name.monkey.retromusic.model.Song

fun Song.toHistoryEntity(timePlayed: Long): HistoryEntity {
    return HistoryEntity(
        id = id,
        title = title,
        trackNumber = trackNumber,
        year = year,
        duration = duration,
        data = data,
        dateModified = dateModified,
        albumId = albumId,
        albumName = albumName,
        artistId = artistId,
        artistName = artistName,
        composer = composer,
        albumArtist = albumArtist,
        timePlayed = timePlayed
    )
}