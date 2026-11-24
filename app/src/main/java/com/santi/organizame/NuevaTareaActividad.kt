package com.santi.organizame

import android.os.Bundle
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import com.santi.organizame.databinding.ActividadNuevaTareaBinding
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.AnticipacionRecordatorio
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import android.widget.Toast
import android.view.View
import java.time.Instant
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.santi.organizame.modelo.CategoriaRepository
import com.santi.organizame.modelo.ActividadRepository
import com.santi.organizame.modelo.OrganizaMeDatabase
import com.santi.organizame.modelo.Recurrencia

class NuevaTareaActividad : AppCompatActivity() {

    private lateinit var binding: ActividadNuevaTareaBinding
    private lateinit var repositorio: ActividadRepository
    private var actividadEditando: Actividad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActividadNuevaTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val vista = binding.root

        val izquierda = vista.paddingLeft
        val arriba = vista.paddingTop
        val derecha = vista.paddingRight
        val abajo = vista.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(vista) { _, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            vista.setPadding(
                izquierda + barras.left,
                arriba + barras.top,
                derecha + barras.right,
                abajo + barras.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(vista)


        binding.btnVolverNuevaTarea.setOnClickListener {
            finish()
        }

        configurarSelectoresFechaHora()

        val tiposActividad = TipoActividad.values().toList()

        val nombresTipos = tiposActividad.map { tipo ->
            if (tipo == TipoActividad.TAREA) "Tarea" else "Evento"
        }

        val adaptadorTipos = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombresTipos
        )

        adaptadorTipos.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.tipoActividad.adapter = adaptadorTipos

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

        var categorias: List<Categoria?> = listOf(null)
        val categoriaParserId = intent.getLongExtra(EXTRA_PARSER_CATEGORIA_ID, -1L)

        val adaptadorCategorias = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf("Sin categoría")
        )

        adaptadorCategorias.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.categoriaTarea.adapter = adaptadorCategorias

// Repositorio para consultar las categorías de Room
        val categoriaRepository = CategoriaRepository(
            OrganizaMeDatabase.obtener(applicationContext).categoriaDao()
        )

// Evitamos guardar hasta terminar de cargar las categorías
        binding.btnGuardarTarea.isEnabled = false
        binding.categoriaTarea.isEnabled = false

