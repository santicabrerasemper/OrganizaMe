package com.santi.organizame

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.santi.organizame.databinding.ActividadNuevaTareaBinding
import com.santi.organizame.domain.recordatorios.ResultadoProgramacionRecordatorio
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.AnticipacionRecordatorio
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.Recurrencia
import com.santi.organizame.modelo.TipoActividad
import com.santi.organizame.ui.formulario.FormularioActividadEfecto
import com.santi.organizame.ui.formulario.FormularioActividadUiState
import com.santi.organizame.ui.formulario.FormularioActividadViewModel
import com.santi.organizame.ui.formulario.FormularioActividadViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import kotlinx.coroutines.launch

class NuevaTareaActividad : AppCompatActivity() {

    private lateinit var binding: ActividadNuevaTareaBinding
    private val viewModel: FormularioActividadViewModel by viewModels {
        FormularioActividadViewModelFactory(applicationContext)
    }

    private var actividadEditando: Actividad? = null
    private var categorias: List<Categoria?> = listOf(null)
    private lateinit var adaptadorCategorias: ArrayAdapter<String>
    private val tiposActividad = TipoActividad.values().toList()
    private var datosInicialesAplicados = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActividadNuevaTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarInsets()

        binding.btnVolverNuevaTarea.setOnClickListener { finish() }
        configurarSelectoresFechaHora()
        configurarSelectores()
        aplicarDatosDelParser()

        binding.btnGuardarTarea.isEnabled = false
        binding.categoriaTarea.isEnabled = false
        binding.btnGuardarTarea.setOnClickListener { guardarActividad() }

