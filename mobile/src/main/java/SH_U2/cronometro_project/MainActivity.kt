package SH_U2.cronometro_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import SH_U2.cronometro_project.ui.theme.Cronometro_projectTheme
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
        setContentView(R.layout.layout) // Asegúrate de que el nombre del layout sea correcto

        // Inicializa las vistas
        timerTextView = findViewById(R.id.timerTextView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        resetButton = findViewById(R.id.resetButton)

        // Asigna los listeners a los botones
        startButton.setOnClickListener { startTimer() }
        stopButton.setOnClickListener { stopTimer() }
        resetButton.setOnClickListener { resetTimer() }
    }

    private fun startTimer() {
        if (!isRunning) {
            isRunning = true
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