package com.santi.organizame.ui.calendario

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.santi.organizame.databinding.ActivityCalendarioBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.widget.Toast
import com.santi.organizame.modelo.Actividad
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.santi.organizame.ui.consulta.ConsultaActividadesViewModel
import com.santi.organizame.ui.consulta.ConsultaActividadesViewModelFactory
import kotlinx.coroutines.launch
import com.santi.organizame.R

class CalendarioActividad : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarioBinding
    private lateinit var adaptador: AdaptadorDiasCalendario
    private var mesActual: YearMonth = YearMonth.now()
    private val viewModel: ConsultaActividadesViewModel by viewModels {
        ConsultaActividadesViewModelFactory(applicationContext)
    }
    private var actividades: List<Actividad> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarioBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val vista = binding.root

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






        findViewById<Button>(R.id.btnVolverCalendario).setOnClickListener {
            finish()
        }

        adaptador = AdaptadorDiasCalendario(emptyList()) { fecha ->
            mostrarActividadesDelDia(fecha)
        }
        binding.listaDiasCalendario.layoutManager = GridLayoutManager(this, 7)
        binding.listaDiasCalendario.adapter = adaptador

        binding.btnMesAnterior.setOnClickListener {
            mesActual = mesActual.minusMonths(1)
            mostrarMes(fechasConActividades())
        }

        binding.btnMesSiguiente.setOnClickListener {
            mesActual = mesActual.plusMonths(1)
            mostrarMes(fechasConActividades())
        }

        observarViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargar()
    }

    private fun mostrarMes(fechasConActividades: Set<LocalDate>) {
        val locale = Locale.forLanguageTag("es-AR")
        val formatoMes = DateTimeFormatter.ofPattern("MMMM yyyy", locale)

        binding.textoMes.text = mesActual
            .format(formatoMes)
            .replaceFirstChar { it.titlecase(locale) }

        val primerDiaDelMes = mesActual.atDay(1)
        val espaciosIniciales = primerDiaDelMes.dayOfWeek.value - 1
        val cantidadDias = mesActual.lengthOfMonth()
        val cantidadCasillas = maxOf(
            35,
            ((espaciosIniciales + cantidadDias + 6) / 7) * 7
        )

        val dias = (0 until cantidadCasillas).map { posicion ->
            val numeroDia = posicion - espaciosIniciales + 1

            if (numeroDia in 1..cantidadDias) {
                val fecha = mesActual.atDay(numeroDia)
                DiaCalendario(
                    fecha = fecha,
                    tieneActividades = fecha in fechasConActividades
                )
            } else {
                DiaCalendario(
                    fecha = null,
                    tieneActividades = false
                )
            }
        }

        adaptador.actualizarLista(dias)
    }


    private fun mostrarActividadesDelDia(fecha: LocalDate) {

        val actividadesDelDia = actividades.filter {
            it.fecha == fecha
        }

        fun dp(valor: Int): Int {
            return (valor * resources.displayMetrics.density).toInt()
        }

        val colorTexto = ContextCompat.getColor(
            this, R.color.on_background
        )

        val colorSecundario = ContextCompat.getColor(
            this, R.color.on_surface_variant
        )

        val colorTarjeta = ContextCompat.getColor(
            this, R.color.surface_variant
        )

        // Contenedor de las actividades
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(20), dp(20),
                dp(20), dp(8)
            )
        }

        if (actividadesDelDia.isEmpty()) {

            val mensaje = TextView(this).apply {
                text = "📅  No hay actividades para este día."
                textSize = 16f
                setTextColor(colorTexto)
            }

            contenedor.addView(mensaje)

        } else {

            actividadesDelDia.forEachIndexed { indice, actividad ->

                // Tarjeta individual
                val tarjeta = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL

                    setPadding(
                        dp(16), dp(16),
                        dp(16), dp(16)
                    )

                    background = GradientDrawable().apply {
                        setColor(colorTarjeta)
                        cornerRadius = dp(16).toFloat()
                    }
                }

                // Título
                val titulo = TextView(this).apply {
                    text = "${indice + 1}. ${actividad.titulo}"
                    textSize = 24f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(colorTexto)
                }

                tarjeta.addView(titulo)

                // Información
                val datos = buildString {

                    append(
                        if (actividad.tipo.name == "TAREA")
                            "📋  Tarea"
                        else
                            "📅  Evento"
                    )

                    append("\n")

                    actividad.hora?.let {
                        append("🕒  Hora: ")
                        append(
                            it.format(
                                DateTimeFormatter.ofPattern("HH:mm")
                            )
                        )
                        append(" hs\n")
                    }

                    val iconoPrioridad = when (
                        actividad.prioridad.name
                    ) {
                        "ALTA" -> "🔴"
                        "MEDIA" -> "🟡"
                        else -> "🟢"
                    }

                    append("$iconoPrioridad  Prioridad: ")
                    append(actividad.prioridad)

                    actividad.estadoTarea?.let {
                        append("\n")

                        val iconoEstado =
                            if (it.name == "COMPLETADA")
                                "✅"
                            else
                                "⏳"

                        append("$iconoEstado  Estado: $it")
                    }

                    actividad.descripcion
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            append("\n\n")
                            append("📝  ")
                            append(it)
                        }
                }

                val textoDatos = TextView(this).apply {
                    text = datos
                    textSize = 18f
                    setTextColor(colorSecundario)

                    setPadding(
                        0, dp(12), 0, 0
                    )

                    setLineSpacing(
                        dp(6).toFloat(), 1f
                    )
                }

                tarjeta.addView(textoDatos)

                val parametros = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                }

                contenedor.addView(tarjeta, parametros)
            }
        }

        // Permite desplazarse cuando hay muchas actividades
        val scroll = ScrollView(this).apply {
            addView(contenedor)
        }

        val fechaFormateada = fecha.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )

        AlertDialog.Builder(this)
            .setTitle("Actividades · $fechaFormateada")
            .setView(scroll)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.estado.collect { estado ->
                        if (estado.cargaCompletada) {
                            actividades = estado.actividades
                            mostrarMes(fechasConActividades())
                        }
                    }
                }
                launch {
                    viewModel.errores.collect { mensaje ->
                        Toast.makeText(
                            this@CalendarioActividad,
                            mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun fechasConActividades(): Set<LocalDate> =
        actividades.mapNotNull { it.fecha }.toSet()
}
