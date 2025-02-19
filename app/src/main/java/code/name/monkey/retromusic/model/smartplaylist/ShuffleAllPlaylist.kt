package code.name.monkey.retromusic.model.smartplaylist

import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.R
import kotlinx.parcelize.Parcelize

@Parcelize
class ShuffleAllPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.action_shuffle_all),
    iconRes = R.drawable.ic_shuffle
) {

}