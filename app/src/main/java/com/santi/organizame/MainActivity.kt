package com.santi.organizame

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.santi.organizame.databinding.ActivityMainBinding
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.ActividadRepository
import com.santi.organizame.modelo.OrganizaMeDatabase
import com.santi.organizame.modelo.AnticipacionRecordatorio
import java.time.format.DateTimeFormatter
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var actividadRepository: ActividadRepository

    private lateinit var adaptador: AdaptadorTarjetasActividad

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
        adaptador = AdaptadorTarjetasActividad(
            actividades = emptyList(),

            alEditar = { actividad ->
                val intent = Intent(this, NuevaTareaActividad::class.java)

                intent.putExtra("actividadId", actividad.id)

                nuevaTareaLauncher.launch(intent)
            },

            alEliminar = { actividad ->

                AlertDialog.Builder(this)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Seguro que querés eliminar \"${actividad.titulo}\"?")
                    .setPositiveButton("Sí") { _, _ ->

                        actividadRepository.eliminar(actividad) { resultado ->
                            runOnUiThread {
                                resultado
                                    .onSuccess {
                                        cargarActividades()
                                    }
                                    .onFailure {
                                        Toast.makeText(
                                            this,
                                            "No se pudo eliminar la tarea",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        binding.listaActividades.layoutManager = LinearLayoutManager(this)
        binding.listaActividades.adapter = adaptador

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

                        if (actividades.isEmpty()) {
                            binding.statusText.visibility = View.VISIBLE
                            binding.statusText.text = "Todavía no hay tareas guardadas."
                        } else {
                            binding.statusText.visibility = View.GONE
                        }

                        adaptador.actualizarLista(actividades)
                    }
                    .onFailure {
                        binding.statusText.visibility = View.VISIBLE
                        binding.statusText.text = "No se pudieron cargar las tareas."
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

            append("\nCategoría: ").append(
                nombreCategoria(actividad.categoriaId)
            )

            append("\nRecordatorio: ").append(
                nombreRecordatorio(actividad.recordatorio)
            )

            actividad.estadoTarea?.let {
                append("\nEstado: ").append(it)
            }
        }

    private fun nombreCategoria(categoriaId: Long?): String =
        when (categoriaId) {
            1L -> "Personal"
            2L -> "Estudio"
            3L -> "Trabajo"
            else -> "Sin categoría"
        }

    private fun nombreRecordatorio(
        recordatorio: AnticipacionRecordatorio?
    ): String =
        when (recordatorio) {
            AnticipacionRecordatorio.A_LA_HORA -> "A la hora"
            AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES -> "10 minutos antes"
            AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES -> "30 minutos antes"
            AnticipacionRecordatorio.UNA_HORA_ANTES -> "1 hora antes"
            AnticipacionRecordatorio.UN_DIA_ANTES -> "1 día antes"
            null -> "Sin recordatorio"
        }

    private companion object {
        val FORMATO_FECHA: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")

        val FORMATO_HORA: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm")
    }
}
