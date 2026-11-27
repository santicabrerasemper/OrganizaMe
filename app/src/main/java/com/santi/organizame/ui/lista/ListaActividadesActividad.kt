package com.santi.organizame.ui.lista

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.santi.organizame.R
import com.santi.organizame.filtros.CriteriosFiltroActividad
import com.santi.organizame.filtros.OrdenActividad
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.Recurrencia
import com.santi.organizame.modelo.TipoActividad
import com.santi.organizame.ui.lista.ListaActividadesEfecto
import com.santi.organizame.ui.lista.ListaActividadesUiState
import com.santi.organizame.ui.lista.ListaActividadesViewModel
import com.santi.organizame.ui.lista.ListaActividadesViewModelFactory
import com.santi.organizame.ui.formulario.NuevaTareaActividad
import java.time.format.DateTimeFormatter
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.ScrollView
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

class ListaActividadesActividad : AppCompatActivity() {
    private val viewModel: ListaActividadesViewModel by viewModels {
        ListaActividadesViewModelFactory(applicationContext)
    }

    private var categoriasFiltro: List<Categoria> = emptyList()

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
        configurarInsets()
        findViewById<Button>(R.id.btnVolverLista).setOnClickListener { finish() }

        secciones = listOf(
            Seccion(R.id.btnPendientes, R.id.textoPendientes, R.id.listaPendientes,
                "Pendientes y próximas", "No tenés tareas pendientes o próximas.", crearAdaptador()),
            Seccion(R.id.btnVencidas, R.id.textoVencidas, R.id.listaVencidas,
                "Vencidas", "No tenés tareas vencidas.", crearAdaptador()),
            Seccion(R.id.btnCompletadas, R.id.textoCompletadas, R.id.listaCompletadas,
                "Completadas", "Todavía no tenés tareas completadas.", crearAdaptador()),
            Seccion(R.id.btnEventos, R.id.textoEventos, R.id.listaEventos,
                "Eventos", "No tenés eventos guardados.", crearAdaptador())
        )

