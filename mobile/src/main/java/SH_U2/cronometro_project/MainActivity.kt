package SH_U2.cronometro_project

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
        setContentView(R.layout.layout) // Nombre del UI

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
    override fun onStop() {
        super.onStop()
        // Inicia el servicio en primer plano solo si no está en ejecución
        val serviceIntent = Intent(this, TimerService::class.java)
        startForegroundService(serviceIntent)
    }
    override fun onDestroy() {
        super.onDestroy()
        // Se detiene el servicio si el cronómetro se detiene
        if (isRunning) {
            val serviceIntent = Intent(this, TimerService::class.java)
            stopService(serviceIntent)
        }
    }
    fun stopTimerFromNotification() {
        if (isRunning) {
            isRunning = false
            timer?.cancel()
            updateTimerText() // Actualiza el texto del cronómetro
        }
    }
    fun resetTimerFromNotification() {
        isRunning = false // Detén el cronómetro
        timeElapsed = 0L // Reinicia el tiempo
        updateTimerText() // Actualiza el texto del cronómetro
    }



    private fun startTimer() {
        if (!isRunning) {
            isRunning = true

            // Pasa el tiempo transcurrido al servicio
            val serviceIntent = Intent(this, TimerService::class.java)
            serviceIntent.putExtra("elapsedTime", timeElapsed) // Enviar tiempo actual
            startService(serviceIntent)

            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeElapsed++
                    updateTimerText()
                }

                override fun onFinish() {
                    // No se llamará ya que usamos Long.MAX_VALUE
                }
            }.start()
        }
    }


    private fun stopTimer() {
        if (isRunning) {
            isRunning = false
            timer?.cancel()
            // Detiene el servicio cuando se detiene el cronómetro
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