        categoriaRepository.obtenerTodas { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { categoriasGuardadas ->

                        categorias = listOf(null) + categoriasGuardadas

                        adaptadorCategorias.clear()
                        adaptadorCategorias.addAll(
                            categorias.map { it?.nombre ?: "Sin categoría" }
                        )
                        adaptadorCategorias.notifyDataSetChanged()

                        // Si estamos editando, recuperar la categoría seleccionada
                        actividadEditando?.let { actividad ->
                            val posicion = categorias.indexOfFirst {
                                it?.id == actividad.categoriaId
                            }

                            binding.categoriaTarea.setSelection(
                                if (posicion >= 0) posicion else 0
                            )
                        }

                        if (actividadEditando == null && categoriaParserId >= 0L) {
                            val posicion = categorias.indexOfFirst {
                                it?.id == categoriaParserId
                            }
                            binding.categoriaTarea.setSelection(
                                if (posicion >= 0) posicion else 0
                            )
                        }

                        binding.categoriaTarea.isEnabled = true
                        binding.btnGuardarTarea.isEnabled = true
                    }
                    .onFailure {
                        Toast.makeText(
                            this,
                            "No se pudieron cargar las categorías",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }


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

        val opcionesRecurrencia = listOf(
            "No repetir",
            "Todos los días",
            "Todas las semanas",
            "Todos los meses"
        )

        val adaptadorRecurrencia = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesRecurrencia
        )
        adaptadorRecurrencia.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.recurrenciaTarea.adapter = adaptadorRecurrencia

        binding.tipoActividad.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val esTarea = TipoActividad.values()[position] == TipoActividad.TAREA
                    binding.recurrenciaTarea.isEnabled = esTarea
                    binding.ayudaRecurrencia.visibility =
                        if (esTarea) View.VISIBLE else View.GONE
                    if (!esTarea) binding.recurrenciaTarea.setSelection(0)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

        if (intent.hasExtra(EXTRA_PARSER_TEXTO_ORIGINAL)) {
            val textoOriginal = intent.getStringExtra(EXTRA_PARSER_TEXTO_ORIGINAL).orEmpty()
            val advertencias =
                intent.getStringArrayListExtra(EXTRA_PARSER_ADVERTENCIAS).orEmpty()

            binding.resumenInterpretacion.visibility = View.VISIBLE
            binding.tituloFormulario.text = "Revisar interpretación"
            binding.resumenInterpretacion.text = buildString {
                append("Frase interpretada: “").append(textoOriginal).append("”")
                append("\n\nRevisá todos los campos antes de guardar.")
                if (advertencias.isNotEmpty()) {
                    append("\n\nAdvertencias:")
                    advertencias.forEach { append("\n• ").append(it) }
                }
            }

            binding.tituloTarea.setText(
                intent.getStringExtra(EXTRA_PARSER_TITULO).orEmpty()
            )
            binding.descripcionTarea.setText(
                intent.getStringExtra(EXTRA_PARSER_DESCRIPCION).orEmpty()
            )

            intent.getStringExtra(EXTRA_PARSER_RECORDATORIO)
                ?.let {
                    runCatching {
                        AnticipacionRecordatorio.valueOf(it)
                    }.getOrNull()
                }
                ?.let { recordatorio ->
                    val posicion = when (recordatorio) {
                        AnticipacionRecordatorio.A_LA_HORA -> 1
                        AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES -> 2
                        AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES -> 3
                        AnticipacionRecordatorio.UNA_HORA_ANTES -> 4
                        AnticipacionRecordatorio.UN_DIA_ANTES -> 5
                    }

                    binding.recordatorioTarea.setSelection(posicion)
                }

            intent.getStringExtra(EXTRA_PARSER_TIPO)
                ?.let { runCatching { TipoActividad.valueOf(it) }.getOrNull() }
                ?.let { binding.tipoActividad.setSelection(tiposActividad.indexOf(it)) }

            intent.getStringExtra(EXTRA_PARSER_FECHA)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?.let { binding.fechaTarea.setText(it.format(FORMATO_FECHA)) }

            intent.getStringExtra(EXTRA_PARSER_HORA)
                ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
                ?.let { binding.horaTarea.setText(it.format(FORMATO_HORA)) }

            intent.getStringExtra(EXTRA_PARSER_PRIORIDAD)
                ?.let { runCatching { Prioridad.valueOf(it) }.getOrNull() }
                ?.let { binding.prioridadTarea.setSelection(Prioridad.values().indexOf(it)) }

            binding.btnGuardarTarea.text = "Confirmar y guardar"
        }

        val actividadId = intent.getLongExtra("actividadId", -1L)

        if (actividadId != -1L) {
            repositorio.obtenerPorId(actividadId) { resultado ->
                runOnUiThread {
                    resultado
                        .onSuccess { actividad ->
                            if (actividad != null) {
                                actividadEditando = actividad

                                binding.tipoActividad.setSelection(
                                    tiposActividad.indexOf(actividad.tipo)
                                )

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
                                binding.recurrenciaTarea.setSelection(
                                    Recurrencia.values().indexOf(actividad.recurrencia)
                                )

                                binding.btnGuardarTarea.text = "Guardar cambios"
                                binding.tituloFormulario.text = "Editar actividad"
                            }
                        }
                        .onFailure {
                            Toast.makeText(
                                this,
                                "No se pudo cargar la actividad",
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
            val tipoSeleccionado =
                TipoActividad.values()[binding.tipoActividad.selectedItemPosition]
            val recurrenciaSeleccionada =
                Recurrencia.values()[binding.recurrenciaTarea.selectedItemPosition]
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

            if(tipoSeleccionado == TipoActividad.EVENTO && fecha.isBlank()){
                binding.fechaTarea.error = "La fecha es obligatoria para un evento"
                return@setOnClickListener
            }

            if (recurrenciaSeleccionada != Recurrencia.SIN_REPETICION &&
                (tipoSeleccionado != TipoActividad.TAREA || fecha.isBlank())
            ) {
                binding.fechaTarea.error =
                    "Una tarea repetitiva necesita una fecha inicial"
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

            val momentoRecordatorio = ProgramadorRecordatorios.calcularMomentoAviso(
                fechaConvertida,
                horaConvertida,
                recordatorioSeleccionado
            )

            if (recordatorioSeleccionado != null &&
                (momentoRecordatorio == null || !momentoRecordatorio.isAfter(Instant.now()))
            ) {
                binding.fechaTarea.error = "El momento del recordatorio ya pasó"
                binding.horaTarea.error = "Elegí una fecha y hora futuras"
                return@setOnClickListener
            }


            binding.btnGuardarTarea.isEnabled = false

            if (actividadEditando == null) {

                // CREAR una tarea nueva
                val nuevaActividad = Actividad(
                    tipo = tipoSeleccionado,
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = fechaConvertida,
                    hora = horaConvertida,
                    prioridad = prioridadSeleccionada,
                    categoriaId = categoriaSeleccionada?.id,
                    recordatorio = recordatorioSeleccionado,
                    fechaRecordatorioProgramado = momentoRecordatorio,
                    recurrencia = recurrenciaSeleccionada,
                    estadoTarea =
                        if (tipoSeleccionado == TipoActividad.TAREA) {
                            EstadoTarea.PENDIENTE
                        } else {
                            null
                        }
                )

                repositorio.guardar(nuevaActividad) { resultado ->
                    runOnUiThread {
                        resultado
                            .onSuccess { idGuardado ->

                                val actividadGuardada = nuevaActividad.copy(
                                    id = idGuardado
                                )

                                programarRecordatorio(actividadGuardada)

                                setResult(RESULT_OK)
                                finish()
                            }
                            .onFailure {
                                binding.btnGuardarTarea.isEnabled = true

                                Toast.makeText(
                                    this,
                                    "No se pudo guardar la actividad",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }

            } else {

                val actividadOriginal = actividadEditando!!
                val esTarea = tipoSeleccionado == TipoActividad.TAREA

                val actividadActualizada = actividadOriginal.copy(
                    tipo = tipoSeleccionado,
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = fechaConvertida,
                    hora = horaConvertida,
                    prioridad = prioridadSeleccionada,
                    categoriaId = categoriaSeleccionada?.id,
                    recordatorio = recordatorioSeleccionado,
                    recurrencia =
                        if (esTarea) recurrenciaSeleccionada
                        else Recurrencia.SIN_REPETICION,
                    recurrenciaProcesada =
                        if (actividadOriginal.estadoTarea == EstadoTarea.COMPLETADA) {
                            actividadOriginal.recurrenciaProcesada
                        } else {
                            false
                        },
                    estadoTarea =
                        if (esTarea) {
                            actividadOriginal.estadoTarea ?: EstadoTarea.PENDIENTE
                        } else {
                            null
                        },
                    fechaCompletada =
                        if (esTarea) actividadOriginal.fechaCompletada else null,
                    fechaRecordatorioProgramado =
                        if (esTarea &&
                            actividadOriginal.estadoTarea == EstadoTarea.COMPLETADA
                        ) {
                            null
                        } else {
                            momentoRecordatorio
                        },
                    fechaModificacion = Instant.now()
                )

                repositorio.actualizar(actividadActualizada) { resultado ->
                    runOnUiThread {
                        resultado
                            .onSuccess {

                                programarRecordatorio(actividadActualizada)

                                setResult(RESULT_OK)
                                finish()
                            }
                            .onFailure {
                                binding.btnGuardarTarea.isEnabled = true

                                Toast.makeText(
                                    this,
                                    "No se pudo actualizar la actividad",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            }
        }
    }

    private fun programarRecordatorio(actividad: Actividad) {
        val programador = ProgramadorRecordatorios(applicationContext)
        val resultado = programador.programar(actividad)

        if (resultado == ProgramadorRecordatorios.ResultadoProgramacion.APROXIMADO) {
            Toast.makeText(
                this,
                "Permití alarmas exactas para recibir el recordatorio a la hora indicada",
                Toast.LENGTH_LONG
            ).show()
            programador.solicitarAccesoAlarmasExactas()
        }
    }

    private fun configurarSelectoresFechaHora() {
        binding.fechaTarea.setOnClickListener {
            val inicial = convertirFecha(binding.fechaTarea.text.toString())
                ?: LocalDate.now()
            DatePickerDialog(
                this,
                { _, anio, mes, dia ->
                    binding.fechaTarea.setText(
                        LocalDate.of(anio, mes + 1, dia).format(FORMATO_FECHA)
                    )
                    binding.fechaTarea.error = null
                },
                inicial.year,
                inicial.monthValue - 1,
                inicial.dayOfMonth
            ).show()
        }

        binding.horaTarea.setOnClickListener {
            val inicial = convertirHora(binding.horaTarea.text.toString())
                ?: LocalTime.now().withSecond(0).withNano(0)
            TimePickerDialog(
                this,
                { _, hora, minuto ->
                    binding.horaTarea.setText(
                        LocalTime.of(hora, minuto).format(FORMATO_HORA)
                    )
                    binding.horaTarea.error = null
                },
                inicial.hour,
                inicial.minute,
                true
            ).show()
        }

        binding.btnLimpiarFechaHora.setOnClickListener {
            binding.fechaTarea.text?.clear()
            binding.horaTarea.text?.clear()
            binding.recordatorioTarea.setSelection(0)
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

    companion object {
        const val EXTRA_PARSER_TEXTO_ORIGINAL = "parserTextoOriginal"
        const val EXTRA_PARSER_TITULO = "parserTitulo"
        const val EXTRA_PARSER_TIPO = "parserTipo"
        const val EXTRA_PARSER_FECHA = "parserFecha"
        const val EXTRA_PARSER_HORA = "parserHora"
        const val EXTRA_PARSER_PRIORIDAD = "parserPrioridad"
        const val EXTRA_PARSER_CATEGORIA_ID = "parserCategoriaId"
        const val EXTRA_PARSER_ADVERTENCIAS = "parserAdvertencias"

        const val EXTRA_PARSER_DESCRIPCION = "parserDescripcion"
        const val EXTRA_PARSER_RECORDATORIO = "parserRecordatorio"

        val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT)

        val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter
            .ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT)
    }
}
