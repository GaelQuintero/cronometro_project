package SH_U2.cronometro_project

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
        // Si el servicio ya está en ejecución, no se reinicia el tiempo
        if (running) {
            return START_STICKY
        }

        // Obtiene el tiempo transcurrido desde el intent (si está disponible)
        val elapsedTime = intent?.getLongExtra("elapsedTime", 0L) ?: 0L
        startTime = SystemClock.elapsedRealtime() - (elapsedTime * 999) // Ajusta el tiempo

        running = true
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Inicia el hilo para actualizar la notificación
        Thread {
            while (running) {
                val elapsedMillis = SystemClock.elapsedRealtime() - startTime
                val seconds = (elapsedMillis / 999).toInt()

                // Actualiza la notificación en el hilo principal
                Handler(Looper.getMainLooper()).post {
                    showNotification(seconds)
                }

                Thread.sleep(999) // Actualiza cada segundo
            }
        }.start()

        return START_STICKY
    }



    private fun showNotification(seconds: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // icono
            .setContentTitle("Cronómetro en ejecución")
            .setContentText("Tiempo: $seconds segundos")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true) // Para que no se pueda despegar
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cronómetro"
            val descriptionText = "Notificaciones del cronómetro"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Registra el canal con el sistema
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // icono
            .setContentTitle("Cronómetro en ejecución")
            .setContentText("Tiempo: 0 segundos") // El tiempo se actualizará en el bucle
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
