package com.santi.organizame

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
import com.santi.organizame.modelo.ActividadRepository
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.santi.organizame.modelo.OrganizaMeDatabase

class CalendarioActividad : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarioBinding
    private lateinit var adaptador: AdaptadorDiasCalendario
    private var mesActual: YearMonth = YearMonth.now()
    private lateinit var actividadRepository: ActividadRepository
    private var actividades: List<Actividad> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<Button>(R.id.btnVolverCalendario).setOnClickListener {
            finish()
        }

        actividadRepository = ActividadRepository(
            OrganizaMeDatabase.obtener(applicationContext).actividadDao()
        )

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

        cargarActividades()
    }

    private fun mostrarMes(fechasConActividades: Set<LocalDate>) {
        val locale = Locale("es", "AR")
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

        val mensaje = if (actividadesDelDia.isEmpty()) {

            "No hay actividades para este día."

        } else {

            actividadesDelDia.mapIndexed { indice, actividad ->

                buildString {
                    append("${indice + 1}. ${actividad.titulo}\n")

                    actividad.descripcion?.takeIf { it.isNotBlank() }?.let {
                        append("Descripción: $it\n")
                    }

                    actividad.hora?.let {
                        append("Hora: $it\n")
                    }

                    append("Prioridad: ${actividad.prioridad}\n")

                    actividad.estadoTarea?.let {
                        append("Estado: $it\n")
                    }
                }.trimEnd()

            }.joinToString("\n\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Actividades del ${fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
            .setMessage(mensaje)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun cargarActividades() {
        actividadRepository.obtenerTodas { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { lista ->
                        actividades = lista
                        mostrarMes(fechasConActividades())
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

    private fun fechasConActividades(): Set<LocalDate> =
        actividades.mapNotNull { it.fecha }.toSet()
}