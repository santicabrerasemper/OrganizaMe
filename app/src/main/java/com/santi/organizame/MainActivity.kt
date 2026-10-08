package com.santi.organizame

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.santi.organizame.databinding.ActivityMainBinding
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.ActividadRepository
import com.santi.organizame.modelo.OrganizaMeDatabase
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var actividadRepository: ActividadRepository

    private val nuevaTareaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == RESULT_OK) {
                cargarActividades()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actividadRepository = ActividadRepository(
            OrganizaMeDatabase.obtener(applicationContext).actividadDao()
        )

        binding.btnNuevaTarea.setOnClickListener {
            val intent = Intent(this, NuevaTareaActividad::class.java)
            nuevaTareaLauncher.launch(intent)
        }

        cargarActividades()
    }

    private fun cargarActividades() {
        actividadRepository.obtenerTodas { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { actividades ->
                        binding.statusText.text =
                            if (actividades.isEmpty()) {
                                "Todavía no hay tareas guardadas."
                            } else {
                                actividades.joinToString("\n\n") { formatearActividad(it) }
                            }
                    }
                    .onFailure {
                        binding.statusText.text =
                            "No se pudieron cargar las tareas."
                    }
            }
        }
    }

    private fun formatearActividad(actividad: Actividad): String =
        buildString {
            append(actividad.titulo)

            actividad.descripcion
                ?.takeIf { it.isNotBlank() }
                ?.let { append("\n").append(it) }

            actividad.fecha?.let {
                append("\nFecha: ").append(it.format(FORMATO_FECHA))
            }

            actividad.hora?.let {
                append("\nHora: ").append(it.format(FORMATO_HORA))
            }

            append("\nPrioridad: ").append(actividad.prioridad)

            actividad.estadoTarea?.let {
                append("\nEstado: ").append(it)
            }
        }

    private companion object {
        val FORMATO_FECHA: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")

        val FORMATO_HORA: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm")
    }
}
