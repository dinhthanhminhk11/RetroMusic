package code.name.monkey.retromusic.model.smartplaylist

import code.name.monkey.retromusic.MyApplication
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class NotPlayedPlaylist : AbsSmartPlaylist(
    name = MyApplication.getContext().getString(R.string.not_recently_played),
    iconRes = R.drawable.ic_audiotrack
) {
    override fun songs(): List<Song> {
        return topPlayedRepository.notRecentlyPlayedTracks()
    }
}