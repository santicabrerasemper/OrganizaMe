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
import com.santi.organizame.modelo.CategoriaRepository
import java.time.format.DateTimeFormatter
import android.view.View
import android.widget.Toast
import com.santi.organizame.modelo.EstadoTarea
import java.time.Instant
import com.santi.organizame.modelo.TipoActividad
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.santi.organizame.modelo.Prioridad
import android.text.Editable
import android.text.TextWatcher
import com.santi.organizame.modelo.Categoria
import android.app.NotificationChannel
import android.app.NotificationManager
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var actividadRepository: ActividadRepository

    private var actividadesGuardadas: List<Actividad> = emptyList()
    private var categoriasFiltro: List<Categoria> = emptyList()

    private lateinit var categoriaRepository: CategoriaRepository

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
        crearCanalRecordatorios()
        solicitarPermisoNotificaciones()

        actividadRepository = ActividadRepository(
            OrganizaMeDatabase.obtener(applicationContext).actividadDao()
        )
        categoriaRepository = CategoriaRepository(
            OrganizaMeDatabase.obtener(applicationContext).categoriaDao()
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
                    .setTitle("Eliminar actividad")
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
                                            "No se pudo eliminar la actividad",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            },

            alCambiarEstado = { actividad ->

                val actividadActualizada =
                    if (actividad.estadoTarea == EstadoTarea.PENDIENTE) {

                        actividad.copy(
                            estadoTarea = EstadoTarea.COMPLETADA,
                            fechaCompletada = Instant.now(),
                            fechaModificacion = Instant.now()
                        )

                    } else {

                        actividad.copy(
                            estadoTarea = EstadoTarea.PENDIENTE,
                            fechaCompletada = null,
                            fechaModificacion = Instant.now()
                        )
                    }

                actividadRepository.actualizar(actividadActualizada) { resultado ->
                    runOnUiThread {
                        resultado
                            .onSuccess {
                                cargarActividades()
                            }
                            .onFailure {
                                Toast.makeText(
                                    this,
                                    "No se pudo cambiar el estado de la tarea",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            },

            alVerDetalle = { actividad ->

                val categoriaId = actividad.categoriaId

                if (categoriaId == null) {
                    mostrarDetalle(actividad, "Sin categoría")
                } else {
                    categoriaRepository.obtenerPorId(categoriaId) { resultado ->
                        runOnUiThread {
                            resultado
                                .onSuccess { categoria ->
                                    val nombre = categoria?.nombre ?: "Sin categoría"
                                    mostrarDetalle(actividad, nombre)
                                }
                                .onFailure {
                                    Toast.makeText(
                                        this,
                                        "No se pudo consultar la categoría",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }
            }
        )

        binding.listaActividades.layoutManager = LinearLayoutManager(this)
        binding.listaActividades.adapter = adaptador

        binding.filtroCategoria.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    aplicarFiltro()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        val opcionesEstado = listOf(
            "Todos los estados",
            "Pendientes",
            "Completadas"
        )

        val adaptadorEstado = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesEstado
        )

        adaptadorEstado.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.filtroEstado.adapter = adaptadorEstado

        binding.filtroEstado.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    aplicarFiltro()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        val opcionesPrioridad = listOf(
            "Todas las prioridades",
            "Alta",
            "Media",
            "Baja"
        )

        val adaptadorPrioridad = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesPrioridad
        )

        adaptadorPrioridad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.filtroPrioridad.adapter = adaptadorPrioridad

        binding.filtroPrioridad.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    aplicarFiltro()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        val opcionesTipo = listOf(
            "Todos los tipos",
            "Tareas",
            "Eventos"
        )

        val adaptadorTipo = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesTipo
        )

        adaptadorTipo.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.filtroTipo.adapter = adaptadorTipo

        binding.filtroTipo.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    aplicarFiltro()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.buscarTitulo.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(texto: Editable?) {
                aplicarFiltro()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}
        })



        binding.btnMostrarTodas.setOnClickListener {

            binding.filtroCategoria.setSelection(0)
            binding.filtroEstado.setSelection(0)
            binding.filtroPrioridad.setSelection(0)
            binding.filtroTipo.setSelection(0)
            binding.buscarTitulo.text.clear()

            aplicarFiltro()
        }


    binding.btnNuevaTarea.setOnClickListener {
        val intent = Intent(this, NuevaTareaActividad::class.java)
        nuevaTareaLauncher.launch(intent)
    }

    binding.btnCategorias.setOnClickListener {
        val intent = Intent(this, CategoriasActividad::class.java)
        startActivity(intent)
    }

    binding.btnCalendario.setOnClickListener {
        val intent = Intent(this, CalendarioActividad::class.java)
        startActivity(intent)
    }


    cargarActividades()
}

override fun onResume() {
    super.onResume()
    cargarCategoriasFiltro()
}

    private fun cargarCategoriasFiltro() {

        categoriaRepository.obtenerTodas { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { categorias ->

                        categoriasFiltro = categorias

                        val opciones = listOf(
                            "Todas las categorías",
                            "Sin categoría"
                        ) + categorias.map { it.nombre }

                        val adaptadorFiltro = ArrayAdapter(
                            this,
                            android.R.layout.simple_spinner_item,
                            opciones
                        )

                        adaptadorFiltro.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                        )

                        binding.filtroCategoria.adapter = adaptadorFiltro
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
    }

    private fun cargarActividades() {
        actividadRepository.obtenerTodas { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { actividades ->
                        actividadesGuardadas = actividades
                        aplicarFiltro()
                    }
                    .onFailure {
                        binding.statusText.visibility = View.VISIBLE
                        binding.statusText.text =
                            "No se pudieron cargar las tareas."
                    }
            }
        }
    }

    private fun crearCanalRecordatorios() {

        val canal = NotificationChannel(
            "recordatorios",
            "Recordatorios de actividades",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val administrador = getSystemService(NotificationManager::class.java)

        administrador.createNotificationChannel(canal)
    }

    private fun solicitarPermisoNotificaciones() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permisoConcedido = ContextCompat.checkSelfPermission(
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
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permitido ->

            if (!permitido) {
                Toast.makeText(
                    this,
                    "Las notificaciones están desactivadas",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun aplicarFiltro() {

        val posicionCategoria = binding.filtroCategoria.selectedItemPosition
        val posicionEstado = binding.filtroEstado.selectedItemPosition
        val posicionPrioridad = binding.filtroPrioridad.selectedItemPosition
        val posicionTipo = binding.filtroTipo.selectedItemPosition

        // Filtrar por categoría
        val actividadesPorCategoria = when (posicionCategoria) {

            -1, 0 -> actividadesGuardadas

            1 -> actividadesGuardadas.filter {
                it.categoriaId == null
            }

            else -> {
                val categoria = categoriasFiltro.getOrNull(posicionCategoria - 2)

                actividadesGuardadas.filter {
                    it.categoriaId == categoria?.id
                }
            }
        }

        // Filtrar por estado
        val actividadesPorEstado = when (posicionEstado) {

            1 -> actividadesPorCategoria.filter {
                it.estadoTarea == EstadoTarea.PENDIENTE
            }

            2 -> actividadesPorCategoria.filter {
                it.estadoTarea == EstadoTarea.COMPLETADA
            }

            else -> actividadesPorCategoria
        }

        // Filtrar por prioridad
        val actividadesPorPrioridad = when (posicionPrioridad) {

            1 -> actividadesPorEstado.filter {
                it.prioridad == Prioridad.ALTA
            }

            2 -> actividadesPorEstado.filter {
                it.prioridad == Prioridad.MEDIA
            }

            3 -> actividadesPorEstado.filter {
                it.prioridad == Prioridad.BAJA
            }

            else -> actividadesPorEstado
        }

        // Filtrar por tipo de actividad
        val actividadesPorTipo = when (posicionTipo) {

            1 -> actividadesPorPrioridad.filter {
                it.tipo == TipoActividad.TAREA
            }

            2 -> actividadesPorPrioridad.filter {
                it.tipo == TipoActividad.EVENTO
            }

            else -> actividadesPorPrioridad
        }

        // Buscar actividades por título
        val textoBuscado = binding.buscarTitulo.text.toString().trim()

        val actividadesFiltradas = actividadesPorTipo.filter {
            it.titulo.contains(textoBuscado, ignoreCase = true)
        }

        adaptador.actualizarLista(actividadesFiltradas)

        if (actividadesFiltradas.isEmpty()) {
            binding.statusText.visibility = View.VISIBLE
            binding.statusText.text =
                if (actividadesGuardadas.isEmpty())
                    "Todavía no hay actividades guardadas."
                else
                    "No hay actividades con los filtros seleccionados."
        } else {
            binding.statusText.visibility = View.GONE
        }
    }

private fun mostrarDetalle(
    actividad: Actividad,
    nombreCategoria: String
) {
    AlertDialog.Builder(this)
        .setTitle("Detalle de la actividad")
        .setMessage(formatearActividad(actividad, nombreCategoria))
        .setPositiveButton("Cerrar", null)
        .show()
}
private fun formatearActividad(
    actividad: Actividad,
    nombreCategoria: String
): String =
    buildString {
        append(actividad.titulo)

        append("\nTipo: ").append(
            if (actividad.tipo == TipoActividad.TAREA) "Tarea" else "Evento"
        )

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

        append("\nCategoría: ").append(nombreCategoria)

        append("\nRecordatorio: ").append(
            nombreRecordatorio(actividad.recordatorio)
        )

        actividad.estadoTarea?.let {
            append("\nEstado: ").append(it)
        }
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






