package work.matse.blockui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import androidx.core.app.NotificationCompat


class ToastService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    var text: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        text = intent?.getStringExtra(EXTRA_MESSAGE)
        createNotification(text!!)
        return START_NOT_STICKY
    }

    private fun createNotification(text: String) {
        val channel = NotificationChannel(getString(R.string.channel_id), "BLOCKUI Notification Channel", NotificationManager.IMPORTANCE_DEFAULT)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        val notificationIntent = Intent(this, BlockUIService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0)
        val notification = NotificationCompat.Builder(this, getString(R.string.channel_id))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(101, notification)
    }
}