        observarViewModel()
        val actividadId = intent.getLongExtra(EXTRA_ACTIVIDAD_ID, -1L)
        viewModel.cargar(actividadId.takeIf { it != -1L })
    }

    private fun configurarInsets() {
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
    }

    private fun configurarSelectores() {
        binding.tipoActividad.adapter = crearAdaptador(
            tiposActividad.map { if (it == TipoActividad.TAREA) "Tarea" else "Evento" }
        )

        binding.prioridadTarea.adapter = crearAdaptador(
            Prioridad.values().map { it.name }
        )
        binding.prioridadTarea.setSelection(
            Prioridad.values().indexOf(Prioridad.MEDIA)
        )

        adaptadorCategorias = crearAdaptador(listOf("Sin categoría"))
        binding.categoriaTarea.adapter = adaptadorCategorias

        binding.recordatorioTarea.adapter = crearAdaptador(
            listOf(
                "Sin recordatorio",
                "A la hora",
                "10 minutos antes",
                "30 minutos antes",
                "1 hora antes",
                "1 día antes"
            )
        )

        binding.recurrenciaTarea.adapter = crearAdaptador(
            listOf(
                "No repetir",
                "Todos los días",
                "Todas las semanas",
                "Todos los meses"
            )
        )

        binding.tipoActividad.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val esTarea = tiposActividad[position] == TipoActividad.TAREA
                    binding.recurrenciaTarea.isEnabled = esTarea
                    binding.ayudaRecurrencia.visibility =
                        if (esTarea) View.VISIBLE else View.GONE
                    if (!esTarea) binding.recurrenciaTarea.setSelection(0)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun crearAdaptador(elementos: List<String>): ArrayAdapter<String> =
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            elementos.toMutableList()
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    private fun aplicarDatosDelParser() {
        if (!intent.hasExtra(EXTRA_PARSER_TEXTO_ORIGINAL)) return

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

        binding.tituloTarea.setText(intent.getStringExtra(EXTRA_PARSER_TITULO).orEmpty())
        binding.descripcionTarea.setText(
            intent.getStringExtra(EXTRA_PARSER_DESCRIPCION).orEmpty()
        )

        intent.getStringExtra(EXTRA_PARSER_RECORDATORIO)
            ?.let { runCatching { AnticipacionRecordatorio.valueOf(it) }.getOrNull() }
            ?.let { recordatorio ->
                binding.recordatorioTarea.setSelection(
                    when (recordatorio) {
                        AnticipacionRecordatorio.A_LA_HORA -> 1
                        AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES -> 2
                        AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES -> 3
                        AnticipacionRecordatorio.UNA_HORA_ANTES -> 4
                        AnticipacionRecordatorio.UN_DIA_ANTES -> 5
                    }
                )
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

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.estado.collect(::renderizarEstado) }
                launch { viewModel.efectos.collect(::procesarEfecto) }
            }
        }
    }

    private fun renderizarEstado(estado: FormularioActividadUiState) {
        val nuevasCategorias: List<Categoria?> = listOf(null) + estado.categorias
        val nuevosNombres = nuevasCategorias.map { it?.nombre ?: "Sin categoría" }
        if (nuevosNombres != categorias.map { it?.nombre ?: "Sin categoría" }) {
            categorias = nuevasCategorias
            adaptadorCategorias.clear()
            adaptadorCategorias.addAll(nuevosNombres)
            adaptadorCategorias.notifyDataSetChanged()
        }

        if (estado.cargaCompletada && !datosInicialesAplicados) {
            datosInicialesAplicados = true
            actividadEditando = estado.actividad
            estado.actividad?.let(::aplicarActividadEditada)
                ?: seleccionarCategoriaDelParser()
        }

        val formularioDisponible = estado.cargaCompletada && !estado.guardando
        binding.btnGuardarTarea.isEnabled = formularioDisponible
        binding.categoriaTarea.isEnabled = formularioDisponible
    }

    private fun aplicarActividadEditada(actividad: Actividad) {
        binding.tipoActividad.setSelection(tiposActividad.indexOf(actividad.tipo))
        binding.tituloTarea.setText(actividad.titulo)
        binding.descripcionTarea.setText(actividad.descripcion.orEmpty())
        binding.fechaTarea.setText(actividad.fecha?.format(FORMATO_FECHA).orEmpty())
        binding.horaTarea.setText(actividad.hora?.format(FORMATO_HORA).orEmpty())
        binding.prioridadTarea.setSelection(
            Prioridad.values().indexOf(actividad.prioridad)
        )
        seleccionarCategoria(actividad.categoriaId)
        binding.recordatorioTarea.setSelection(
            when (actividad.recordatorio) {
                AnticipacionRecordatorio.A_LA_HORA -> 1
                AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES -> 2
                AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES -> 3
                AnticipacionRecordatorio.UNA_HORA_ANTES -> 4
                AnticipacionRecordatorio.UN_DIA_ANTES -> 5
                null -> 0
            }
        )
        binding.recurrenciaTarea.setSelection(
            Recurrencia.values().indexOf(actividad.recurrencia)
        )
        binding.btnGuardarTarea.text = "Guardar cambios"
        binding.tituloFormulario.text = "Editar actividad"
    }

    private fun seleccionarCategoriaDelParser() {
        val categoriaParserId = intent.getLongExtra(EXTRA_PARSER_CATEGORIA_ID, -1L)
        if (categoriaParserId >= 0L) seleccionarCategoria(categoriaParserId)
    }

    private fun seleccionarCategoria(categoriaId: Long?) {
        val posicion = categorias.indexOfFirst { it?.id == categoriaId }
        binding.categoriaTarea.setSelection(if (posicion >= 0) posicion else 0)
    }

    private fun procesarEfecto(efecto: FormularioActividadEfecto) {
        when (efecto) {
            is FormularioActividadEfecto.Guardada -> {
                if (efecto.programacion == ResultadoProgramacionRecordatorio.APROXIMADO) {
                    Toast.makeText(
                        this,
                        "Permití alarmas exactas para recibir el recordatorio a la hora indicada",
                        Toast.LENGTH_LONG
                    ).show()
                    ProgramadorRecordatorios(applicationContext)
                        .solicitarAccesoAlarmasExactas()
                }
                setResult(RESULT_OK)
                finish()
            }

            is FormularioActividadEfecto.Mensaje -> {
                Toast.makeText(this, efecto.texto, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun guardarActividad() {
        val titulo = binding.tituloTarea.text.toString()
        val descripcion = binding.descripcionTarea.text.toString()
        val fechaTexto = binding.fechaTarea.text.toString()
        val horaTexto = binding.horaTarea.text.toString()
        val prioridad = Prioridad.values()[binding.prioridadTarea.selectedItemPosition]
        val categoria = categorias.getOrNull(binding.categoriaTarea.selectedItemPosition)
        val tipo = tiposActividad[binding.tipoActividad.selectedItemPosition]
        val recurrencia = Recurrencia.values()[binding.recurrenciaTarea.selectedItemPosition]
        val recordatorio = recordatorioSeleccionado()

        if (titulo.isBlank()) {
            binding.tituloTarea.error = "El título es obligatorio"
            return
        }
        if (tipo == TipoActividad.EVENTO && fechaTexto.isBlank()) {
            binding.fechaTarea.error = "La fecha es obligatoria para un evento"
            return
        }
        if (recurrencia != Recurrencia.SIN_REPETICION &&
            (tipo != TipoActividad.TAREA || fechaTexto.isBlank())
        ) {
            binding.fechaTarea.error = "Una tarea repetitiva necesita una fecha inicial"
            return
        }
        if (horaTexto.isNotBlank() && fechaTexto.isBlank()) {
            binding.fechaTarea.error = "Tenés que ingresar una fecha"
            return
        }
        if (recordatorio != null && (fechaTexto.isBlank() || horaTexto.isBlank())) {
            binding.fechaTarea.error = "El recordatorio necesita fecha y hora"
            binding.horaTarea.error = "El recordatorio necesita fecha y hora"
            return
        }

        val fecha = convertirFecha(fechaTexto) ?: if (fechaTexto.isNotBlank()) {
            binding.fechaTarea.error = "Usá el formato dd/MM/aaaa"
            return
        } else null
        val hora = convertirHora(horaTexto) ?: if (horaTexto.isNotBlank()) {
            binding.horaTarea.error = "Usá el formato HH:mm"
            return
        } else null
        val momentoRecordatorio = ProgramadorRecordatorios.calcularMomentoAviso(
            fecha,
            hora,
            recordatorio
        )

        if (recordatorio != null &&
            (momentoRecordatorio == null || !momentoRecordatorio.isAfter(Instant.now()))
        ) {
            binding.fechaTarea.error = "El momento del recordatorio ya pasó"
            binding.horaTarea.error = "Elegí una fecha y hora futuras"
            return
        }

        val original = actividadEditando
        val actividad = if (original == null) {
            Actividad(
                tipo = tipo,
                titulo = titulo,
                descripcion = descripcion,
                fecha = fecha,
                hora = hora,
                prioridad = prioridad,
                categoriaId = categoria?.id,
                recordatorio = recordatorio,
                fechaRecordatorioProgramado = momentoRecordatorio,
                recurrencia = recurrencia,
                estadoTarea = if (tipo == TipoActividad.TAREA) {
                    EstadoTarea.PENDIENTE
                } else null
            )
        } else {
            val esTarea = tipo == TipoActividad.TAREA
            original.copy(
                tipo = tipo,
                titulo = titulo,
                descripcion = descripcion,
                fecha = fecha,
                hora = hora,
                prioridad = prioridad,
                categoriaId = categoria?.id,
                recordatorio = recordatorio,
                recurrencia = if (esTarea) recurrencia else Recurrencia.SIN_REPETICION,
                recurrenciaProcesada = if (original.estadoTarea == EstadoTarea.COMPLETADA) {
                    original.recurrenciaProcesada
                } else false,
                estadoTarea = if (esTarea) {
                    original.estadoTarea ?: EstadoTarea.PENDIENTE
                } else null,
                fechaCompletada = if (esTarea) original.fechaCompletada else null,
                fechaRecordatorioProgramado = if (
                    esTarea && original.estadoTarea == EstadoTarea.COMPLETADA
                ) null else momentoRecordatorio,
                fechaModificacion = Instant.now()
            )
        }

        viewModel.guardar(actividad)
    }

    private fun recordatorioSeleccionado(): AnticipacionRecordatorio? =
        when (binding.recordatorioTarea.selectedItemPosition) {
            1 -> AnticipacionRecordatorio.A_LA_HORA
            2 -> AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES
            3 -> AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES
            4 -> AnticipacionRecordatorio.UNA_HORA_ANTES
            5 -> AnticipacionRecordatorio.UN_DIA_ANTES
            else -> null
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
        private const val EXTRA_ACTIVIDAD_ID = "actividadId"
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