        configurarFiltros()
        configurarSecciones()
        findViewById<EditText>(R.id.buscarTitulo).doAfterTextChanged {
            actualizarFiltrosViewModel()
        }
        observarViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargar()
    }

    private fun configurarInsets() {
        val vista = findViewById<View>(R.id.pantallaListaActividades)
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

    private fun configurarSecciones() {
        secciones.forEach { seccion ->
            findViewById<RecyclerView>(seccion.idLista).apply {
                layoutManager = LinearLayoutManager(this@ListaActividadesActividad)
                adapter = seccion.adaptador
                isNestedScrollingEnabled = false
            }
            findViewById<TextView>(seccion.idMensaje).text = seccion.mensajeVacio
            findViewById<Button>(seccion.idBoton).setOnClickListener {
                if (!seccionesAbiertas.add(seccion.idBoton)) {
                    seccionesAbiertas.remove(seccion.idBoton)
                }
                actualizarSeccion(seccion)
            }
            actualizarSeccion(seccion)
        }
    }

    private fun configurarFiltros() {
        prepararFiltro(R.id.filtroEstado,
            listOf("Todos los estados", "Pendientes", "Completadas"))
        prepararFiltro(R.id.filtroPrioridad,
            listOf("Todas las prioridades", "Alta", "Media", "Baja"))
        prepararFiltro(R.id.filtroTipo,
            listOf("Todos los tipos", "Tareas", "Eventos"))
        prepararFiltro(R.id.ordenActividades,
            listOf("Ordenar por fecha y hora", "Ordenar por prioridad",
                "Ordenar por título", "Más recientes primero"))

        actualizarFiltroCategorias(emptyList())
        findViewById<Spinner>(R.id.filtroCategoria).onItemSelectedListener = listenerFiltros()

        findViewById<Button>(R.id.btnMostrarTodas).setOnClickListener {
            findViewById<Spinner>(R.id.filtroCategoria).setSelection(0)
            findViewById<Spinner>(R.id.filtroEstado).setSelection(0)
            findViewById<Spinner>(R.id.filtroPrioridad).setSelection(0)
            findViewById<Spinner>(R.id.filtroTipo).setSelection(0)
            findViewById<Spinner>(R.id.ordenActividades).setSelection(0)
            findViewById<EditText>(R.id.buscarTitulo).text.clear()
            seccionesAbiertas.clear()
            actualizarFiltrosViewModel()
        }
    }

    private fun prepararFiltro(idFiltro: Int, opciones: List<String>) {
        findViewById<Spinner>(idFiltro).apply {
            adapter = crearAdaptadorSpinner(opciones)
            onItemSelectedListener = listenerFiltros()
        }
    }

    private fun crearAdaptadorSpinner(opciones: List<String>): ArrayAdapter<String> =
        ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    private fun listenerFiltros(): AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) = actualizarFiltrosViewModel()

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.estado.collect(::renderizarEstado) }
                launch { viewModel.efectos.collect(::procesarEfecto) }
            }
        }
    }

    private fun renderizarEstado(estado: ListaActividadesUiState) {
        actualizarFiltroCategorias(estado.categorias)
        val listas = listOf(
            estado.secciones.pendientes,
            estado.secciones.vencidas,
            estado.secciones.completadas,
            estado.secciones.eventos
        )
        if (!estado.hayFiltrosActivos) seccionesAbiertas.clear()

        secciones.zip(listas).forEach { (seccion, datos) ->
            seccion.adaptador.actualizarLista(datos)
            if (estado.hayFiltrosActivos) {
                if (datos.isNotEmpty()) seccionesAbiertas.add(seccion.idBoton)
                else seccionesAbiertas.remove(seccion.idBoton)
            }
            actualizarSeccion(seccion)
        }
    }

    private fun actualizarFiltroCategorias(categorias: List<Categoria>) {
        val spinner = findViewById<Spinner>(R.id.filtroCategoria)
        if (categoriasFiltro == categorias && spinner.adapter != null) return

        val posicionAnterior = spinner.selectedItemPosition.coerceAtLeast(0)
        val categoriaSeleccionada = if (posicionAnterior >= 2) {
            categoriasFiltro.getOrNull(posicionAnterior - 2)?.id
        } else null
        val seleccionSinCategoria = posicionAnterior == 1

        categoriasFiltro = categorias
        spinner.adapter = crearAdaptadorSpinner(
            listOf("Todas las categorías", "Sin categoría") +
                categorias.map(Categoria::nombre)
        )
        val nuevaPosicion = when {
            seleccionSinCategoria -> 1
            categoriaSeleccionada != null ->
                categorias.indexOfFirst { it.id == categoriaSeleccionada }
                    .takeIf { it >= 0 }?.plus(2) ?: 0
            else -> 0
        }
        spinner.setSelection(nuevaPosicion)
    }

    private fun actualizarFiltrosViewModel() {
        if (!::secciones.isInitialized) return
        val posicionCategoria = posicion(R.id.filtroCategoria)
        val posicionEstado = posicion(R.id.filtroEstado)
        val posicionPrioridad = posicion(R.id.filtroPrioridad)
        val posicionTipo = posicion(R.id.filtroTipo)
        val posicionOrden = posicion(R.id.ordenActividades)

        viewModel.actualizarFiltros(
            nuevosCriterios = CriteriosFiltroActividad(
                texto = findViewById<EditText>(R.id.buscarTitulo).text.toString().trim(),
                categoriaId = if (posicionCategoria >= 2) {
                    categoriasFiltro.getOrNull(posicionCategoria - 2)?.id
                } else null,
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
            ),
            nuevoOrden = OrdenActividad.entries.getOrElse(posicionOrden) {
                OrdenActividad.FECHA_HORA
            }
        )
    }

    private fun posicion(id: Int): Int =
        findViewById<Spinner>(id).selectedItemPosition.coerceAtLeast(0)

    private fun crearAdaptador(): AdaptadorTarjetasActividad =
        AdaptadorTarjetasActividad(
            actividades = emptyList(),
            alEditar = { actividad ->
                startActivity(
                    Intent(this, NuevaTareaActividad::class.java)
                        .putExtra("actividadId", actividad.id)
                )
            },
            alEliminar = ::confirmarEliminacion,
            alCambiarEstado = viewModel::cambiarEstado,
            alVerDetalle = viewModel::solicitarDetalle
        )

    private fun confirmarEliminacion(actividad: Actividad) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar actividad")
            .setMessage("¿Seguro que querés eliminar \"${actividad.titulo}\"?")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminar(actividad) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun procesarEfecto(efecto: ListaActividadesEfecto) {
        when (efecto) {
            is ListaActividadesEfecto.Mensaje -> Toast.makeText(
                this, efecto.texto,
                if (efecto.largo) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
            is ListaActividadesEfecto.MostrarDetalle ->
                mostrarDetalle(efecto.actividad, efecto.nombreCategoria)
        }
    }

    private fun actualizarSeccion(seccion: Seccion) {
        val abierta = seccion.idBoton in seccionesAbiertas
        val cantidad = seccion.adaptador.itemCount
        val flecha = if (abierta) "▲" else "▼"
        findViewById<Button>(seccion.idBoton).text =
            "${seccion.nombre} $flecha ($cantidad)"
        findViewById<RecyclerView>(seccion.idLista).visibility =
            if (abierta && cantidad > 0) View.VISIBLE else View.GONE
        findViewById<TextView>(seccion.idMensaje).visibility =
            if (abierta && cantidad == 0) View.VISIBLE else View.GONE
    }


    private fun mostrarDetalle(
        actividad: Actividad,
        nombreCategoria: String
    ) {
        fun dp(valor: Int): Int =
            (valor * resources.displayMetrics.density).toInt()

        val colorTexto = ContextCompat.getColor(
            this, R.color.on_background
        )
        val colorSecundario = ContextCompat.getColor(
            this, R.color.on_surface_variant
        )
        val colorTarjeta = ContextCompat.getColor(
            this, R.color.surface_variant
        )
        val colorPrincipal = ContextCompat.getColor(
            this, R.color.primary
        )

        val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))

            background = GradientDrawable().apply {
                setColor(colorTarjeta)
                cornerRadius = dp(16).toFloat()
            }
        }

        fun agregarTexto(
            contenido: String,
            tamano: Float = 19f,
            negrita: Boolean = false,
            destacado: Boolean = false,
            espacioSuperior: Int = 10
        ) {
            val texto = TextView(this).apply {
                text = contenido
                textSize = tamano
                setTextColor(
                    if (destacado) colorPrincipal
                    else if (negrita) colorTexto
                    else colorSecundario
                )
                if (negrita) {
                    typeface = Typeface.DEFAULT_BOLD
                }
                setLineSpacing(dp(3).toFloat(), 1f)
            }

            val parametros = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(espacioSuperior)
            }

            contenedor.addView(texto, parametros)
        }

        // Título principal
        agregarTexto(
            actividad.titulo,
            tamano = 24f,
            negrita = true,
            destacado = true,
            espacioSuperior = 0
        )

        // Tipo de actividad
        agregarTexto(
            if (actividad.tipo == TipoActividad.TAREA) {
                "📋  Tarea"
            } else {
                "📅  Evento"
            }
        )

        // Fecha y hora
        val fecha = actividad.fecha?.format(formatoFecha)
            ?: "Sin fecha"

        val hora = actividad.hora?.format(formatoHora)

        agregarTexto(
            "📅  $fecha" +
                    if (hora != null) "  ·  $hora hs" else ""
        )

        // Prioridad
        val iconoPrioridad = when (actividad.prioridad) {
            Prioridad.ALTA -> "🔴"
            Prioridad.MEDIA -> "🟡"
            Prioridad.BAJA -> "🟢"
        }

        agregarTexto(
            "$iconoPrioridad  Prioridad: ${actividad.prioridad}"
        )

        // Estado de la tarea
        actividad.estadoTarea?.let { estado ->
            val icono = if (estado == EstadoTarea.COMPLETADA) {
                "✅"
            } else {
                "⏳"
            }

            agregarTexto("$icono  Estado: $estado")
        }

        // Categoría
        agregarTexto("📁  Categoría: $nombreCategoria")

        // Recordatorio
        val nombreRecordatorio = when (actividad.recordatorio) {
            com.santi.organizame.modelo.AnticipacionRecordatorio.A_LA_HORA ->
                "A la hora"

            com.santi.organizame.modelo.AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES ->
                "10 minutos antes"

            com.santi.organizame.modelo.AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES ->
                "30 minutos antes"

            com.santi.organizame.modelo.AnticipacionRecordatorio.UNA_HORA_ANTES ->
                "1 hora antes"

            com.santi.organizame.modelo.AnticipacionRecordatorio.UN_DIA_ANTES ->
                "1 día antes"

            null -> "Sin recordatorio"
        }

        agregarTexto("🔔  Recordatorio: $nombreRecordatorio")

        // Repetición
        if (actividad.recurrencia != Recurrencia.SIN_REPETICION) {
            agregarTexto(
                "🔁  Repetición: ${nombreRecurrencia(actividad.recurrencia)}"
            )
        }

        // Descripción
        actividad.descripcion
            ?.takeIf { it.isNotBlank() }
            ?.let { descripcion ->

                agregarTexto(
                    "DESCRIPCIÓN",
                    tamano = 14f,
                    negrita = true,
                    destacado = true,
                    espacioSuperior = 22
                )

                agregarTexto(
                    descripcion,
                    tamano = 16f,
                    espacioSuperior = 6
                )
            }

        // Permitir desplazarse si hay muchos datos
        val scroll = ScrollView(this).apply {
            setPadding(dp(12), dp(8), dp(12), dp(8))
            clipToPadding = false
            addView(contenedor)
        }

        val dialogo = AlertDialog.Builder(this)
            .setTitle("Detalle de la actividad")
            .setView(scroll)
            .setPositiveButton("Cerrar", null)
            .create()

        dialogo.show()

        dialogo.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun nombreRecurrencia(recurrencia: Recurrencia): String =
        when (recurrencia) {
            Recurrencia.SIN_REPETICION -> "No repetir"
            Recurrencia.DIARIA -> "Diaria"
            Recurrencia.SEMANAL -> "Semanal"
            Recurrencia.MENSUAL -> "Mensual"
        }
}
