


package com.santi.organizame

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.ActividadRepository
import com.santi.organizame.modelo.CategoriaRepository
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.OrganizaMeDatabase
import com.santi.organizame.modelo.TipoActividad
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.Recurrencia
import com.santi.organizame.modelo.GeneradorRecurrencia
import com.santi.organizame.filtros.CriteriosFiltroActividad
import com.santi.organizame.filtros.FiltroActividades
import com.santi.organizame.filtros.OrdenActividad
import com.santi.organizame.filtros.OrdenadorActividades
class ListaActividadesActividad : AppCompatActivity() {

    private lateinit var repositorio: ActividadRepository
    private lateinit var categoriaRepository: CategoriaRepository
    private var categoriasFiltro: List<Categoria> = emptyList()

    private var actividadesGuardadas: List<Actividad> = emptyList()

    // Información necesaria para cada sección
    private data class Seccion(
        val idBoton: Int,
        val idMensaje: Int,
        val idLista: Int,
        val nombre: String,
        val mensajeVacio: String,
        val adaptador: AdaptadorTarjetasActividad
    )

    private lateinit var secciones: List<Seccion>
    private val seccionesAbiertas = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_actividades)

        // Respetar las barras del sistema
        val vista = findViewById<View>(R.id.pantallaListaActividades)

        val izquierda = vista.paddingLeft
        val arriba = vista.paddingTop
        val derecha = vista.paddingRight
        val abajo = vista.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(vista) { _, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            vista.setPadding(
                izquierda + barras.left,
                arriba + barras.top,
                derecha + barras.right,
                abajo + barras.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(vista)

        findViewById<Button>(R.id.btnVolverLista).setOnClickListener {
            finish()
        }

        val baseDatos = OrganizaMeDatabase.obtener(applicationContext)

        repositorio = ActividadRepository(baseDatos.actividadDao())
        categoriaRepository = CategoriaRepository(baseDatos.categoriaDao())

        // Las cuatro secciones usan las tarjetas existentes
        secciones = listOf(
            Seccion(
                R.id.btnPendientes,
                R.id.textoPendientes,
                R.id.listaPendientes,
                "Pendientes y próximas",
                "No tenés tareas pendientes o próximas.",
                crearAdaptador()
            ),
            Seccion(
                R.id.btnVencidas,
                R.id.textoVencidas,
                R.id.listaVencidas,
                "Vencidas",
                "No tenés tareas vencidas.",
                crearAdaptador()
            ),
            Seccion(
                R.id.btnCompletadas,
                R.id.textoCompletadas,
                R.id.listaCompletadas,
                "Completadas",
                "Todavía no tenés tareas completadas.",
                crearAdaptador()
            ),
            Seccion(
                R.id.btnEventos,
                R.id.textoEventos,
                R.id.listaEventos,
                "Eventos",
                "No tenés eventos guardados.",
                crearAdaptador()
            )
        )

        // Preparar una sola vez los controles compartidos por todas las secciones.
        configurarFiltros()

        // Preparar las listas desplegables
        secciones.forEach { seccion ->
            val lista = findViewById<RecyclerView>(seccion.idLista)
            lista.layoutManager = LinearLayoutManager(this)
            lista.adapter = seccion.adaptador
            lista.isNestedScrollingEnabled = false

            findViewById<TextView>(seccion.idMensaje).text =
                seccion.mensajeVacio

            findViewById<Button>(seccion.idBoton).setOnClickListener {
                if (!seccionesAbiertas.add(seccion.idBoton)) {
                    seccionesAbiertas.remove(seccion.idBoton)
                }

                actualizarSeccion(seccion)
            }

            actualizarSeccion(seccion)
        }

        findViewById<EditText>(R.id.buscarTitulo).doAfterTextChanged {
            actualizarSecciones()
        }
    }




    private fun configurarFiltros() {

        prepararFiltro(
            R.id.filtroEstado,
            listOf("Todos los estados", "Pendientes", "Completadas")
        )

        prepararFiltro(
            R.id.filtroPrioridad,
            listOf("Todas las prioridades", "Alta", "Media", "Baja")
        )

        prepararFiltro(
            R.id.filtroTipo,
            listOf("Todos los tipos", "Tareas", "Eventos")
        )

        prepararFiltro(
            R.id.ordenActividades,
            listOf(
                "Ordenar por fecha y hora",
                "Ordenar por prioridad",
                "Ordenar por título",
                "Más recientes primero"
            )
        )

        val filtroCategoria = findViewById<Spinner>(R.id.filtroCategoria)

        filtroCategoria.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    actualizarSecciones()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        findViewById<Button>(R.id.btnMostrarTodas).setOnClickListener {
            findViewById<Spinner>(R.id.filtroCategoria).setSelection(0)
            findViewById<Spinner>(R.id.filtroEstado).setSelection(0)
            findViewById<Spinner>(R.id.filtroPrioridad).setSelection(0)
            findViewById<Spinner>(R.id.filtroTipo).setSelection(0)
            findViewById<Spinner>(R.id.ordenActividades).setSelection(0)
            findViewById<EditText>(R.id.buscarTitulo).text.clear()

            seccionesAbiertas.clear()
            actualizarSecciones()
        }
    }

    private fun prepararFiltro(
        idFiltro: Int,
        opciones: List<String>
    ) {
        val filtro = findViewById<Spinner>(idFiltro)

        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        )

        adaptador.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        filtro.adapter = adaptador

        filtro.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    actualizarSecciones()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun cargarCategoriasFiltro() {
        categoriaRepository.obtenerTodas { resultado ->
            runOnUiThread {
                resultado.onSuccess { categorias ->
                    categoriasFiltro = categorias

                    val opciones = listOf(
                        "Todas las categorías",
                        "Sin categoría"
                    ) + categorias.map { it.nombre }

                    val adaptador = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        opciones
                    )

                    adaptador.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                    )

                    findViewById<Spinner>(
                        R.id.filtroCategoria
                    ).adapter = adaptador
                }
            }
        }
    }



    override fun onResume() {
        super.onResume()
        cargarCategoriasFiltro()
        cargarActividades()
    }

    // Reutilizar los botones de las tarjetas
    private fun crearAdaptador(): AdaptadorTarjetasActividad {
        return AdaptadorTarjetasActividad(
            actividades = emptyList(),

            alEditar = { actividad ->
                val intent = Intent(
                    this,
                    NuevaTareaActividad::class.java
                )

                intent.putExtra("actividadId", actividad.id)
                startActivity(intent)
            },

            alEliminar = { actividad ->
                confirmarEliminacion(actividad)
            },

            alCambiarEstado = { actividad ->
                cambiarEstadoTarea(actividad)
            },

            alVerDetalle = { actividad ->
                mostrarDetalleConCategoria(actividad)
            }
        )
    }

    // Leer y separar las actividades de Room
    private fun cargarActividades() {
        repositorio.obtenerTodas { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { actividades ->
                        actividadesGuardadas = actividades
                        actualizarSecciones()
                    }
                    .onFailure {
                        Toast.makeText(
                            this,
                            "No se pudieron cargar las actividades",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }


    private fun actualizarSecciones() {

        val busqueda = findViewById<EditText>(
            R.id.buscarTitulo
        ).text.toString().trim()

        val posicionCategoria =
            findViewById<Spinner>(R.id.filtroCategoria).selectedItemPosition

        val posicionEstado =
            findViewById<Spinner>(R.id.filtroEstado).selectedItemPosition

        val posicionPrioridad =
            findViewById<Spinner>(R.id.filtroPrioridad).selectedItemPosition

        val posicionTipo =
            findViewById<Spinner>(R.id.filtroTipo).selectedItemPosition

        val ordenSeleccionado = OrdenActividad.values()[
            findViewById<Spinner>(R.id.ordenActividades).selectedItemPosition
        ]

        val criterios = CriteriosFiltroActividad(
            texto = busqueda,
            categoriaId = if (posicionCategoria >= 2) {
                categoriasFiltro.getOrNull(posicionCategoria - 2)?.id
            } else {
                null
            },
            soloSinCategoria = posicionCategoria == 1,
            estado = when (posicionEstado) {
                1 -> EstadoTarea.PENDIENTE
                2 -> EstadoTarea.COMPLETADA
                else -> null
            },
            prioridad = when (posicionPrioridad) {
                1 -> Prioridad.ALTA
                2 -> Prioridad.MEDIA
                3 -> Prioridad.BAJA
                else -> null
            },
            tipo = when (posicionTipo) {
                1 -> TipoActividad.TAREA
                2 -> TipoActividad.EVENTO
                else -> null
            }
        )

        val actividades = FiltroActividades.aplicar(
            actividadesGuardadas,
            criterios
        )

        val ahora = LocalDateTime.now()

        // Tareas pendientes que no están vencidas
        val pendientes = OrdenadorActividades.ordenar(actividades.filter {
            it.tipo == TipoActividad.TAREA &&
                    it.estadoTarea == EstadoTarea.PENDIENTE &&
                    !estaVencida(it, ahora)
        }, ordenSeleccionado)

        // Tareas vencidas
        val vencidas = OrdenadorActividades.ordenar(actividades.filter {
            estaVencida(it, ahora)
        }, ordenSeleccionado)

        // Tareas completadas
        val completadas = OrdenadorActividades.ordenar(actividades.filter {
            it.tipo == TipoActividad.TAREA &&
                    it.estadoTarea == EstadoTarea.COMPLETADA
        }, ordenSeleccionado)

        // Eventos
        val eventos = OrdenadorActividades.ordenar(actividades.filter {
            it.tipo == TipoActividad.EVENTO
        }, ordenSeleccionado)

        val listas = listOf(
            pendientes,
            vencidas,
            completadas,
            eventos
        )

        // Si se borró la búsqueda, cerrar todas las secciones
        if (busqueda.isBlank()) {
            seccionesAbiertas.clear()
        }


        // Comprobar si hay una búsqueda o algún filtro activo
        val hayFiltrosActivos =
            busqueda.isNotBlank() ||
                    posicionCategoria > 0 ||
                    posicionEstado > 0 ||
                    posicionPrioridad > 0 ||
                    posicionTipo > 0

        // Si no hay filtros, cerrar todas las secciones
        if (!hayFiltrosActivos) {
            seccionesAbiertas.clear()
        }

        // Actualizar las cuatro secciones
        secciones.zip(listas).forEach { (seccion, datos) ->

            seccion.adaptador.actualizarLista(datos)

            if (hayFiltrosActivos) {
                if (datos.isNotEmpty()) {
                    seccionesAbiertas.add(seccion.idBoton)
                } else {
                    seccionesAbiertas.remove(seccion.idBoton)
                }
            }

            actualizarSeccion(seccion)
        }
    }

    // Mostrar o esconder una sección
    private fun actualizarSeccion(seccion: Seccion) {
        val abierta = seccion.idBoton in seccionesAbiertas
        val cantidad = seccion.adaptador.itemCount

        val boton = findViewById<Button>(seccion.idBoton)
        val mensaje = findViewById<TextView>(seccion.idMensaje)
        val lista = findViewById<RecyclerView>(seccion.idLista)

        val flecha = if (abierta) "▲" else "▼"
        boton.text = "${seccion.nombre} $flecha ($cantidad)"

        lista.visibility =
            if (abierta && cantidad > 0) View.VISIBLE else View.GONE

        mensaje.visibility =
            if (abierta && cantidad == 0) View.VISIBLE else View.GONE
    }

    // Comprobar si una tarea está vencida
    private fun estaVencida(
        actividad: Actividad,
        ahora: LocalDateTime
    ): Boolean {
        if (
            actividad.tipo != TipoActividad.TAREA ||
            actividad.estadoTarea != EstadoTarea.PENDIENTE
        ) return false

        val fecha = actividad.fecha ?: return false

        return fecha.isBefore(ahora.toLocalDate()) ||
                (
                        fecha == ahora.toLocalDate() &&
                                actividad.hora?.isBefore(ahora.toLocalTime()) == true
                        )
    }

    // Completar o reabrir una tarea
    private fun cambiarEstadoTarea(actividad: Actividad) {
        if (actividad.tipo != TipoActividad.TAREA) return

        val momento = Instant.now()
        val completar = actividad.estadoTarea == EstadoTarea.PENDIENTE
        val crearSiguiente = completar &&
                actividad.recurrencia != Recurrencia.SIN_REPETICION &&
                !actividad.recurrenciaProcesada &&
                actividad.fecha != null

        val actualizada = if (completar) {
            actividad.copy(
                estadoTarea = EstadoTarea.COMPLETADA,
                fechaCompletada = momento,
                fechaRecordatorioProgramado = null,
                recurrenciaProcesada = actividad.recurrenciaProcesada || crearSiguiente,
                fechaModificacion = momento
            )
        } else {
            actividad.copy(
                estadoTarea = EstadoTarea.PENDIENTE,
                fechaCompletada = null,
                fechaRecordatorioProgramado =
                    ProgramadorRecordatorios.calcularMomentoAviso(
                        actividad.fecha,
                        actividad.hora,
                        actividad.recordatorio
                    )?.takeIf { it.isAfter(momento) },
                fechaModificacion = momento
            )
        }

        repositorio.actualizar(actualizada) { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess {
                        val programador =
                            ProgramadorRecordatorios(applicationContext)

                        if (completar) {
                            programador.cancelar(actualizada.id)
                        } else {
                            programador.programar(actualizada)
                        }

                        if (crearSiguiente) {
                            crearSiguienteRecurrencia(actualizada, momento)
                        } else {
                            cargarActividades()
                        }
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
    }

    private fun crearSiguienteRecurrencia(
        actividad: Actividad,
        momento: Instant
    ) {
        val siguienteBase = GeneradorRecurrencia.crearSiguiente(
            actividadCompletada = actividad,
            momento = momento
        ) ?: return cargarActividades()

        val siguienteRecordatorio = ProgramadorRecordatorios.calcularMomentoAviso(
            siguienteBase.fecha,
            siguienteBase.hora,
            siguienteBase.recordatorio
        )?.takeIf { it.isAfter(momento) }

        val siguiente = siguienteBase.copy(
            fechaRecordatorioProgramado = siguienteRecordatorio
        )

        repositorio.guardar(siguiente) { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { nuevoId ->
                        ProgramadorRecordatorios(applicationContext)
                            .programar(siguiente.copy(id = nuevoId))
                        Toast.makeText(
                            this,
                            "Se creó la próxima tarea repetitiva",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onFailure {
                        Toast.makeText(
                            this,
                            "La tarea se completó, pero no se pudo crear la siguiente",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                cargarActividades()
            }
        }
    }

    // Eliminar una actividad con confirmación
    private fun confirmarEliminacion(actividad: Actividad) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar actividad")
            .setMessage(
                "¿Seguro que querés eliminar \"${actividad.titulo}\"?"
            )
            .setPositiveButton("Eliminar") { _, _ ->
                repositorio.eliminar(actividad) { resultado ->
                    runOnUiThread {
                        resultado
                            .onSuccess {
                                ProgramadorRecordatorios(applicationContext)
                                    .cancelar(actividad.id)

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
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Consultar la categoría para mostrar el detalle
    private fun mostrarDetalleConCategoria(actividad: Actividad) {
        val idCategoria = actividad.categoriaId

        if (idCategoria == null) {
            mostrarDetalle(actividad, "Sin categoría")
            return
        }

        categoriaRepository.obtenerPorId(idCategoria) { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { categoria ->
                        mostrarDetalle(
                            actividad,
                            categoria?.nombre ?: "Sin categoría"
                        )
                    }
                    .onFailure {
                        Toast.makeText(
                            this,
                            "No se pudo consultar la categoría",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun mostrarDetalle(
        actividad: Actividad,
        nombreCategoria: String
    ) {
        val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

        val detalle = buildString {
            append("Título: ${actividad.titulo}")
            append("\nTipo: ${actividad.tipo}")

            actividad.descripcion?.takeIf { it.isNotBlank() }?.let {
                append("\nDescripción: $it")
            }

            actividad.fecha?.let {
                append("\nFecha: ${it.format(formatoFecha)}")
            }

            actividad.hora?.let {
                append("\nHora: ${it.format(formatoHora)}")
            }

            append("\nPrioridad: ${actividad.prioridad}")
            append("\nCategoría: $nombreCategoria")

            actividad.estadoTarea?.let {
                append("\nEstado: $it")
            }

            append(
                "\nRecordatorio: " +
                        (actividad.recordatorio?.name?.replace("_", " ")
                            ?: "Sin recordatorio")
            )

            if (actividad.recurrencia != Recurrencia.SIN_REPETICION) {
                append("\nRepetición: ${nombreRecurrencia(actividad.recurrencia)}")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Detalle de la actividad")
            .setMessage(detalle)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun nombreRecurrencia(recurrencia: Recurrencia): String =
        when (recurrencia) {
            Recurrencia.SIN_REPETICION -> "No repetir"
            Recurrencia.DIARIA -> "Diaria"
            Recurrencia.SEMANAL -> "Semanal"
            Recurrencia.MENSUAL -> "Mensual"
        }
}


