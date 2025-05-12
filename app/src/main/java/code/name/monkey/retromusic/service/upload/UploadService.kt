package code.name.monkey.retromusic.service.upload

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import code.name.monkey.retromusic.FILE_HASH
import code.name.monkey.retromusic.FILE_NAME
import code.name.monkey.retromusic.FILE_SIZE
import code.name.monkey.retromusic.FILE_URI
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.UPLOAD_CHANNEL
import code.name.monkey.retromusic.UPLOAD_CHUNKS
import code.name.monkey.retromusic.extensions.postOnMainThread
import code.name.monkey.retromusic.fragments.upload.FileUploader
import code.name.monkey.retromusic.util.EventCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
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

        startForeground(1, createNotification(0))

        serviceScope.launch {
            sendStart()
            uploadManager.uploadFile(
                fileHash = fileHash,
                file = file,
                uploadedChunks = uploadedChunks,
                fileName = fileName,
                fileSize = fileSize.toInt(),
                onProgress = { progress ->
                    updateNotification(progress)
                    sendUploadProgress(progress)
                },
                onError = {
                    sendUploadFailed()
                    stopSelf()
                }
            )
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
        EventCenter.getInstance(0)
            .postOnMainThread(EventCenter.EventType.UPLOAD_PROGRESS_ACTION, progress)
    }

    private fun sendStart() {
        EventCenter.getInstance(0)
            .postOnMainThread(EventCenter.EventType.UPLOAD_START_UPLOAD_PROGRESS)
    }

    private fun sendUploadFailed() {
        EventCenter.getInstance(0)
            .postOnMainThread(EventCenter.EventType.UPLOAD_ACTION_FAILED)
    }
}