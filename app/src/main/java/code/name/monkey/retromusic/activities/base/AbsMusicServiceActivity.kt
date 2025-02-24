package code.name.monkey.retromusic.activities.base

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import code.name.monkey.appthemehelper.util.VersionUtils
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.db.toPlayCount
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.interfaces.IMusicServiceEventListener
import code.name.monkey.retromusic.repository.Repository
import code.name.monkey.retromusic.service.MusicService
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

abstract class AbsMusicServiceActivity : AbsBaseActivity(), IMusicServiceEventListener {
    private val mMusicServiceEventListeners = ArrayList<IMusicServiceEventListener>()

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var musicPlayerRemote: MusicPlayerRemote

    private var serviceToken: MusicPlayerRemote.ServiceToken? = null
    private var musicStateReceiver: MusicStateReceiver? = null
    private var receiverRegistered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceToken = musicPlayerRemote.bindToService(this, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                this@AbsMusicServiceActivity.onServiceConnected()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                this@AbsMusicServiceActivity.onServiceDisconnected()
            }
        })

        setPermissionDeniedMessage(getString(R.string.permission_external_storage_denied))
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayerRemote.unbindFromService(serviceToken)
        if (receiverRegistered && musicStateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(musicStateReceiver!!)
            receiverRegistered = false
        }
    }

    fun addMusicServiceEventListener(listenerI: IMusicServiceEventListener?) {
        if (listenerI != null) {
            mMusicServiceEventListeners.add(listenerI)
        }
    }

    fun removeMusicServiceEventListener(listenerI: IMusicServiceEventListener?) {
        if (listenerI != null) {
            mMusicServiceEventListeners.remove(listenerI)
        }
    }

    override fun onServiceConnected() {
        if (!receiverRegistered) {
            musicStateReceiver = MusicStateReceiver(this)

            val filter = IntentFilter()
            filter.addAction(MusicService.PLAY_STATE_CHANGED)
            filter.addAction(MusicService.SHUFFLE_MODE_CHANGED)
            filter.addAction(MusicService.REPEAT_MODE_CHANGED)
            filter.addAction(MusicService.META_CHANGED)
            filter.addAction(MusicService.QUEUE_CHANGED)
            filter.addAction(MusicService.MEDIA_STORE_CHANGED)
            filter.addAction(MusicService.FAVORITE_STATE_CHANGED)

            LocalBroadcastManager.getInstance(this).registerReceiver(musicStateReceiver!!, filter)
            receiverRegistered = true
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceConnected()
        }
    }

    override fun onServiceDisconnected() {
        if (receiverRegistered && musicStateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(musicStateReceiver!!)
            receiverRegistered = false
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceDisconnected()
        }
    }

    override fun onPlayingMetaChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayingMetaChanged()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            if (!PreferenceUtil.pauseHistory) {
                repository.upsertSongInHistory(musicPlayerRemote.currentSong)
            }
            val song = repository.findSongExistInPlayCount(musicPlayerRemote.currentSong.id)
                ?.apply { playCount += 1 }
                ?: musicPlayerRemote.currentSong.toPlayCount()

            repository.upsertSongInPlayCount(song)
        }
    }

    override fun onQueueChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onQueueChanged()
        }
    }

    override fun onPlayStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayStateChanged()
        }
    }

    override fun onMediaStoreChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onMediaStoreChanged()
        }
    }

    override fun onRepeatModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onRepeatModeChanged()
        }
    }

    override fun onShuffleModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onShuffleModeChanged()
        }
    }

    override fun onFavoriteStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onFavoriteStateChanged()
        }
    }

    override fun onHasPermissionsChanged(hasPermissions: Boolean) {
        super.onHasPermissionsChanged(hasPermissions)
        val intent = Intent(MusicService.MEDIA_STORE_CHANGED)
        intent.putExtra(
            "from_permissions_changed",
            true
        ) // just in case we need to know this at some point
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        logD("sendBroadcast $hasPermissions")
    }

    override fun getPermissionsToRequest(): Array<String> {
        return mutableListOf<String>().apply {
            if (VersionUtils.hasT()) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
                add(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (!VersionUtils.hasR()) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private class MusicStateReceiver(activity: AbsMusicServiceActivity) : BroadcastReceiver() {

        private val reference: WeakReference<AbsMusicServiceActivity> = WeakReference(activity)

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val activity = reference.get()
            if (activity != null && action != null) {
                when (action) {
                    MusicService.FAVORITE_STATE_CHANGED -> activity.onFavoriteStateChanged()
                    MusicService.META_CHANGED -> activity.onPlayingMetaChanged()
                    MusicService.QUEUE_CHANGED -> activity.onQueueChanged()
                    MusicService.PLAY_STATE_CHANGED -> activity.onPlayStateChanged()
                    MusicService.REPEAT_MODE_CHANGED -> activity.onRepeatModeChanged()
                    MusicService.SHUFFLE_MODE_CHANGED -> activity.onShuffleModeChanged()
                    MusicService.MEDIA_STORE_CHANGED -> activity.onMediaStoreChanged()
                }
            }
        }
    }

    companion object {
        val TAG: String = AbsMusicServiceActivity::class.java.simpleName
    }
}