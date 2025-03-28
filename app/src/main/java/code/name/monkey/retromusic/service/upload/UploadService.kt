package code.name.monkey.retromusic.service.upload

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.UPLOAD_ACTION_FAILED
import code.name.monkey.retromusic.UPLOAD_CHANNEL
import code.name.monkey.retromusic.UPLOAD_PROGRESS
import code.name.monkey.retromusic.UPLOAD_PROGRESS_ACTION
import code.name.monkey.retromusic.fragments.upload.FileUploader
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
        val fileUri = intent?.getStringExtra("fileUri") ?: return START_NOT_STICKY
        val fileHash = intent.getStringExtra("fileHash") ?: return START_NOT_STICKY
        val fileName = intent.getStringExtra("fileName") ?: return START_NOT_STICKY

        val file = File(fileUri)
        if (!file.exists() || !file.canRead()) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(1, createNotification(0))

        serviceScope.launch {
            uploadManager.uploadFile(
                fileHash = fileHash,
                file = file,
                fileName = fileName,
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
        val intent = Intent(UPLOAD_PROGRESS_ACTION)
        intent.putExtra(UPLOAD_PROGRESS, progress)
        sendBroadcast(intent)
    }

    private fun sendUploadFailed() {
        val intent = Intent(UPLOAD_ACTION_FAILED)
        sendBroadcast(intent)
    }
}