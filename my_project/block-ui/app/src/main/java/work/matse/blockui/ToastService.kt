package work.matse.blockui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat


class ToastService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        return START_NOT_STICKY
    }

    private fun createNotification() {
        val channel = NotificationChannel(getString(R.string.channel_id), "BLOCKUI Notification Channel",
            NotificationManager.IMPORTANCE_DEFAULT)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        //запуск сервиса
        //Intent реагирует на нажатие
        val notificationIntent = Intent(this, Service::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0)
        val notification = NotificationCompat.Builder(this, getString(R.string.channel_id))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_desc))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(101, notification)
    }
}