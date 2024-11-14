package SHU2.wearos


import android.os.Bundle
import android.util.Log
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
        setContentView(R.layout.layout_wear) // Asegúrate de que el nombre coincide con tu XML

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

                // Si la consulta fue exitosa, mostramos los datos de las vueltas
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
                    }
                    row.addView(lapIdTextView)

                    val lapTimeTextView = TextView(this).apply {
                        text = lapTime
                        setPadding(10, 10, 10, 10)
                    }
                    row.addView(lapTimeTextView)

                    val totalTimeTextView = TextView(this).apply {
                        text = totalTime
                        setPadding(10, 10, 10, 10)
                    }
                    row.addView(totalTimeTextView)



                    // Añadir la fila a la tabla
                    lapTableLayout.addView(row)
                }
            }
    }
}
