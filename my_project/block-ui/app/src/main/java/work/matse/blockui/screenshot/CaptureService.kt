package work.matse.blockui.screenshot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_NONE
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import work.matse.blockui.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


class CaptureService : Service() {
    companion object {
        private val TAG = CaptureService::class.qualifiedName
        val ACTION_ENABLE_CAPTURE = "enable_capture"
    }

    private val capture = Capture(this)
    var timer = Timer()
    val filename = "inaut.jpg"
    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    var fos: OutputStream? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create notification channel
            val NOTIFICATION_CHANNEL_ID = "com.example.simpleapp"
            val channelName = "My Background Service"
            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, IMPORTANCE_NONE)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)!!
            manager!!.createNotificationChannel(chan)

            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_notifications_active_black_24dp)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
            startForeground(1000, notification)
        }
        enableCapture()

        return Service.START_STICKY
    }


    private fun enableCapture() {
        if (CaptureActivity.projection == null) {
            val intent = Intent(this, CaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            onEnableCapture()
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap) : String? {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (File(imagesDir, filename).exists()) {
                File(imagesDir, filename).delete()
            }
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        }*/
        val file = File(imagesDir, filename)
        fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos?.flush()
        fos?.close()
        var str : String = imagesDir.toString()
        str += "/$filename"
        return str
    }

    private fun onEnableCapture() {
        CaptureActivity.projection?.run {
            capture.run(this) {
                val monitor = object : TimerTask() {
                    override fun run() {
                        saveMediaToStorage(capture.main_bitmap!!)
                    }
                }
                timer.schedule(monitor, 1000, 1000)
            }
        }
    }

    private fun disableCapture() {
        capture.stop()
        CaptureActivity.projection = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disableCapture()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}