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
import androidx.core.app.NotificationCompat
import work.matse.blockui.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Files
import java.util.*


class CaptureService : Service() {
    companion object {
        private val TAG = CaptureService::class.qualifiedName
        val ACTION_ENABLE_CAPTURE = "enable_capture"
    }

    private val capture = Capture(this)
    var timer = Timer()

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
            startForeground(100000, notification)
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
        val filename = "inaut.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            var image = File(imagesDir, filename)
            image.delete()
            image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        fos?.close()
        var str : String = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).toString()
        str += "/$filename"
        return str
    }

    private fun onEnableCapture() {
        CaptureActivity.projection?.run {
            capture.run(this) {
                val monitor = object : TimerTask() {
                    override fun run() {
                        saveMediaToStorage(capture.main_bitmap!!)
                        println("SAVED")
                        capture.stop()
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