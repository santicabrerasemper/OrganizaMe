package com.santi.organizame.filtros

import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Prioridad
import java.text.Normalizer
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

object OrdenadorActividades {

    fun ordenar(
        actividades: List<Actividad>,
        orden: OrdenActividad
    ): List<Actividad> = actividades.sortedWith(comparador(orden))

    fun comparador(orden: OrdenActividad): Comparator<Actividad> =
        when (orden) {
            OrdenActividad.FECHA_HORA -> porFechaHora()
            OrdenActividad.PRIORIDAD -> compareByDescending<Actividad> {
                pesoPrioridad(it.prioridad)
            }.then(porFechaHora())

            OrdenActividad.TITULO -> compareBy<Actividad> {
                normalizar(it.titulo)
            }.then(porFechaHora())

            OrdenActividad.RECIENTES -> compareByDescending<Actividad> {
                it.fechaCreacion
            }.thenBy { normalizar(it.titulo) }
        }

    private fun porFechaHora(): Comparator<Actividad> =
        compareBy<Actividad> { it.fecha ?: LocalDate.MAX }
            .thenBy { it.hora ?: LocalTime.MAX }
            .thenByDescending { pesoPrioridad(it.prioridad) }
            .thenBy { normalizar(it.titulo) }

    private fun pesoPrioridad(prioridad: Prioridad): Int =
        when (prioridad) {
            Prioridad.ALTA -> 3
            Prioridad.MEDIA -> 2
            Prioridad.BAJA -> 1
        }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase(Locale.ROOT)
}
