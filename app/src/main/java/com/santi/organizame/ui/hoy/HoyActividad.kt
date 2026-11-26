
package com.santi.organizame.ui.hoy

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.TipoActividad
import com.santi.organizame.ui.consulta.ConsultaActividadesViewModel
import com.santi.organizame.ui.consulta.ConsultaActividadesViewModelFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import com.santi.organizame.R
import com.santi.organizame.ui.formulario.NuevaTareaActividad

class HoyActividad : AppCompatActivity() {

    private val viewModel: ConsultaActividadesViewModel by viewModels {
        ConsultaActividadesViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hoy)

        // Respetar las barras de Android
        val vista = findViewById<View>(R.id.pantallaHoy)

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

        // Botón Volver
        findViewById<Button>(R.id.btnVolverHoy).setOnClickListener {
            finish()
        }

        observarViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargar()
    }

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.estado.collect { estado ->
                        if (estado.cargaCompletada) mostrarResumen(estado.actividades)
                    }
                }
                launch {
                    viewModel.errores.collect { mensaje ->
                        Toast.makeText(this@HoyActividad, mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Calcular las actividades del día y las vencidas
    private fun mostrarResumen(actividades: List<Actividad>) {
        val ahora = LocalDateTime.now()
        val hoy = ahora.toLocalDate()

        val actividadesHoy = actividades.filter {
            it.fecha == hoy
        }

        val tareasHoy = actividadesHoy.filter {
            it.tipo == TipoActividad.TAREA
        }

        val pendientes = tareasHoy.count {
            it.estadoTarea == EstadoTarea.PENDIENTE
        }

        val completadas = tareasHoy.count {
            it.estadoTarea == EstadoTarea.COMPLETADA
        }

        val eventos = actividadesHoy.count {
            it.tipo == TipoActividad.EVENTO
        }

        val vencidas = actividades.filter {
            estaVencida(it, ahora)
        }

        // Mostrar la fecha actual
        val formatoFecha = DateTimeFormatter.ofPattern(
            "EEEE d 'de' MMMM",
            Locale.forLanguageTag("es-AR")
        )

        findViewById<TextView>(R.id.fechaHoy).text =
            hoy.format(formatoFecha)

        // Mostrar los cuatro recuadros del resumen
        mostrarRecuadrosResumen(
            pendientes,
            completadas,
            eventos,
            vencidas.size
        )

        // Contenedor donde se muestran las actividades
        val contenedor = findViewById<LinearLayout>(
            R.id.contenedorActividadesHoy
        )

        contenedor.removeAllViews()

        // Actividades programadas para hoy
        agregarSeccion(
            contenedor,
            "Actividades de hoy",
            actividadesHoy,
            ahora
        )

        // Tareas vencidas de días anteriores
        val vencidasAnteriores = vencidas.filter {
            it.fecha?.isBefore(hoy) == true
        }

        if (vencidasAnteriores.isNotEmpty()) {
            agregarSeccion(
                contenedor,
                "Tareas vencidas de días anteriores",
                vencidasAnteriores,
                ahora
            )
        }
    }

    // Mostrar los contadores en cuatro recuadros
    private fun mostrarRecuadrosResumen(
        pendientes: Int,
        completadas: Int,
        eventos: Int,
        vencidas: Int
    ) {
        val contenedor = findViewById<LinearLayout>(R.id.resumenHoy)
        contenedor.removeAllViews()

        val filas = listOf(
            listOf(
                "Pendientes" to pendientes,
                "Completadas" to completadas
            ),
            listOf(
                "Eventos" to eventos,
                "Vencidas" to vencidas
            )
        )

        filas.forEach { datosFila ->

            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            datosFila.forEach { (nombre, cantidad) ->

                val esVencida = nombre == "Vencidas"

                val tarjeta = TextView(this).apply {
                    text = "$cantidad\n$nombre"
                    textSize = 17f
                    gravity = Gravity.CENTER
                    setTypeface(null, Typeface.BOLD)

                    setTextColor(
                        color(if (esVencida) R.color.danger else R.color.primary)
                    )

                    setPadding(
                        dp(12),
                        dp(14),
                        dp(12),
                        dp(14)
                    )

                    background = GradientDrawable().apply {
                        cornerRadius = dp(12).toFloat()

                        setColor(
                            color(
                                if (esVencida) R.color.danger_container
                                else R.color.primary_container
                            )
                        )
                    }

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        setMargins(
                            dp(4),
                            dp(4),
                            dp(4),
                            dp(4)
                        )
                    }
                }

                fila.addView(tarjeta)
            }

            contenedor.addView(fila)
        }
    }

    // Comprobar si una tarea está vencida
    private fun estaVencida(
        actividad: Actividad,
        ahora: LocalDateTime
    ): Boolean {

        if (
            actividad.tipo != TipoActividad.TAREA ||
            actividad.estadoTarea != EstadoTarea.PENDIENTE
        ) {
            return false
        }

        val fecha = actividad.fecha ?: return false

        return fecha.isBefore(ahora.toLocalDate()) ||
                (
                        fecha == ahora.toLocalDate() &&
                                actividad.hora?.isBefore(ahora.toLocalTime()) == true
                        )
    }

    // Mostrar una sección de actividades
    private fun agregarSeccion(
        contenedor: LinearLayout,
        titulo: String,
        actividades: List<Actividad>,
        ahora: LocalDateTime
    ) {
        val encabezado = TextView(this).apply {
            text = titulo
            textSize = 20f
            setTextColor(color(R.color.primary_dark))
            setPadding(0, dp(12), 0, dp(12))
        }

        contenedor.addView(encabezado)

        // Mensaje cuando no hay actividades
        if (actividades.isEmpty()) {
            contenedor.addView(TextView(this).apply {
                text = if (titulo == "Actividades de hoy") {
                    "No tenés actividades programadas para hoy."
                } else {
                    "No hay actividades en esta sección."
                }

                textSize = 16f
                setTextColor(color(R.color.on_surface_variant))
                setPadding(0, dp(16), 0, dp(16))
            })

            return
        }

        // Crear una tarjeta por cada actividad
        actividades.forEach { actividad ->

            val vencida = estaVencida(actividad, ahora)

            val estado = when {
                actividad.tipo == TipoActividad.EVENTO ->
                    "Evento"

                actividad.estadoTarea == EstadoTarea.COMPLETADA ->
                    "Tarea completada"

                else ->
                    "Tarea pendiente"
            }

            // Título de la actividad
            val textoTitulo = if (vencida) {
                "⚠ VENCIDA · ${actividad.titulo}"
            } else {
                actividad.titulo
            }

            val tituloConColor = SpannableString(textoTitulo)

            // Pintar de rojo solamente VENCIDA
            if (vencida) {
                val inicio = textoTitulo.indexOf("VENCIDA")

                tituloConColor.setSpan(
                    ForegroundColorSpan(color(R.color.danger)),
                    inicio,
                    inicio + "VENCIDA".length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            // Contenedor visual de la tarjeta
            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL

                setPadding(
                    dp(14),
                    dp(14),
                    dp(14),
                    dp(14)
                )

                background = GradientDrawable().apply {
                    cornerRadius = dp(12).toFloat()

                    setColor(
                        if (vencida) {
                            color(R.color.danger_container)
                        } else {
                            color(R.color.surface)
                        }
                    )
                }

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(10)
                }
            }

            // Título destacado
            val vistaTitulo = TextView(this).apply {
                text = tituloConColor
                textSize = 17f
                setTypeface(null, Typeface.BOLD)
                setTextColor(color(R.color.primary_dark))
            }

            // Estado, hora y fecha
            val vistaDetalles = TextView(this).apply {
                text = buildString {
                    append(estado)

                    actividad.hora?.let {
                        append(" · ")
                        append(
                            it.format(
                                DateTimeFormatter.ofPattern("HH:mm")
                            )
                        )
                    }

                    actividad.fecha?.let {
                        append("\nFecha: ")
                        append(
                            it.format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            )
                        )
                    }


                }

                textSize = 14f
                setTextColor(color(R.color.on_surface_variant))
                setPadding(0, dp(6), 0, 0)
            }

            tarjeta.addView(vistaTitulo)
            tarjeta.addView(vistaDetalles)



            val botonEditar = Button(this).apply {
                text = "✎ Editar actividad"
                textSize = 14f
                isAllCaps = false

                backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        color(R.color.primary)
                    )

                setTextColor(color(R.color.on_primary))

                setOnClickListener {
                    tarjeta.performClick()
                }
            }

            tarjeta.addView(botonEditar)

            // Abrir la actividad para editarla
            tarjeta.setOnClickListener {
                val intent = Intent(
                    this@HoyActividad,
                    NuevaTareaActividad::class.java
                )

                intent.putExtra("actividadId", actividad.id)
                startActivity(intent)
            }

            contenedor.addView(tarjeta)
        }
    }

    // Convertir medidas a dp
    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    private fun color(id: Int): Int = ContextCompat.getColor(this, id)
}
