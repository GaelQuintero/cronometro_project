package SH_U2.cronometro_project

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat

class TimerService : Service() {
    private var startTime: Long = 0
    private var running: Boolean = false
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (running) {
            return START_STICKY
        }

        val elapsedTime = intent?.getLongExtra("elapsedTime", 0L) ?: 0L
        startTime = SystemClock.elapsedRealtime() - (elapsedTime * 1000)

        running = true
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        Thread {
            while (running) {
                val elapsedMillis = SystemClock.elapsedRealtime() - startTime
                val seconds = (elapsedMillis / 1000).toInt()

                Handler(Looper.getMainLooper()).post {
                    showNotification(seconds)
                }

                Thread.sleep(999)
            }
        }.start()

        return START_STICKY
    }

    private fun formatTime(seconds: Int): String {
        val hours = (seconds / 3600) % 60
        val minutes = (seconds / 60) % 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    private fun showNotification(seconds: Int) {
        val formattedTime = formatTime(seconds)

        // Intentos para detener y reiniciar
        val stopIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_STOP"
        }
        val stopPendingIntent = PendingIntent.getActivity(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val resetIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_RESET"
        }
        val resetPendingIntent = PendingIntent.getActivity(this, 0, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Cronómetro en ejecución")
            .setContentText("Tiempo: $formattedTime")
            .addAction(0, "Detener", stopPendingIntent)
            .addAction(0, "Reiniciar", resetPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Notificación silenciosa
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Cronómetro", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Notificaciones del cronómetro"
                setSound(null, null) // Notificación sin sonido
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Cronómetro en ejecución")
            .setContentText("Tiempo: 0 segundos")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        private const val CHANNEL_ID = "timer_channel"
        private const val NOTIFICATION_ID = 1
    }
}
