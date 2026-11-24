package com.santi.organizame.resumen

import com.santi.organizame.filtros.OrdenActividad
import com.santi.organizame.filtros.OrdenadorActividades
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.TipoActividad
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

data class ResumenSemanal(
    val inicio: LocalDate,
    val fin: LocalDate,
    val actividades: List<Actividad>,
    val pendientes: Int,
    val completadas: Int,
    val eventos: Int,
    val vencidas: Int
)

object CalculadorResumenSemanal {

    fun inicioDeSemana(fecha: LocalDate): LocalDate =
        fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun calcular(
        actividades: List<Actividad>,
        inicio: LocalDate,
        ahora: LocalDateTime = LocalDateTime.now()
    ): ResumenSemanal {
        val fin = inicio.plusDays(6)
        val semana = OrdenadorActividades.ordenar(
            actividades.filter { actividad ->
                actividad.fecha?.let { !it.isBefore(inicio) && !it.isAfter(fin) } == true
            },
            OrdenActividad.FECHA_HORA
        )

        return ResumenSemanal(
            inicio = inicio,
            fin = fin,
            actividades = semana,
            pendientes = semana.count {
                it.tipo == TipoActividad.TAREA && it.estadoTarea == EstadoTarea.PENDIENTE
            },
            completadas = semana.count {
                it.tipo == TipoActividad.TAREA && it.estadoTarea == EstadoTarea.COMPLETADA
            },
            eventos = semana.count { it.tipo == TipoActividad.EVENTO },
            vencidas = semana.count { estaVencida(it, ahora) }
        )
    }

    private fun estaVencida(actividad: Actividad, ahora: LocalDateTime): Boolean {
        if (actividad.tipo != TipoActividad.TAREA ||
            actividad.estadoTarea != EstadoTarea.PENDIENTE
        ) return false

        val fecha = actividad.fecha ?: return false
        return fecha.isBefore(ahora.toLocalDate()) ||
                (fecha == ahora.toLocalDate() &&
                        actividad.hora?.isBefore(ahora.toLocalTime()) == true)
    }
}
