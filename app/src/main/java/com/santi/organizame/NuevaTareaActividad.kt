package com.santi.organizame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.santi.organizame.databinding.ActividadNuevaTareaBinding
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import android.widget.ArrayAdapter
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.AnticipacionRecordatorio
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import android.widget.Toast
import java.time.Instant
import com.santi.organizame.modelo.ActividadRepository
import com.santi.organizame.modelo.OrganizaMeDatabase

class NuevaTareaActividad : AppCompatActivity() {

    private lateinit var binding: ActividadNuevaTareaBinding
    private lateinit var repositorio: ActividadRepository
    private var actividadEditando: Actividad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActividadNuevaTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repositorio = ActividadRepository(
            OrganizaMeDatabase.obtener(applicationContext).actividadDao()
        )

        val prioridades = Prioridad.values().map { it.name }

        val adaptadorPrioridades = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            prioridades
        )

        adaptadorPrioridades.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.prioridadTarea.adapter = adaptadorPrioridades

        binding.prioridadTarea.setSelection(
            Prioridad.values().indexOf(Prioridad.MEDIA)
        )

        val categorias = listOf(
            null,
            Categoria(1, "Personal", "#4CAF50"),
            Categoria(2, "Estudio", "#2196F3"),
            Categoria(3, "Trabajo", "#FF9800")
        )

        val nombresCategorias = categorias.map {
            it?.nombre ?: "Sin categoría"
        }

        val adaptadorCategorias = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombresCategorias
        )

        adaptadorCategorias.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.categoriaTarea.adapter = adaptadorCategorias


        val opcionesRecordatorio = listOf(
            "Sin recordatorio",
            "A la hora",
            "10 minutos antes",
            "30 minutos antes",
            "1 hora antes",
            "1 día antes"
        )

        val adaptadorRecordatorio = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesRecordatorio
        )

        adaptadorRecordatorio.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.recordatorioTarea.adapter = adaptadorRecordatorio

        val actividadId = intent.getLongExtra("actividadId", -1L)

        if (actividadId != -1L) {
            repositorio.obtenerPorId(actividadId) { resultado ->
                runOnUiThread {
                    resultado
                        .onSuccess { actividad ->
                            if (actividad != null) {
                                actividadEditando = actividad

                                binding.tituloTarea.setText(actividad.titulo)
                                binding.descripcionTarea.setText(actividad.descripcion ?: "")

                                binding.fechaTarea.setText(
                                    actividad.fecha?.format(FORMATO_FECHA) ?: ""
                                )

                                binding.horaTarea.setText(
                                    actividad.hora?.format(FORMATO_HORA) ?: ""
                                )

                                binding.prioridadTarea.setSelection(
                                    Prioridad.values().indexOf(actividad.prioridad)
                                )

                                val posicionCategoria = categorias.indexOfFirst {
                                    it?.id == actividad.categoriaId
                                }

                                binding.categoriaTarea.setSelection(
                                    if (posicionCategoria >= 0) posicionCategoria else 0
                                )

                                val posicionRecordatorio =
                                    when (actividad.recordatorio) {
                                        AnticipacionRecordatorio.A_LA_HORA -> 1
                                        AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES -> 2
                                        AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES -> 3
                                        AnticipacionRecordatorio.UNA_HORA_ANTES -> 4
                                        AnticipacionRecordatorio.UN_DIA_ANTES -> 5
                                        null -> 0
                                    }

                                binding.recordatorioTarea.setSelection(posicionRecordatorio)

                                binding.btnGuardarTarea.text = "Guardar cambios"
                            }
                        }
                        .onFailure {
                            Toast.makeText(
                                this,
                                "No se pudo cargar la tarea",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        }

        binding.btnGuardarTarea.setOnClickListener {

            val titulo = binding.tituloTarea.text.toString()
            val descripcion = binding.descripcionTarea.text.toString()
            val fecha = binding.fechaTarea.text.toString()
            val hora = binding.horaTarea.text.toString()
            val prioridadSeleccionada =
                Prioridad.valueOf(binding.prioridadTarea.selectedItem.toString())
            val categoriaSeleccionada =
                categorias[binding.categoriaTarea.selectedItemPosition]

            val recordatorioSeleccionado =
                when (binding.recordatorioTarea.selectedItemPosition) {

                    1 -> AnticipacionRecordatorio.A_LA_HORA
                    2 -> AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES
                    3 -> AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES
                    4 -> AnticipacionRecordatorio.UNA_HORA_ANTES
                    5 -> AnticipacionRecordatorio.UN_DIA_ANTES

                    else -> null
                }

            // Validar título
            if (titulo.isBlank()) {
                binding.tituloTarea.error = "El título es obligatorio"
                return@setOnClickListener
            }

// Si hay hora, tiene que haber fecha
            if (hora.isNotBlank() && fecha.isBlank()) {
                binding.fechaTarea.error = "Tenés que ingresar una fecha"
                return@setOnClickListener
            }

// Si hay recordatorio, tiene que haber fecha y hora
            if (recordatorioSeleccionado != null &&
                (fecha.isBlank() || hora.isBlank())
            ) {
                binding.fechaTarea.error = "El recordatorio necesita fecha y hora"
                binding.horaTarea.error = "El recordatorio necesita fecha y hora"
                return@setOnClickListener
            }

            val fechaConvertida = convertirFecha(fecha) ?: if (fecha.isNotBlank()) {
                binding.fechaTarea.error = "Usá el formato dd/MM/aaaa"
                return@setOnClickListener
            } else {
                null
            }

            val horaConvertida = convertirHora(hora) ?: if (hora.isNotBlank()) {
                binding.horaTarea.error = "Usá el formato HH:mm"
                return@setOnClickListener
            } else {
                null
            }


            binding.btnGuardarTarea.isEnabled = false

            if (actividadEditando == null) {

                // CREAR una tarea nueva
                val nuevaTarea = Actividad(
                    tipo = TipoActividad.TAREA,
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = fechaConvertida,
                    hora = horaConvertida,
                    prioridad = prioridadSeleccionada,
                    categoriaId = categoriaSeleccionada?.id,
                    recordatorio = recordatorioSeleccionado,
                    estadoTarea = EstadoTarea.PENDIENTE
                )

                repositorio.guardar(nuevaTarea) { resultado ->
                    runOnUiThread {
                        resultado
                            .onSuccess {
                                setResult(RESULT_OK)
                                finish()
                            }
                            .onFailure {
                                binding.btnGuardarTarea.isEnabled = true

                                Toast.makeText(
                                    this,
                                    "No se pudo guardar la tarea",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }

            } else {

                // EDITAR la tarea existente
                val tareaActualizada = actividadEditando!!.copy(
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = fechaConvertida,
                    hora = horaConvertida,
                    prioridad = prioridadSeleccionada,
                    categoriaId = categoriaSeleccionada?.id,
                    recordatorio = recordatorioSeleccionado,
                    fechaModificacion = Instant.now()
                )

                repositorio.actualizar(tareaActualizada) { resultado ->
                    runOnUiThread {
                        resultado
                            .onSuccess {
                                setResult(RESULT_OK)
                                finish()
                            }
                            .onFailure {
                                binding.btnGuardarTarea.isEnabled = true

                                Toast.makeText(
                                    this,
                                    "No se pudo actualizar la tarea",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            }
        }
    }

    private fun convertirFecha(valor: String): LocalDate? =
        convertir(valor, FORMATO_FECHA, LocalDate::parse)

    private fun convertirHora(valor: String): LocalTime? =
        convertir(valor, FORMATO_HORA, LocalTime::parse)

    private fun <T> convertir(
        valor: String,
        formato: DateTimeFormatter,
        conversor: (CharSequence, DateTimeFormatter) -> T
    ): T? {
        if (valor.isBlank()) return null

        return try {
            conversor(valor.trim(), formato)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private companion object {
        val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT)

        val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter
            .ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT)
    }
}
