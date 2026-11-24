package com.santi.organizame.modelo

import java.time.Instant
import java.time.LocalDateTime

object GeneradorRecurrencia {

    fun crearSiguiente(
        actividadCompletada: Actividad,
        momento: Instant = Instant.now(),
        ahoraLocal: LocalDateTime = LocalDateTime.now()
    ): Actividad? {
        if (actividadCompletada.tipo != TipoActividad.TAREA ||
            actividadCompletada.recurrencia == Recurrencia.SIN_REPETICION
        ) return null

        val fecha = actividadCompletada.fecha ?: return null
        val siguienteFecha = CalculadoraRecurrencia.siguienteFecha(
            fechaActual = fecha,
            hora = actividadCompletada.hora,
            recurrencia = actividadCompletada.recurrencia,
            ahora = ahoraLocal
        ) ?: return null

        return actividadCompletada.copy(
            id = 0,
            fecha = siguienteFecha,
            estadoTarea = EstadoTarea.PENDIENTE,
            fechaCompletada = null,
            fechaRecordatorioProgramado = null,
            recurrenciaProcesada = false,
            fechaCreacion = momento,
            fechaModificacion = momento
        )
    }
}
