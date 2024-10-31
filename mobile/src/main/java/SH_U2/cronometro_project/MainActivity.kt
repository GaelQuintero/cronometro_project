package SH_U2.cronometro_project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: Button

    private var isRunning = false
    private var timeElapsed = 0L // tiempo en segundos
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        // Solicita permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // Inicializa las vistas
        timerTextView = findViewById(R.id.timerTextView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        resetButton = findViewById(R.id.resetButton)

        // listeners a los botones
        startButton.setOnClickListener { startTimer() }
        stopButton.setOnClickListener { stopTimer() }
        resetButton.setOnClickListener { resetTimer() }
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onStart() {
        super.onStart()
        // Detén el servicio cuando la app esté en primer plano
        val serviceIntent = Intent(this, TimerService::class.java)
        stopService(serviceIntent)
    }

    override fun onStop() {
        super.onStop()
        // Inicia el servicio solo si el cronómetro está en marcha y la app está en segundo plano
        if (isRunning) {
            val serviceIntent = Intent(this, TimerService::class.java)
            serviceIntent.putExtra("elapsedTime", timeElapsed)
            startForegroundService(serviceIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detiene el servicio si el cronómetro se detiene
        if (isRunning) {
            val serviceIntent = Intent(this, TimerService::class.java)
            stopService(serviceIntent)
        }
    }

    fun stopTimerFromNotification() {
        if (isRunning) {
            isRunning = false
            timer?.cancel()
            updateTimerText()
        }
    }

    fun resetTimerFromNotification() {
        isRunning = false
        timeElapsed = 0L
        updateTimerText()
    }

    private fun startTimer() {
        if (!isRunning) {
            isRunning = true

            val serviceIntent = Intent(this, TimerService::class.java)
            serviceIntent.putExtra("elapsedTime", timeElapsed)
            startService(serviceIntent)

            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeElapsed++
                    updateTimerText()
                }

                override fun onFinish() {}
            }.start()
        }
    }

    private fun stopTimer() {
        if (isRunning) {
            isRunning = false
            timer?.cancel()
            stopService(Intent(this, TimerService::class.java))
        }
    }

    private fun resetTimer() {
        stopTimer()
        timeElapsed = 0L
        updateTimerText()
    }

    private fun updateTimerText() {
        val hours = (timeElapsed / 3600) % 60
        val minutes = (timeElapsed / 60) % 60
        val seconds = timeElapsed % 60
        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        timerTextView.text = timeString
    }
}
