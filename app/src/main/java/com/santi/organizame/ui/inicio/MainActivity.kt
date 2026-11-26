
package com.santi.organizame.ui.inicio

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.santi.organizame.databinding.ActivityMainBinding
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.AnticipacionRecordatorio
import com.santi.organizame.modelo.TipoActividad
import androidx.appcompat.app.AppCompatDelegate
import com.santi.organizame.parser.ResultadoParser
import com.santi.organizame.ui.inicio.InicioEfecto
import com.santi.organizame.ui.inicio.InicioViewModel
import com.santi.organizame.ui.inicio.InicioViewModelFactory
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import com.santi.organizame.data.recordatorios.ReceptorRecordatorio
import com.santi.organizame.ui.calendario.CalendarioActividad
import com.santi.organizame.ui.categorias.CategoriasActividad
import com.santi.organizame.ui.formulario.NuevaTareaActividad
import com.santi.organizame.ui.hoy.HoyActividad
import com.santi.organizame.ui.lista.ListaActividadesActividad
import com.santi.organizame.ui.parser.AyudaParserActividad
import com.santi.organizame.ui.respaldo.RespaldoActividad
import com.santi.organizame.ui.resumen.ResumenSemanalActividad

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: InicioViewModel by viewModels {
        InicioViewModelFactory(applicationContext)
    }

    // Abrir el formulario de tareas
    private val nuevaTareaLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val preferencias = getSharedPreferences(
            "ajustes_organizame",
            MODE_PRIVATE
        )

        val modoOscuro = preferencias.getBoolean("modo_oscuro", false)

        AppCompatDelegate.setDefaultNightMode(
            if (modoOscuro)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnModoOscuro.text =
            if (modoOscuro) "Desactivar modo oscuro"
            else "Activar modo oscuro"

        binding.btnModoOscuro.setOnClickListener {

            val nuevoModo = !preferencias.getBoolean(
                "modo_oscuro",
                false
            )

            preferencias.edit()
                .putBoolean("modo_oscuro", nuevoModo)
                .apply()

            AppCompatDelegate.setDefaultNightMode(
                if (nuevoModo)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        observarViewModel()

        // Configurar notificaciones

        solicitarPermisoNotificaciones()

        // Botón Hoy
        binding.btnHoy.setOnClickListener {
            startActivity(
                Intent(this, HoyActividad::class.java)
            )
        }

        // Botón Ver tareas y eventos
        binding.btnVerActividades.setOnClickListener {
            startActivity(
                Intent(this, ListaActividadesActividad::class.java)
            )
        }

        // Botón Nueva tarea
        binding.btnNuevaTarea.setOnClickListener {
            val intent = Intent(
                this,
                NuevaTareaActividad::class.java
            )
            nuevaTareaLauncher.launch(intent)
        }

        // Botón Interpretar frase
        binding.btnInterpretar.setOnClickListener {
            interpretarFraseRapida()
        }

        // Botón Ayuda del intérprete
        binding.btnAyudaParser.setOnClickListener {
            startActivity(
                Intent(this, AyudaParserActividad::class.java)
            )
        }

        // Botón Categorías
        binding.btnCategorias.setOnClickListener {
            startActivity(
                Intent(this, CategoriasActividad::class.java)
            )
        }

        // Botón Calendario
        binding.btnCalendario.setOnClickListener {
            startActivity(
                Intent(this, CalendarioActividad::class.java)
            )
        }

        binding.btnResumenSemanal.setOnClickListener {
            startActivity(Intent(this, ResumenSemanalActividad::class.java))
        }

        binding.btnRespaldo.setOnClickListener {
            startActivity(Intent(this, RespaldoActividad::class.java))
        }

        // Abrir detalles si llegó desde un recordatorio
        procesarIntentRecordatorio(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarIntentRecordatorio(intent)
    }

    // Interpretar una frase escrita por el usuario
    private fun interpretarFraseRapida() {
        val frase = binding.entradaRapida.text.toString().trim()
        if (frase.isBlank()) {
            binding.entradaRapida.error = "Ingresá una frase"
            return
        }
        viewModel.interpretar(frase)
    }

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.estado.collect {
                        binding.btnInterpretar.isEnabled = !it.interpretando
                    }
                }
                launch { viewModel.efectos.collect(::procesarEfecto) }
            }
        }
    }

    private fun procesarEfecto(efecto: InicioEfecto) {
        when (efecto) {
            is InicioEfecto.AbrirFormulario -> abrirFormulario(efecto.resultado)
            is InicioEfecto.MostrarDetalle -> mostrarDetalle(
                efecto.detalle.actividad,
                efecto.detalle.nombreCategoria
            )
            is InicioEfecto.Mensaje -> Toast.makeText(
                this,
                efecto.texto,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun abrirFormulario(interpretacion: ResultadoParser) {
        val intent = Intent(this, NuevaTareaActividad::class.java).apply {
            putExtra(
                NuevaTareaActividad.EXTRA_PARSER_TEXTO_ORIGINAL,
                interpretacion.textoOriginal
            )
            putExtra(
                NuevaTareaActividad.EXTRA_PARSER_TITULO,
                interpretacion.titulo.valor.orEmpty()
            )
            putExtra(
                NuevaTareaActividad.EXTRA_PARSER_TIPO,
                interpretacion.tipo.valor?.name
            )
            putExtra(
                NuevaTareaActividad.EXTRA_PARSER_FECHA,
                interpretacion.fecha.valor?.toString()
            )
            putExtra(
                NuevaTareaActividad.EXTRA_PARSER_HORA,
                interpretacion.hora.valor?.toString()
            )
            putExtra(
                NuevaTareaActividad.EXTRA_PARSER_PRIORIDAD,
                interpretacion.prioridad.valor?.name
            )
            putExtra(
                NuevaTareaActividad.EXTRA_PARSER_CATEGORIA_ID,
                interpretacion.categoriaId.valor ?: -1L
            )
            putStringArrayListExtra(
                NuevaTareaActividad.EXTRA_PARSER_ADVERTENCIAS,
                ArrayList(interpretacion.advertencias)
            )
            putExtra(
                NuevaTareaActividad.EXTRA_PARSER_DESCRIPCION,
                interpretacion.descripcion.valor
            )
            putExtra(
                NuevaTareaActividad.EXTRA_PARSER_RECORDATORIO,
                interpretacion.recordatorio.valor?.name
            )
        }
        nuevaTareaLauncher.launch(intent)
    }

    // Crear el canal de notificaciones
    private fun crearCanalRecordatorios() {

        val canal = NotificationChannel(
            ReceptorRecordatorio.CANAL_ID,
            "Recordatorios de actividades",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description =
                "Avisos de las tareas y eventos de OrganizaMe"
        }

        val administrador =
            getSystemService(NotificationManager::class.java)

        administrador.createNotificationChannel(canal)
    }

    // Procesar una notificación recibida
    private fun procesarIntentRecordatorio(intent: Intent?) {

        val actividadId = intent?.getLongExtra(
            ReceptorRecordatorio.EXTRA_ACTIVIDAD_ID,
            -1L
        ) ?: -1L

        if (actividadId <= 0L) return

        intent?.removeExtra(
            ReceptorRecordatorio.EXTRA_ACTIVIDAD_ID
        )

        viewModel.abrirActividad(actividadId)
    }

    // Solicitar permiso para mostrar notificaciones
    private fun solicitarPermisoNotificaciones() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permisoConcedido =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!permisoConcedido) {
                permisoNotificaciones.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private val permisoNotificaciones =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permitido ->

            if (!permitido) {
                Toast.makeText(
                    this,
                    "Las notificaciones están desactivadas",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // Mostrar detalle de una tarea o evento
    private fun mostrarDetalle(
        actividad: Actividad,
        nombreCategoria: String
    ) {

        AlertDialog.Builder(this)
            .setTitle("Detalle de la actividad")
            .setMessage(
                formatearActividad(
                    actividad,
                    nombreCategoria
                )
            )
            .setPositiveButton("Cerrar", null)
            .show()
    }

    // Preparar el texto del detalle


    private fun formatearActividad(
        actividad: Actividad,
        nombreCategoria: String
    ): String = buildString {

        // Tipo de actividad
        val tipo = if (actividad.tipo == TipoActividad.TAREA) {
            "📋  Tarea"
        } else {
            "📅  Evento"
        }

        append(tipo)
        append("\n\n")

        // Fecha y hora
        append("📅  ")

        append(
            actividad.fecha?.format(FORMATO_FECHA)
                ?: "Sin fecha"
        )

        actividad.hora?.let {
            append("   ·   ")
            append(it.format(FORMATO_HORA))
            append(" hs")
        }

        append("\n\n")

        // Prioridad
        val iconoPrioridad = when (actividad.prioridad.name) {
            "ALTA" -> "🔴"
            "MEDIA" -> "🟡"
            else -> "🟢"
        }

        append("$iconoPrioridad  Prioridad: ")
        append(actividad.prioridad)
        append("\n")

        // Categoría
        append("📁  Categoría: ")
        append(nombreCategoria)
        append("\n")

        // Recordatorio
        append("🔔  Recordatorio: ")
        append(nombreRecordatorio(actividad.recordatorio))

        // Estado de la tarea
        actividad.estadoTarea?.let {
            append("\n")

            val iconoEstado = if (it.name == "COMPLETADA") {
                "✅"
            } else {
                "⏳"
            }

            append("$iconoEstado  Estado: ")
            append(it)
        }

        // Descripción separada
        actividad.descripcion
            ?.takeIf { it.isNotBlank() }
            ?.let {
                append("\n\n")
                append("📝  DESCRIPCIÓN")
                append("\n")
                append(it)
            }
    }

    // Mostrar el nombre del recordatorio
    private fun nombreRecordatorio(
        recordatorio: AnticipacionRecordatorio?
    ): String = when (recordatorio) {

        AnticipacionRecordatorio.A_LA_HORA ->
            "A la hora"

        AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES ->
            "10 minutos antes"

        AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES ->
            "30 minutos antes"

        AnticipacionRecordatorio.UNA_HORA_ANTES ->
            "1 hora antes"

        AnticipacionRecordatorio.UN_DIA_ANTES ->
            "1 día antes"

        null -> "Sin recordatorio"
    }

    private companion object {

        val FORMATO_FECHA: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")

        val FORMATO_HORA: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm")
    }
}
