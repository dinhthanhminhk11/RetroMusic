package code.name.monkey.retromusic.service.upload

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import code.name.monkey.retromusic.FILE_HASH
import code.name.monkey.retromusic.FILE_NAME
import code.name.monkey.retromusic.FILE_SIZE
import code.name.monkey.retromusic.FILE_URI
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.UPLOAD_CHANNEL
import code.name.monkey.retromusic.UPLOAD_CHUNKS
import code.name.monkey.retromusic.extensions.postOnMainThread
import code.name.monkey.retromusic.fragments.upload.FileUploader
import code.name.monkey.retromusic.network.Result
import code.name.monkey.retromusic.util.EventsCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.io.File

class UploadService : Service() {
    private val uploadManager: FileUploader by inject { parametersOf(applicationContext) }
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val fileUri = intent?.getStringExtra(FILE_URI) ?: return START_NOT_STICKY
        val fileHash = intent.getStringExtra(FILE_HASH) ?: return START_NOT_STICKY
        val fileName = intent.getStringExtra(FILE_NAME) ?: return START_NOT_STICKY
        val fileSize = intent.getIntExtra(FILE_SIZE, 0)
        val uploadedChunks = intent.getIntegerArrayListExtra(UPLOAD_CHUNKS) ?: arrayListOf()

        val file = File(fileUri)
        if (!file.exists() || !file.canRead()) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            ServiceCompat.startForeground(
                this,
                1,
                createNotification(0),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                else
                    0
            )
        } catch (e: Exception) {
            Timber.tag("UploadService").e(e, "Không thể start foreground service")
            sendUploadFailed()
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch {
            sendStart()
            val result = uploadManager.uploadFile(
                fileHash = fileHash,
                file = file,
                uploadedChunks = uploadedChunks,
                fileName = fileName,
                fileSize = fileSize,
                onProgress = { progress ->
                    updateNotification(progress)
                    sendUploadProgress(progress)
                },
                onError = {
                    sendUploadFailed()
                }
            )
            when (result) {
                is Result.Success -> {
                    updateNotification(100)
                    sendUploadProgress(100)
                    sendUploadSuccess()
                }
                else -> sendUploadFailed()
            }
            stopSelf()
        }
        return START_STICKY
    }


    private fun createNotification(progress: Int): Notification {
        val builder = NotificationCompat.Builder(this, UPLOAD_CHANNEL)
            .setContentTitle("Uploading File")
            .setSmallIcon(R.drawable.ic_upload)
            .setProgress(100, progress, false)
            .setOngoing(true)
        return builder.build()
    }

    private fun updateNotification(progress: Int) {
        notificationManager.notify(1, createNotification(progress))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun sendUploadProgress(progress: Int) {
        EventsCenter.getInstance(0)
            .postOnMainThread(EventsCenter.EventType.UPLOAD_PROGRESS_ACTION, progress)
    }

    private fun sendStart() {
        EventsCenter.getInstance(0)
            .postOnMainThread(EventsCenter.EventType.UPLOAD_START_UPLOAD_PROGRESS)
    }

    private fun sendUploadFailed() {
        EventsCenter.getInstance(0)
            .postOnMainThread(EventsCenter.EventType.UPLOAD_ACTION_FAILED)
    }

    private fun sendUploadSuccess() {
        EventsCenter.getInstance(0)
            .postOnMainThread(EventsCenter.EventType.UPLOAD_ACTION_SUCCESS)
    }
}