package SH_U2.cronometro_project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: Button
    private lateinit var firestore: FirebaseFirestore

    private var isRunning = false
    private var timeElapsed = 0L // tiempo en segundos
    private var totalElapsedTime = 0L // tiempo total acumulado en segundos
    private var lapCount = 0 // contador de vueltas
    private var timer: CountDownTimer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        // Inicializa Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        // Configura el botón de vueltas
        val lapButton = findViewById<Button>(R.id.lapButton)
        lapButton.setOnClickListener { saveLapTime() }

        // Solicita permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // Inicializa las vistas
        timerTextView = findViewById(R.id.timerTextView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        resetButton = findViewById(R.id.resetButton)

        // Listeners a los botones
        startButton.setOnClickListener { startTimer() }
        stopButton.setOnClickListener { stopTimer() }
        resetButton.setOnClickListener { resetTimer() }
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
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
        totalElapsedTime = 0L
        lapCount = 0
        updateTimerText()
    }

    private fun updateTimerText() {
        val timeString = formatTime(timeElapsed)
        timerTextView.text = timeString
    }

    private fun formatTime(timeInSeconds: Long): String {
        val hours = (timeInSeconds / 3600) % 24
        val minutes = (timeInSeconds / 60) % 60
        val seconds = timeInSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun saveLapTime() {
        lapCount++  // Incrementa el contador de vueltas
        totalElapsedTime += timeElapsed  // Suma el tiempo parcial al total acumulado
        val currentTimeMillis = System.currentTimeMillis()
        val formattedTimestamp = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(currentTimeMillis))
        val deviceModel = android.os.Build.MODEL
        // Crea un mapa con los datos de la vuelta
        val lapData = hashMapOf(
            "lapId" to lapCount,
            "lapTime" to formatTime(timeElapsed),
            "totalTime" to formatTime(totalElapsedTime),
            "timestamp" to  formattedTimestamp,
            "deviceModel" to deviceModel
        )

        // Guarda los datos en Firestore
        firestore.collection("vueltas").add(lapData)
            .addOnSuccessListener {
                Toast.makeText(this, "Vuelta registrada con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al registrar la vuelta", Toast.LENGTH_SHORT).show()
            }


        // Reinicia el tiempo parcial para la siguiente vuelta
        timeElapsed = 0L
        updateTimerText()
    }
}
