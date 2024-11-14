package SHU2.wearos

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivityWear : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var lapTableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_wear)

        firestore = FirebaseFirestore.getInstance()
        lapTableLayout = findViewById(R.id.lapTableLayout)

        // Iniciamos el listener en tiempo real
        loadLapDataRealTime()
    }

    private fun loadLapDataRealTime() {
        firestore.collection("vueltas")
            .addSnapshotListener { result, exception ->
                if (exception != null) {
                    Log.w("Firestore", "Error obteniendo los documentos.", exception)
                    return@addSnapshotListener
                }

                // Limpiar las filas actuales en la vista antes de agregar nuevas
                lapTableLayout.removeAllViews()

                // Agregar fila de encabezado
                val headerRow = TableRow(this).apply {
                    layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)
                }

                val headerLapTextView = TextView(this).apply {
                    text = "VUELTA"
                    setPadding(10, 10, -10,10)
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    textSize =9f
                }
                val headerTimeTextView = TextView(this).apply {
                    text = "TIEMPO"
                    setPadding(10,10,6, 10)
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    textSize = 9f
                }
                val headerTotalTextView = TextView(this).apply {
                    text = "TOTAL"
                    setPadding(-6,10, 10, 10)
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    textSize =9f
                }

                headerRow.addView(headerLapTextView)
                headerRow.addView(headerTimeTextView)
                headerRow.addView(headerTotalTextView)

                // Añadir la fila de encabezado a la tabla
                lapTableLayout.addView(headerRow)

                // Agregar filas de datos dinámicamente
                result?.forEach { document ->
                    val lapData = document.data
                    val lapId = lapData["lapId"].toString()
                    val lapTime = lapData["lapTime"].toString()
                    val totalTime = lapData["totalTime"].toString()

                    val row = TableRow(this).apply {
                        layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)
                    }

                    val lapIdTextView = TextView(this).apply {
                        text = lapId
                        setPadding(10, 10, 10, 10)
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER // Centra el texto horizontalmente
                    }
                    row.addView(lapIdTextView)

                    val lapTimeTextView = TextView(this).apply {
                        text = lapTime
                        setPadding(10, 10, 10, 10)
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER // Centra el texto horizontalmente
                    }
                    row.addView(lapTimeTextView)

                    val totalTimeTextView = TextView(this).apply {
                        text = totalTime
                        setPadding(10, 10, 10, 10)
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER // Centra el texto horizontalmente
                    }
                    row.addView(totalTimeTextView)

                    // Añadir la fila de datos a la tabla
                    lapTableLayout.addView(row)
                }
            }
    }
}
