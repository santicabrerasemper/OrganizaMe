package com.santi.organizame.ui.resumen

import android.graphics.Typeface
import android.os.Bundle
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
import com.santi.organizame.modelo.Recurrencia
import com.santi.organizame.modelo.TipoActividad
import com.santi.organizame.resumen.CalculadorResumenSemanal
import com.santi.organizame.ui.consulta.ConsultaActividadesViewModel
import com.santi.organizame.ui.consulta.ConsultaActividadesViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import com.santi.organizame.R

class ResumenSemanalActividad : AppCompatActivity() {

    private val viewModel: ConsultaActividadesViewModel by viewModels {
        ConsultaActividadesViewModelFactory(applicationContext)
    }
    private var actividades: List<Actividad> = emptyList()
    private var inicioSemana = CalculadorResumenSemanal.inicioDeSemana(LocalDate.now())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen_semanal)
        aplicarInsets()

        findViewById<Button>(R.id.btnVolverResumen).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSemanaAnterior).setOnClickListener {
            inicioSemana = inicioSemana.minusWeeks(1)
            mostrarResumen()
        }
        findViewById<Button>(R.id.btnSemanaActual).setOnClickListener {
            inicioSemana = CalculadorResumenSemanal.inicioDeSemana(LocalDate.now())
            mostrarResumen()
        }
        findViewById<Button>(R.id.btnSemanaSiguiente).setOnClickListener {
            inicioSemana = inicioSemana.plusWeeks(1)
            mostrarResumen()
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
                        if (estado.cargaCompletada) {
                            actividades = estado.actividades
                            mostrarResumen()
                        }
                    }
                }
                launch {
                    viewModel.errores.collect {
                        Toast.makeText(
                            this@ResumenSemanalActividad,
                            "No se pudo generar el resumen semanal",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun mostrarResumen() {
        val resumen = CalculadorResumenSemanal.calcular(actividades, inicioSemana)
        val formatoRango = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-AR"))
        findViewById<TextView>(R.id.textoRangoSemana).text =
            "${resumen.inicio.format(formatoRango)} — ${resumen.fin.format(formatoRango)}"
        findViewById<TextView>(R.id.textoContadoresSemana).text =
            "${resumen.pendientes} pendientes  ·  ${resumen.completadas} completadas\n" +
                    "${resumen.eventos} eventos  ·  ${resumen.vencidas} vencidas"

        val contenedor = findViewById<LinearLayout>(R.id.contenedorResumenSemana)
        contenedor.removeAllViews()

        if (resumen.actividades.isEmpty()) {
            contenedor.addView(TextView(this).apply {
                text = "No hay actividades programadas para esta semana."
                textSize = 16f
                setTextColor(color(R.color.on_surface_variant))
                setPadding(0, dp(18), 0, dp(18))
            })
            return
        }

        val formatoDia = DateTimeFormatter.ofPattern(
            "EEEE d 'de' MMMM",
            Locale.forLanguageTag("es-AR")
        )
        resumen.actividades.groupBy { checkNotNull(it.fecha) }.forEach { (fecha, items) ->
            contenedor.addView(TextView(this).apply {
                text = fecha.format(formatoDia).replaceFirstChar { it.uppercase() }
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(color(R.color.primary_dark))
                setPadding(0, dp(14), 0, dp(8))
            })

            items.forEach { actividad ->
                contenedor.addView(TextView(this).apply {
                    text = buildString {
                        append(actividad.titulo)
                        append("\n")
                        append(if (actividad.tipo == TipoActividad.TAREA) "Tarea" else "Evento")
                        actividad.hora?.let { append(" · ${it.format(FORMATO_HORA)}") }
                        append(" · Prioridad ${actividad.prioridad.name.lowercase()}")
                        actividad.estadoTarea?.let {
                            append(if (it == EstadoTarea.COMPLETADA) " · Completada" else " · Pendiente")
                        }
                        if (actividad.recurrencia != Recurrencia.SIN_REPETICION) {
                            append("\nRepetición ${actividad.recurrencia.name.lowercase()}")
                        }
                    }
                    textSize = 15f
                    setTextColor(color(R.color.on_background))
                    setBackgroundResource(R.drawable.bg_card)
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(8) }
                })
            }
        }
    }

    private fun aplicarInsets() {
        val vista = findViewById<View>(R.id.pantallaResumenSemanal)
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

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    private fun color(id: Int): Int = ContextCompat.getColor(this, id)

    private companion object {
        val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
